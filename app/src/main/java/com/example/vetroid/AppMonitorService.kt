package com.example.vetroid

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import com.example.vetroid.data.AppDatabase
import com.example.vetroid.executor.ActionExecutor
import com.example.vetroid.executor.ConstraintChecker
import com.example.vetroid.executor.LogHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

class AppMonitorService : Service() {

    companion object {
        private const val TAG = "AppMonitorService"
        private const val CHANNEL_ID = "app_monitor_channel"
        private const val NOTIF_ID = 42
        private const val POLL_INTERVAL_MS = 2000L
    }

    private val handler = Handler(Looper.getMainLooper())
    private var lastForegroundPackage = ""
    private var lastEventQueryTime = System.currentTimeMillis()

    private val pollRunnable = object : Runnable {
        override fun run() {
            checkForegroundApp()
            handler.postDelayed(this, POLL_INTERVAL_MS)
        }
    }

    override fun onCreate() {
        super.onCreate()
        startForegroundWithNotification()
        handler.post(pollRunnable)
        Log.d(TAG, "AppMonitorService başlatıldı")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        handler.removeCallbacks(pollRunnable)
        Log.d(TAG, "AppMonitorService durduruldu")
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun checkForegroundApp() {
        val usm = getSystemService(USAGE_STATS_SERVICE) as UsageStatsManager
        val now = System.currentTimeMillis()
        val events = usm.queryEvents(lastEventQueryTime - 500, now)
        lastEventQueryTime = now

        val event = UsageEvents.Event()
        var latestForeground: String? = null

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                latestForeground = event.packageName
            }
        }

        // Uygulama değişmediyse işlem yok
        if (latestForeground == null || latestForeground == lastForegroundPackage) return
        // Kendi uygulamamızı atla
        if (latestForeground == packageName) return

        val openedPackage = latestForeground
        lastForegroundPackage = openedPackage
        Log.d(TAG, "Ön plana geçen uygulama: $openedPackage")

        checkAndFireTriggers(openedPackage)
    }

    private fun checkAndFireTriggers(packageName: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(this@AppMonitorService)
                val triggers = db.triggerDao().getActiveTriggersByType(AppTriggerSetupActivity.TRIGGER_TYPE)

                for (trigger in triggers) {
                    val params = JSONObject(trigger.params)
                    if (params.optString("packageName") != packageName) continue

                    val scenario = db.scenarioDao().getScenarioById(trigger.scenarioId) ?: continue
                    if (!scenario.isActive) continue

                    val constraints = db.constraintDao().getConstraintsByScenarioSync(scenario.id)
                    if (!ConstraintChecker(this@AppMonitorService).checkAll(constraints)) {
                        Log.d(TAG, "Kısıtlama engelledi: ${scenario.name}")
                        LogHelper.log(db, scenario.id, scenario.name, "APP_USAGE", false, "Kısıtlama engelledi")
                        continue
                    }

                    val actions = db.actionDao().getActionsByScenarioSync(scenario.id)
                    val executor = ActionExecutor(this@AppMonitorService)
                    for (action in actions) {
                        if (action.isActive) executor.executeAction(action)
                    }
                    LogHelper.log(db, scenario.id, scenario.name, "APP_USAGE", true)
                    Log.d(TAG, "✅ Senaryo çalıştırıldı: ${scenario.name}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Trigger kontrolü hatası: ${e.message}", e)
            }
        }
    }

    private fun startForegroundWithNotification() {
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "Uygulama İzleyici", NotificationManager.IMPORTANCE_MIN)
                    .apply { description = "Vetroid uygulama tetikleyicileri için arka plan servisi" }
            )
        }

        val notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION") Notification.Builder(this)
        }
            .setSmallIcon(android.R.drawable.ic_menu_recent_history)
            .setContentTitle("Vetroid")
            .setContentText("Uygulama tetikleyicileri aktif")
            .setPriority(Notification.PRIORITY_MIN)
            .build()

        startForeground(NOTIF_ID, notification)
    }
}
