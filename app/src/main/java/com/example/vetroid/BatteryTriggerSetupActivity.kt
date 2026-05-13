package com.example.vetroid

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.example.vetroid.data.AppDatabase
import com.example.vetroid.data.Trigger
import com.example.vetroid.receiver.SystemEventReceiver
import com.google.android.material.button.MaterialButton
import com.google.android.material.radiobutton.MaterialRadioButton
import kotlinx.coroutines.launch
import org.json.JSONObject

class BatteryTriggerSetupActivity : BaseActivity() {

    private lateinit var database: AppDatabase
    private var scenarioId: Long = 0
    private var triggerId: Long = 0

    companion object {
        const val TAG = "BatteryTriggerSetup"
        const val TRIGGER_TYPE = "BATTERY"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_battery_trigger_setup)

        database = AppDatabase.getDatabase(this)
        scenarioId = intent.getLongExtra("scenario_id", 0)
        triggerId = intent.getLongExtra("trigger_id", 0)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        // Daha önce kaydedilmiş bir tetikleyici varsa seçimi yükle
        if (triggerId > 0) {
            lifecycleScope.launch {
                val trigger = database.triggerDao().getTriggerById(triggerId)
                trigger?.let { loadSelection(it.params) }
            }
        }

        findViewById<MaterialButton>(R.id.btnSave).setOnClickListener {
            saveTriger()
        }
    }

    private fun loadSelection(paramsJson: String) {
        try {
            val eventType = JSONObject(paramsJson).getString("eventType")
            val rb = when (eventType) {
                SystemEventReceiver.EVENT_CHARGING_CONNECTED    -> R.id.rbChargingConnected
                SystemEventReceiver.EVENT_CHARGING_DISCONNECTED -> R.id.rbChargingDisconnected
                SystemEventReceiver.EVENT_BATTERY_LOW           -> R.id.rbBatteryLow
                SystemEventReceiver.EVENT_HEADSET_PLUGGED       -> R.id.rbHeadsetPlugged
                SystemEventReceiver.EVENT_HEADSET_UNPLUGGED     -> R.id.rbHeadsetUnplugged
                else -> R.id.rbChargingConnected
            }
            findViewById<MaterialRadioButton>(rb).isChecked = true
        } catch (e: Exception) {
            Log.e(TAG, "Seçim yüklenemedi: ${e.message}")
        }
    }

    private fun getSelectedEventType(): String {
        return when {
            findViewById<MaterialRadioButton>(R.id.rbChargingDisconnected).isChecked -> SystemEventReceiver.EVENT_CHARGING_DISCONNECTED
            findViewById<MaterialRadioButton>(R.id.rbBatteryLow).isChecked           -> SystemEventReceiver.EVENT_BATTERY_LOW
            findViewById<MaterialRadioButton>(R.id.rbHeadsetPlugged).isChecked       -> SystemEventReceiver.EVENT_HEADSET_PLUGGED
            findViewById<MaterialRadioButton>(R.id.rbHeadsetUnplugged).isChecked     -> SystemEventReceiver.EVENT_HEADSET_UNPLUGGED
            else                                                                      -> SystemEventReceiver.EVENT_CHARGING_CONNECTED
        }
    }

    private fun saveTriger() {
        val eventType = getSelectedEventType()
        val paramsJson = JSONObject().apply { put("eventType", eventType) }.toString()

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
                Log.d(TAG, "Tetikleyici kaydedildi: $eventType")
                runOnUiThread {
                    Toast.makeText(this@BatteryTriggerSetupActivity, "Tetikleyici kaydedildi", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Kaydetme hatası: ${e.message}", e)
                runOnUiThread {
                    Toast.makeText(this@BatteryTriggerSetupActivity, "Hata: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
