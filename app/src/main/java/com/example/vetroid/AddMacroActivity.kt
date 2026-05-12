package com.example.vetroid

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.vetroid.data.AppDatabase
import com.example.vetroid.data.Scenario
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class AddMacroActivity : AppCompatActivity() {

    private lateinit var database: AppDatabase
    private lateinit var prefs: SharedPreferences
    private var scenarioId: Long = 0
    private var hasUnsavedChanges = false

    companion object {
        private const val PREFS_NAME = "vetroid_prefs"
        private const val KEY_SCENARIO_ID = "current_scenario_id"
        private const val KEY_MACRO_NAME = "current_macro_name"
        private const val KEY_GEOFENCE_TRIGGER_ID = "current_geofence_trigger_id"
        private const val KEY_TIME_TRIGGER_ID = "current_time_trigger_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_macro)

        database = AppDatabase.getDatabase(this)
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        scenarioId = intent.getLongExtra("scenario_id", 0)
        android.util.Log.d("AddMacroActivity", "scenarioId from intent: $scenarioId")

        val mainLayout = findViewById<android.view.View>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            handleBack()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleBack()
            }
        })

        val etMacroName = findViewById<TextInputEditText>(R.id.etMacroName)

        if (scenarioId > 0) {
            lifecycleScope.launch {
                val scenario = database.scenarioDao().getScenarioById(scenarioId)
                scenario?.let {
                    etMacroName.setText(it.name)
                }
            }
        }

        val btnAddTrigger = findViewById<ImageView>(R.id.btnAddTrigger)
        val btnAddAction = findViewById<ImageView>(R.id.btnAddAction)
        val btnAddConstraint = findViewById<ImageView>(R.id.btnAddConstraint)

        btnAddTrigger.setOnClickListener {
            navigateToActivity(TriggersActivity::class.java, etMacroName, "GEOFENCE")
        }

        btnAddAction.setOnClickListener {
            navigateToActivity(ActionsActivity::class.java, etMacroName)
        }

        btnAddConstraint.setOnClickListener {
            navigateToActivity(ConstraintsActivity::class.java, etMacroName)
        }
    }

    private fun handleBack() {
        val etMacroName = findViewById<TextInputEditText>(R.id.etMacroName)
        val macroName = etMacroName.text?.toString()?.trim()

        if (macroName.isNullOrEmpty()) {
            clearPrefsAndFinish()
            return
        }

        hasUnsavedChanges = true

        AlertDialog.Builder(this)
            .setTitle("Değişiklikleri Kaydet")
            .setMessage("Makroyu kaydetmek ister misiniz?")
            .setPositiveButton("Kaydet") { _, _ ->
                saveScenarioAndFinish(macroName)
            }
            .setNegativeButton("Kaydetme") { _, _ ->
                if (scenarioId == 0L) {
                    clearPrefsAndFinish()
                } else {
                    clearPrefsAndFinish()
                }
            }
            .setNeutralButton("İptal", null)
            .show()
    }

    private fun saveScenarioAndFinish(macroName: String) {
        lifecycleScope.launch {
            try {
                if (scenarioId == 0L) {
                    val scenario = Scenario(
                        name = macroName,
                        isActive = true
                    )
                    scenarioId = database.scenarioDao().insert(scenario)
                    android.util.Log.d("AddMacroActivity", "✅ Yeni Scenario kaydedildi: $scenarioId")
                } else {
                    val existingScenario = database.scenarioDao().getScenarioById(scenarioId)
                    existingScenario?.let {
                        val updated = it.copy(name = macroName)
                        database.scenarioDao().update(updated)
                        android.util.Log.d("AddMacroActivity", "✅ Scenario güncellendi: $scenarioId")
                    }
                }

                clearPrefsAndFinish()
            } catch (e: Exception) {
                android.util.Log.e("AddMacroActivity", "❌ Kaydetme hatası: ${e.message}")
                runOnUiThread {
                    Toast.makeText(this@AddMacroActivity, "Hata: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun clearPrefsAndFinish() {
        prefs.edit()
            .remove(KEY_SCENARIO_ID)
            .remove(KEY_MACRO_NAME)
            .remove(KEY_GEOFENCE_TRIGGER_ID)
            .remove(KEY_TIME_TRIGGER_ID)
            .apply()

        finish()
    }

    private fun navigateToActivity(
        activityClass: Class<*>,
        etMacroName: TextInputEditText,
        triggerType: String? = null
    ) {
        val macroName = etMacroName.text?.toString()?.trim()

        if (macroName.isNullOrEmpty()) {
            Toast.makeText(this, "Lütfen makro ismi girin", Toast.LENGTH_SHORT).show()
            return
        }

        val savedMacroName = prefs.getString(KEY_MACRO_NAME, "")

        if (macroName != savedMacroName) {
            scenarioId = 0
            android.util.Log.d("AddMacroActivity", "📝 İsim değişti, yeni senaryo oluşturulacak")
        }

        lifecycleScope.launch {
            try {
                if (scenarioId == 0L) {
                    val scenario = Scenario(
                        name = macroName,
                        isActive = true
                    )
                    scenarioId = database.scenarioDao().insert(scenario)
                    android.util.Log.d("AddMacroActivity", "✅ Yeni Scenario oluşturuldu: $scenarioId")
                } else {
                    val existingScenario = database.scenarioDao().getScenarioById(scenarioId)
                    existingScenario?.let {
                        val updated = it.copy(name = macroName)
                        database.scenarioDao().update(updated)
                        android.util.Log.d("AddMacroActivity", "✅ Scenario güncellendi: $scenarioId")
                    }
                }

                prefs.edit()
                    .putLong(KEY_SCENARIO_ID, scenarioId)
                    .putString(KEY_MACRO_NAME, macroName)
                    .apply()

                var existingTriggerId = 0L
                if (triggerType != null) {
                    val existingTrigger = database.triggerDao().getTriggerByScenarioAndType(scenarioId, triggerType)
                    existingTriggerId = existingTrigger?.id ?: 0L

                    if (triggerType == "GEOFENCE") {
                        prefs.edit().putLong(KEY_GEOFENCE_TRIGGER_ID, existingTriggerId).apply()
                    } else if (triggerType == "TIME") {
                        prefs.edit().putLong(KEY_TIME_TRIGGER_ID, existingTriggerId).apply()
                    }
                }

                val intent = Intent(this@AddMacroActivity, activityClass)
                intent.putExtra("scenario_id", scenarioId)
                intent.putExtra("trigger_id", existingTriggerId)
                startActivity(intent)
            } catch (e: Exception) {
                android.util.Log.e("AddMacroActivity", "❌ Hata: ${e.message}", e)
                runOnUiThread {
                    Toast.makeText(this@AddMacroActivity, "Hata: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}