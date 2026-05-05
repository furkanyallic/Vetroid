package com.example.vetroid

import android.content.Intent
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
    private var triggerId: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_triggers)

        scenarioId = intent.getLongExtra("scenario_id", 0)
        Log.d("TriggersActivity", "scenarioId: $scenarioId")

        // Bu senaryoya ait en son geofence trigger'ı bul
        loadLastGeofenceTrigger()

        val mainLayout = findViewById<View>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<MaterialCardView>(R.id.cardKonum).setOnClickListener {
            Log.d("TriggersActivity", "Konum kartı tıklandı, triggerId: $triggerId")
            try {
                val intent = Intent(this, GeofenceSetupActivity::class.java)
                intent.putExtra("scenario_id", scenarioId)
                intent.putExtra("trigger_id", triggerId)
                startActivity(intent)
            } catch (e: Exception) {
                Log.e("TriggersActivity", "Hata: ${e.message}")
                Toast.makeText(this, "Hata: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        // Diğer kartlar için boş listener (ileride doldurulacak)
        findViewById<MaterialCardView>(R.id.cardTarihSaat).setOnClickListener { }
        findViewById<MaterialCardView>(R.id.cardPil).setOnClickListener { }
        findViewById<MaterialCardView>(R.id.cardUygulamalar).setOnClickListener { }
    }

    private fun loadLastGeofenceTrigger() {
        val database = AppDatabase.getDatabase(this)
        lifecycleScope.launch {
            val triggers = database.triggerDao().getTriggersByScenarioSync(scenarioId)
            val lastGeofence = triggers.lastOrNull { it.type == "GEOFENCE" }
            triggerId = lastGeofence?.id ?: 0
            Log.d("TriggersActivity", "En son geofence triggerId: $triggerId")
        }
    }
}