package com.example.vetroid

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.example.vetroid.data.Action
import com.example.vetroid.data.ActionParams
import com.example.vetroid.data.AppDatabase
import com.example.vetroid.executor.ActionExecutor
import com.google.android.material.button.MaterialButton
import com.google.android.material.radiobutton.MaterialRadioButton
import kotlinx.coroutines.launch

// Wi-Fi ve Bluetooth için ortak setup activity.
// action_type extra ile hangi aksiyon olduğu belirlenir: "WIFI" veya "BLUETOOTH"
class WifiBluetoothSetupActivity : BaseActivity() {

    private lateinit var database: AppDatabase
    private var scenarioId: Long = 0
    private var actionId: Long = 0
    private lateinit var actionType: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wifi_bluetooth_setup)

        database = AppDatabase.getDatabase(this)
        scenarioId = intent.getLongExtra("scenario_id", 0)
        actionId = intent.getLongExtra("action_id", 0)
        actionType = intent.getStringExtra("action_type") ?: ActionExecutor.ACTION_TYPE_WIFI

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.title = if (actionType == ActionExecutor.ACTION_TYPE_WIFI) "Wi-Fi Ayarı" else "Bluetooth Ayarı"
        toolbar.setNavigationOnClickListener { finish() }

        val rbEnable = findViewById<MaterialRadioButton>(R.id.rbEnable)
        val rbDisable = findViewById<MaterialRadioButton>(R.id.rbDisable)

        // Kaydedilmiş değeri yükle
        if (actionId > 0) {
            lifecycleScope.launch {
                val action = database.actionDao().getActionById(actionId)
                action?.let {
                    val params = ActionParams.fromJson(it.params)
                    val isEnabled = if (actionType == ActionExecutor.ACTION_TYPE_WIFI) {
                        params.wifiEnabled
                    } else {
                        params.bluetoothEnabled
                    }
                    rbEnable.isChecked = isEnabled != false
                    rbDisable.isChecked = isEnabled == false
                }
            }
        }

        val rbEnableLabel = if (actionType == ActionExecutor.ACTION_TYPE_WIFI) "Wi-Fi Aç" else "Bluetooth Aç"
        val rbDisableLabel = if (actionType == ActionExecutor.ACTION_TYPE_WIFI) "Wi-Fi Kapat" else "Bluetooth Kapat"
        rbEnable.text = rbEnableLabel
        rbDisable.text = rbDisableLabel

        findViewById<MaterialButton>(R.id.btnSave).setOnClickListener {
            saveAction(rbEnable.isChecked)
        }
    }

    private fun saveAction(enable: Boolean) {
        val params = if (actionType == ActionExecutor.ACTION_TYPE_WIFI) {
            ActionParams(wifiEnabled = enable)
        } else {
            ActionParams(bluetoothEnabled = enable)
        }

        lifecycleScope.launch {
            try {
                if (actionId > 0) {
                    val existing = database.actionDao().getActionById(actionId)
                    existing?.let {
                        database.actionDao().update(it.copy(params = params.toJson(), isActive = true))
                    }
                } else {
                    database.actionDao().insert(
                        Action(scenarioId = scenarioId, type = actionType, params = params.toJson())
                    )
                }
                Log.d("WifiBluetoothSetup", "Eylem kaydedildi: $actionType, enable=$enable")
                runOnUiThread {
                    Toast.makeText(this@WifiBluetoothSetupActivity, "Eylem kaydedildi", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                Log.e("WifiBluetoothSetup", "Kaydetme hatası: ${e.message}", e)
                runOnUiThread {
                    Toast.makeText(this@WifiBluetoothSetupActivity, "Hata: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
