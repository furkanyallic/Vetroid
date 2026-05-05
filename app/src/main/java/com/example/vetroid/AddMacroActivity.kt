package com.example.vetroid

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
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

    companion object {
        private const val PREFS_NAME = "vetroid_prefs"
        private const val KEY_SCENARIO_ID = "current_scenario_id"
        private const val KEY_MACRO_NAME = "current_macro_name"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_macro)

        database = AppDatabase.getDatabase(this)
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        // Önceki scenario ID'yi al (varsa)
        scenarioId = prefs.getLong(KEY_SCENARIO_ID, 0)

        // 1. Pencere Ayarları (Padding)
        val mainLayout = findViewById<android.view.View>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 2. Toolbar Kurulumu
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            // Geri tuşuna basınca scenarioId ve macroName temizle
            prefs.edit()
                .remove(KEY_SCENARIO_ID)
                .remove(KEY_MACRO_NAME)
                .apply()
            onBackPressedDispatcher.onBackPressed()
        }

        // 3. Makro İsmini Al
        val etMacroName = findViewById<TextInputEditText>(R.id.etMacroName)

        // Eğer daha önce scenario varsa, ismini göster
        if (scenarioId > 0) {
            lifecycleScope.launch {
                val scenario = database.scenarioDao().getScenarioById(scenarioId)
                scenario?.let {
                    etMacroName.setText(it.name)
                }
            }
        }

        // 4. Buton Tanımlamaları
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

        // Kaydedilmiş makro ismini al
        val savedMacroName = prefs.getString(KEY_MACRO_NAME, "")

        // İsim değişti mi? Değiştiyse YENİ senaryo oluştur
        if (macroName != savedMacroName) {
            scenarioId = 0
            android.util.Log.d("AddMacroActivity", "📝 İsim değişti ($savedMacroName → $macroName), yeni senaryo oluşturulacak")
        }

        lifecycleScope.launch {
            try {
                // Scenario oluştur/güncelle
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

                // Hem scenarioId'yi hem makro ismini kaydet
                prefs.edit()
                    .putLong(KEY_SCENARIO_ID, scenarioId)
                    .putString(KEY_MACRO_NAME, macroName)
                    .apply()

                // Mevcut trigger var mı kontrol et (varsa ID'sini gönder)
                var existingTriggerId = 0L
                if (triggerType != null) {
                    val existingTrigger = database.triggerDao().getTriggerByScenarioAndType(scenarioId, triggerType)
                    existingTriggerId = existingTrigger?.id ?: 0L
                    android.util.Log.d("AddMacroActivity", "✅ Mevcut Trigger ID: $existingTriggerId (Tür: $triggerType)")
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