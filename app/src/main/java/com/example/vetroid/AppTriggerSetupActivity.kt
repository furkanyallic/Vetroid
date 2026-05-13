package com.example.vetroid

import android.app.AppOpsManager
import android.content.Intent
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.example.vetroid.data.AppDatabase
import com.example.vetroid.data.Trigger
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch
import org.json.JSONObject

class AppTriggerSetupActivity : BaseActivity() {

    companion object {
        const val TRIGGER_TYPE = "APP_USAGE"
    }

    private lateinit var database: AppDatabase
    private var scenarioId: Long = 0
    private var triggerId: Long = 0
    private var selectedPackage: String? = null
    private var selectedAppName: String? = null

    private val appPickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            selectedPackage = result.data?.getStringExtra(AppPickerActivity.EXTRA_PACKAGE_NAME)
            selectedAppName = result.data?.getStringExtra(AppPickerActivity.EXTRA_APP_NAME)
            showSelectedApp()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_trigger_setup)

        database = AppDatabase.getDatabase(this)
        scenarioId = intent.getLongExtra("scenario_id", 0)
        triggerId = intent.getLongExtra("trigger_id", 0)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        checkUsagePermission()

        if (triggerId > 0) loadExistingTrigger()

        findViewById<MaterialButton>(R.id.btnPickApp).setOnClickListener {
            appPickerLauncher.launch(Intent(this, AppPickerActivity::class.java))
        }

        findViewById<MaterialButton>(R.id.btnSave).setOnClickListener {
            saveTrigger()
        }
    }

    private fun checkUsagePermission() {
        if (!hasUsageStatsPermission()) {
            AlertDialog.Builder(this)
                .setTitle("Kullanım Erişimi Gerekli")
                .setMessage(
                    "Hangi uygulamanın açık olduğunu takip etmek için 'Kullanım Erişimi' iznine ihtiyaç var.\n\n" +
                    "Açılan ekranda Vetroid'i bulup etkinleştirin, sonra geri dönün."
                )
                .setPositiveButton("Ayarlara Git") { _, _ ->
                    startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                }
                .setNegativeButton("İptal") { _, _ -> finish() }
                .setCancelable(false)
                .show()
        }
    }

    private fun hasUsageStatsPermission(): Boolean {
        val appOps = getSystemService(APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun loadExistingTrigger() {
        lifecycleScope.launch {
            val trigger = database.triggerDao().getTriggerById(triggerId)
            if (trigger?.type != TRIGGER_TYPE) return@launch
            val json = JSONObject(trigger.params)
            selectedPackage = json.optString("packageName")
            selectedAppName = json.optString("appName")
            if (!selectedPackage.isNullOrEmpty()) showSelectedApp()
        }
    }

    private fun showSelectedApp() {
        val card = findViewById<MaterialCardView>(R.id.cardSelectedApp)
        card.visibility = View.VISIBLE
        findViewById<TextView>(R.id.tvSelectedAppName).text = selectedAppName ?: selectedPackage
        findViewById<TextView>(R.id.tvSelectedPackage).text = selectedPackage
        findViewById<MaterialButton>(R.id.btnSave).isEnabled = true
    }

    private fun saveTrigger() {
        val pkg = selectedPackage ?: return
        val params = JSONObject().apply {
            put("packageName", pkg)
            put("appName", selectedAppName ?: pkg)
        }.toString()

        lifecycleScope.launch {
            if (triggerId > 0) {
                val existing = database.triggerDao().getTriggerById(triggerId)
                existing?.let { database.triggerDao().update(it.copy(params = params, isActive = true)) }
            } else {
                database.triggerDao().insert(
                    Trigger(scenarioId = scenarioId, type = TRIGGER_TYPE, params = params)
                )
            }
            // Servisi başlat / güncelle
            startService(Intent(this@AppTriggerSetupActivity, AppMonitorService::class.java))
            runOnUiThread {
                Toast.makeText(this@AppTriggerSetupActivity, "Uygulama tetikleyicisi kaydedildi", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}
