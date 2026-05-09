package com.example.vetroid

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.vetroid.data.AppDatabase
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch

class TriggersActivity : AppCompatActivity() {

    private var scenarioId: Long = 0
    private var geofenceTriggerId: Long = 0
    private var timeTriggerId: Long = 0
    private lateinit var prefs: SharedPreferences

    companion object {
        private const val PREFS_NAME = "vetroid_prefs"
        private const val KEY_GEOFENCE_TRIGGER_ID = "current_geofence_trigger_id"
        private const val KEY_TIME_TRIGGER_ID = "current_time_trigger_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_triggers)

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        scenarioId = intent.getLongExtra("scenario_id", 0)
        
        // SharedPreferences'tan triggerId'leri al
        geofenceTriggerId = prefs.getLong(KEY_GEOFENCE_TRIGGER_ID, 0)
        timeTriggerId = prefs.getLong(KEY_TIME_TRIGGER_ID, 0)
        
        Log.d("TriggersActivity", "scenarioId: $scenarioId, geofenceTriggerId: $geofenceTriggerId, timeTriggerId: $timeTriggerId")

        // Bu senaryoya ait en son geofence ve time trigger'ları bul (veritabanından)
        loadLastTriggers()

        val mainLayout = findViewById<View>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

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

        // Diğer kartlar için boş listener (ileride doldurulacak)
        findViewById<MaterialCardView>(R.id.cardPil).setOnClickListener { }
        findViewById<MaterialCardView>(R.id.cardUygulamalar).setOnClickListener { }
    }

    private fun loadLastTriggers() {
        val database = AppDatabase.getDatabase(this)
        lifecycleScope.launch {
            val triggers = database.triggerDao().getTriggersByScenarioSync(scenarioId)
            
            val lastGeofence = triggers.lastOrNull { it.type == "GEOFENCE" }
            geofenceTriggerId = lastGeofence?.id ?: 0
            
            val lastTime = triggers.lastOrNull { it.type == "TIME" }
            timeTriggerId = lastTime?.id ?: 0
            
            Log.d("TriggersActivity", "En son geofence triggerId: $geofenceTriggerId, time triggerId: $timeTriggerId")
        }
    }
}