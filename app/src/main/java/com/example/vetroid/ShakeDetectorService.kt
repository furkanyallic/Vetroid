package com.example.vetroid

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import com.example.vetroid.data.AppDatabase
import com.example.vetroid.executor.ActionExecutor
import com.example.vetroid.executor.ConstraintChecker
import com.example.vetroid.executor.LogHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import kotlin.math.sqrt

class ShakeDetectorService : Service(), SensorEventListener {

    companion object {
        private const val TAG = "ShakeDetectorService"
        private const val CHANNEL_ID = "shake_detector_channel"
        private const val NOTIF_ID = 43
        private const val COOLDOWN_MS = 2000L
        private const val WINDOW_MS = 1200L
    }

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null

    private var lastShakeTime = 0L
    private val shakeTimes = ArrayDeque<Long>()

    // Threshold cache — DB'ye her sensor event'te gitmeyelim
    @Volatile private var cachedThreshold = 11f
    @Volatile private var lastThresholdRefresh = 0L
    private val THRESHOLD_REFRESH_MS = 5000L

    override fun onCreate() {
        super.onCreate()
        startForegroundWithNotification()
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME)
            Log.d(TAG, "ShakeDetectorService başlatıldı")
        } else {
            Log.w(TAG, "Accelerometer sensörü bulunamadı, servis durduruluyor")
            stopSelf()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        sensorManager.unregisterListener(this)
        Log.d(TAG, "ShakeDetectorService durduruldu")
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]
        val gForce = sqrt((x * x + y * y + z * z).toDouble()).toFloat()

        val now = System.currentTimeMillis()
        if (now - lastThresholdRefresh > THRESHOLD_REFRESH_MS) {
            lastThresholdRefresh = now
            CoroutineScope(Dispatchers.IO).launch {
                cachedThreshold = getCurrentThreshold()
            }
        }

        if (gForce > cachedThreshold) {
            CoroutineScope(Dispatchers.IO).launch { onShakeDetected() }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    private suspend fun getCurrentThreshold(): Float {
        val db = AppDatabase.getDatabase(this)
        val triggers = db.triggerDao().getActiveTriggersByType(ShakeTriggerSetupActivity.TRIGGER_TYPE)
        if (triggers.isEmpty()) return Float.MAX_VALUE

        // En hassas (en düşük threshold) tetikleyiciyi kullan
        var minThreshold = Float.MAX_VALUE
        for (trigger in triggers) {
            val sensitivity = try {
                JSONObject(trigger.params).optString("sensitivity", ShakeTriggerSetupActivity.SENSITIVITY_MEDIUM)
            } catch (e: Exception) {
                ShakeTriggerSetupActivity.SENSITIVITY_MEDIUM
            }
            val t = sensitivityToThreshold(sensitivity)
            if (t < minThreshold) minThreshold = t
        }
        return minThreshold
    }

    private fun sensitivityToThreshold(sensitivity: String): Float = when (sensitivity) {
        ShakeTriggerSetupActivity.SENSITIVITY_LOW  -> 17f
        ShakeTriggerSetupActivity.SENSITIVITY_HIGH -> 13f
        else                                       -> 11f  // MEDIUM
    }

    private fun sensitivityToCount(sensitivity: String): Int = when (sensitivity) {
        ShakeTriggerSetupActivity.SENSITIVITY_LOW  -> 7
        ShakeTriggerSetupActivity.SENSITIVITY_HIGH -> 4
        else                                       -> 5  // MEDIUM
    }

    // Çağrı her zaman IO dispatcher'dan geldiği için synchronized ile koruyoruz
    private suspend fun onShakeDetected() {
        val now = System.currentTimeMillis()
        val currentCount: Int

        synchronized(shakeTimes) {
            if (now - lastShakeTime < COOLDOWN_MS) return
            while (shakeTimes.isNotEmpty() && now - shakeTimes.first() > WINDOW_MS) {
                shakeTimes.removeFirst()
            }
            shakeTimes.addLast(now)
            currentCount = shakeTimes.size
        }

        val db = AppDatabase.getDatabase(this@ShakeDetectorService)
        val triggers = db.triggerDao().getActiveTriggersByType(ShakeTriggerSetupActivity.TRIGGER_TYPE)

        for (trigger in triggers) {
            val sensitivity = try {
                JSONObject(trigger.params).optString("sensitivity", ShakeTriggerSetupActivity.SENSITIVITY_MEDIUM)
            } catch (e: Exception) {
                ShakeTriggerSetupActivity.SENSITIVITY_MEDIUM
            }
            if (currentCount < sensitivityToCount(sensitivity)) continue

            val scenario = db.scenarioDao().getScenarioById(trigger.scenarioId) ?: continue
            if (!scenario.isActive) continue

            val constraints = db.constraintDao().getConstraintsByScenarioSync(scenario.id)
            if (!ConstraintChecker(this@ShakeDetectorService).checkAll(constraints)) {
                Log.d(TAG, "Kısıtlama engelledi: ${scenario.name}")
                LogHelper.log(db, scenario.id, scenario.name, "SHAKE", false, "Kısıtlama engelledi")
                continue
            }

            synchronized(shakeTimes) {
                lastShakeTime = System.currentTimeMillis()
                shakeTimes.clear()
            }

            val actions = db.actionDao().getActionsByScenarioSync(scenario.id)
            val executor = ActionExecutor(this@ShakeDetectorService)
            for (action in actions) {
                if (action.isActive) executor.executeAction(action)
            }
            LogHelper.log(db, scenario.id, scenario.name, "SHAKE", true)
            Log.d(TAG, "✅ Shake senaryo çalıştırıldı: ${scenario.name}")

            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                (getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(VIBRATOR_SERVICE) as Vibrator
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(300)
            }
        }
    }

    private fun startForegroundWithNotification() {
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "Sallama Dedektörü", NotificationManager.IMPORTANCE_MIN)
                    .apply { description = "Vetroid sallama tetikleyicisi için arka plan servisi" }
            )
        }

        val notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION") Notification.Builder(this)
        }
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentTitle("Vetroid")
            .setContentText("Sallama tetikleyicisi aktif")
            .setPriority(Notification.PRIORITY_MIN)
            .build()

        startForeground(NOTIF_ID, notification)
    }
}
