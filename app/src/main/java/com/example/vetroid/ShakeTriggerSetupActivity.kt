package com.example.vetroid

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.example.vetroid.data.AppDatabase
import com.example.vetroid.data.Trigger
import com.google.android.material.button.MaterialButton
import com.google.android.material.radiobutton.MaterialRadioButton
import kotlinx.coroutines.launch
import org.json.JSONObject

class ShakeTriggerSetupActivity : BaseActivity() {

    private lateinit var database: AppDatabase
    private var scenarioId: Long = 0
    private var triggerId: Long = 0

    companion object {
        const val TAG = "ShakeTriggerSetup"
        const val TRIGGER_TYPE = "SHAKE"
        const val SENSITIVITY_LOW = "LOW"
        const val SENSITIVITY_MEDIUM = "MEDIUM"
        const val SENSITIVITY_HIGH = "HIGH"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shake_trigger_setup)

        database = AppDatabase.getDatabase(this)
        scenarioId = intent.getLongExtra("scenario_id", 0)
        triggerId = intent.getLongExtra("trigger_id", 0)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        if (triggerId > 0) {
            lifecycleScope.launch {
                val trigger = database.triggerDao().getTriggerById(triggerId)
                trigger?.let { loadSelection(it.params) }
            }
        }

        findViewById<MaterialButton>(R.id.btnSave).setOnClickListener {
            saveTrigger()
        }
    }

    private fun loadSelection(paramsJson: String) {
        try {
            val sensitivity = JSONObject(paramsJson).optString("sensitivity", SENSITIVITY_MEDIUM)
            val rbId = when (sensitivity) {
                SENSITIVITY_LOW  -> R.id.rbLow
                SENSITIVITY_HIGH -> R.id.rbHigh
                else             -> R.id.rbMedium
            }
            runOnUiThread { findViewById<MaterialRadioButton>(rbId).isChecked = true }
        } catch (e: Exception) {
            Log.e(TAG, "Seçim yüklenemedi: ${e.message}")
        }
    }

    private fun getSelectedSensitivity(): String {
        return when {
            findViewById<MaterialRadioButton>(R.id.rbLow).isChecked  -> SENSITIVITY_LOW
            findViewById<MaterialRadioButton>(R.id.rbHigh).isChecked -> SENSITIVITY_HIGH
            else                                                      -> SENSITIVITY_MEDIUM
        }
    }

    private fun saveTrigger() {
        val sensitivity = getSelectedSensitivity()
        val paramsJson = JSONObject().apply { put("sensitivity", sensitivity) }.toString()

        lifecycleScope.launch {
            try {
                if (triggerId > 0) {
                    val existing = database.triggerDao().getTriggerById(triggerId)
                    existing?.let {
                        database.triggerDao().update(it.copy(params = paramsJson, isActive = true))
                    }
                } else {
                    database.triggerDao().insert(
                        Trigger(scenarioId = scenarioId, type = TRIGGER_TYPE, params = paramsJson)
                    )
                }
                Log.d(TAG, "Sallama tetikleyicisi kaydedildi: sensitivity=$sensitivity")

                // Servis başlat
                startService(Intent(this@ShakeTriggerSetupActivity, ShakeDetectorService::class.java))

                runOnUiThread {
                    Toast.makeText(this@ShakeTriggerSetupActivity, "Tetikleyici kaydedildi", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Kaydetme hatası: ${e.message}", e)
                runOnUiThread {
                    Toast.makeText(this@ShakeTriggerSetupActivity, "Hata: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
