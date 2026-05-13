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

class BrightnessActionSetupActivity : BaseActivity() {

    private lateinit var database: AppDatabase
    private var scenarioId: Long = 0
    private var actionId: Long = 0

    // -1 = otomatik, diğerleri Android'in 0-255 skalası
    private val BRIGHTNESS_AUTO = -1
    private val BRIGHTNESS_LOW = 77      // %30
    private val BRIGHTNESS_MEDIUM = 153  // %60
    private val BRIGHTNESS_HIGH = 255    // %100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_brightness_setup)

        database = AppDatabase.getDatabase(this)
        scenarioId = intent.getLongExtra("scenario_id", 0)
        actionId = intent.getLongExtra("action_id", 0)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        // Kaydedilmiş değeri yükle
        if (actionId > 0) {
            lifecycleScope.launch {
                val action = database.actionDao().getActionById(actionId)
                action?.let {
                    val params = ActionParams.fromJson(it.params)
                    applyBrightnessSelection(params.brightness ?: BRIGHTNESS_AUTO)
                }
            }
        }

        findViewById<MaterialButton>(R.id.btnSave).setOnClickListener {
            val brightness = getSelectedBrightness()
            saveAction(brightness)
        }
    }

    private fun applyBrightnessSelection(brightness: Int) {
        val rbAuto = findViewById<MaterialRadioButton>(R.id.rbAuto)
        val rbLow = findViewById<MaterialRadioButton>(R.id.rbLow)
        val rbMedium = findViewById<MaterialRadioButton>(R.id.rbMedium)
        val rbHigh = findViewById<MaterialRadioButton>(R.id.rbHigh)

        when (brightness) {
            BRIGHTNESS_AUTO -> rbAuto.isChecked = true
            BRIGHTNESS_LOW -> rbLow.isChecked = true
            BRIGHTNESS_MEDIUM -> rbMedium.isChecked = true
            BRIGHTNESS_HIGH -> rbHigh.isChecked = true
            else -> rbAuto.isChecked = true
        }
    }

    private fun getSelectedBrightness(): Int {
        return when {
            findViewById<MaterialRadioButton>(R.id.rbLow).isChecked -> BRIGHTNESS_LOW
            findViewById<MaterialRadioButton>(R.id.rbMedium).isChecked -> BRIGHTNESS_MEDIUM
            findViewById<MaterialRadioButton>(R.id.rbHigh).isChecked -> BRIGHTNESS_HIGH
            else -> BRIGHTNESS_AUTO
        }
    }

    private fun saveAction(brightness: Int) {
        val params = ActionParams(brightness = brightness)

        lifecycleScope.launch {
            try {
                if (actionId > 0) {
                    val existing = database.actionDao().getActionById(actionId)
                    existing?.let {
                        database.actionDao().update(it.copy(params = params.toJson(), isActive = true))
                    }
                } else {
                    database.actionDao().insert(
                        Action(scenarioId = scenarioId, type = ActionExecutor.ACTION_TYPE_BRIGHTNESS, params = params.toJson())
                    )
                }
                Log.d("BrightnessSetup", "Parlaklık eylemi kaydedildi: $brightness")
                runOnUiThread {
                    Toast.makeText(this@BrightnessActionSetupActivity, "Eylem kaydedildi", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                Log.e("BrightnessSetup", "Kaydetme hatası: ${e.message}", e)
                runOnUiThread {
                    Toast.makeText(this@BrightnessActionSetupActivity, "Hata: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
