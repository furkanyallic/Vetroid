package com.example.vetroid

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.example.vetroid.data.AppDatabase
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch

class TriggersActivity : BaseActivity() {

    private var scenarioId: Long = 0
    private var geofenceTriggerId: Long = 0
    private var timeTriggerId: Long = 0
    private var batteryTriggerId: Long = 0
    private var appTriggerId: Long = 0
    private var shakeTriggerId: Long = 0
    private lateinit var prefs: SharedPreferences

    companion object {
        private const val PREFS_NAME = "vetroid_prefs"
        private const val KEY_GEOFENCE_TRIGGER_ID = "current_geofence_trigger_id"
        private const val KEY_TIME_TRIGGER_ID = "current_time_trigger_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_triggers)

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        scenarioId = intent.getLongExtra("scenario_id", 0)
        
        // SharedPreferences'tan triggerId'leri al
        geofenceTriggerId = prefs.getLong(KEY_GEOFENCE_TRIGGER_ID, 0)
        timeTriggerId = prefs.getLong(KEY_TIME_TRIGGER_ID, 0)
        
        Log.d("TriggersActivity", "scenarioId: $scenarioId, geofenceTriggerId: $geofenceTriggerId, timeTriggerId: $timeTriggerId")

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        // Bu senaryoya ait en son geofence ve time trigger'ları bul (veritabanından)
        loadLastTriggers()

        // Konum kartı - Geofence
        findViewById<MaterialCardView>(R.id.cardKonum).setOnClickListener {
            Log.d("TriggersActivity", "Konum kartı tıklandı, triggerId: $geofenceTriggerId")
            try {
                val intent = Intent(this, GeofenceSetupActivity::class.java)
                intent.putExtra("scenario_id", scenarioId)
                intent.putExtra("trigger_id", geofenceTriggerId)
                startActivity(intent)
            } catch (e: Exception) {
                Log.e("TriggersActivity", "Hata: ${e.message}")
                Toast.makeText(this, "Hata: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        // Tarih/Saat kartı - Time Trigger
        findViewById<MaterialCardView>(R.id.cardTarihSaat).setOnClickListener {
            Log.d("TriggersActivity", "Tarih/Saat kartı tıklandı, triggerId: $timeTriggerId")
            try {
                val intent = Intent(this, TimeSetupActivity::class.java)
                intent.putExtra("scenario_id", scenarioId)
                intent.putExtra("trigger_id", timeTriggerId)
                startActivity(intent)
            } catch (e: Exception) {
                Log.e("TriggersActivity", "Hata: ${e.message}")
                Toast.makeText(this, "Hata: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<MaterialCardView>(R.id.cardPil).setOnClickListener {
            val intent = Intent(this, BatteryTriggerSetupActivity::class.java).apply {
                putExtra("scenario_id", scenarioId)
                putExtra("trigger_id", batteryTriggerId)
            }
            startActivity(intent)
        }

        findViewById<MaterialCardView>(R.id.cardUygulamalar).setOnClickListener {
            startActivity(Intent(this, AppTriggerSetupActivity::class.java).apply {
                putExtra("scenario_id", scenarioId)
                putExtra("trigger_id", appTriggerId)
            })
        }

        findViewById<MaterialCardView>(R.id.cardSallama).setOnClickListener {
            startActivity(Intent(this, ShakeTriggerSetupActivity::class.java).apply {
                putExtra("scenario_id", scenarioId)
                putExtra("trigger_id", shakeTriggerId)
            })
        }
    }

    override fun onResume() {
        super.onResume()
        loadLastTriggers()
    }

    private fun loadLastTriggers() {
        val database = AppDatabase.getDatabase(this)
        lifecycleScope.launch {
            val triggers = database.triggerDao().getTriggersByScenarioSync(scenarioId)

            geofenceTriggerId = triggers.lastOrNull { it.type == "GEOFENCE" }?.id ?: 0
            timeTriggerId     = triggers.lastOrNull { it.type == "TIME" }?.id ?: 0
            batteryTriggerId  = triggers.lastOrNull { it.type == "BATTERY" }?.id ?: 0
            appTriggerId      = triggers.lastOrNull { it.type == AppTriggerSetupActivity.TRIGGER_TYPE }?.id ?: 0
            shakeTriggerId    = triggers.lastOrNull { it.type == ShakeTriggerSetupActivity.TRIGGER_TYPE }?.id ?: 0

            Log.d("TriggersActivity", "Triggers — geofence:$geofenceTriggerId time:$timeTriggerId battery:$batteryTriggerId app:$appTriggerId shake:$shakeTriggerId")

            val saved   = android.graphics.Color.parseColor("#C8E6C9")
            val default = android.graphics.Color.parseColor("#E8E8E8")
            runOnUiThread {
                findViewById<MaterialCardView>(R.id.cardKonum).setCardBackgroundColor(if (geofenceTriggerId > 0) saved else default)
                findViewById<MaterialCardView>(R.id.cardTarihSaat).setCardBackgroundColor(if (timeTriggerId > 0) saved else default)
                findViewById<MaterialCardView>(R.id.cardPil).setCardBackgroundColor(if (batteryTriggerId > 0) saved else default)
                findViewById<MaterialCardView>(R.id.cardUygulamalar).setCardBackgroundColor(if (appTriggerId > 0) saved else default)
                findViewById<MaterialCardView>(R.id.cardSallama).setCardBackgroundColor(if (shakeTriggerId > 0) saved else default)
            }
        }
    }
}