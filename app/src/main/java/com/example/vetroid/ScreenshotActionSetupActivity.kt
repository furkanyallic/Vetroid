package com.example.vetroid

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.example.vetroid.data.Action
import com.example.vetroid.data.AppDatabase
import com.example.vetroid.executor.ActionExecutor
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class ScreenshotActionSetupActivity : BaseActivity() {

    private lateinit var database: AppDatabase
    private var scenarioId: Long = 0
    private var actionId: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_screenshot_setup)

        database = AppDatabase.getDatabase(this)
        scenarioId = intent.getLongExtra("scenario_id", 0)
        actionId = intent.getLongExtra("action_id", 0)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        findViewById<MaterialButton>(R.id.btnEnableService).setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        findViewById<MaterialButton>(R.id.btnSave).setOnClickListener {
            saveAction()
        }

        updateServiceStatus()
    }

    override fun onResume() {
        super.onResume()
        updateServiceStatus()
    }

    private fun updateServiceStatus() {
        val tv = findViewById<TextView>(R.id.tvServiceStatus)
        val enabled = VetroidAccessibilityService.isEnabled()
        tv.text = if (enabled) "✓ Erişilebilirlik servisi aktif" else "✗ Servis kapalı — aşağıdan etkinleştirin"
        tv.setTextColor(if (enabled) 0xFF4CAF50.toInt() else 0xFFF44336.toInt())
    }

    private fun saveAction() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            Toast.makeText(this, "Bu özellik Android 11+ gerektirir", Toast.LENGTH_LONG).show()
            return
        }
        lifecycleScope.launch {
            try {
                if (actionId > 0) {
                    val existing = database.actionDao().getActionById(actionId)
                    existing?.let {
                        database.actionDao().update(
                            it.copy(type = ActionExecutor.ACTION_TYPE_SCREENSHOT, params = "{}", isActive = true)
                        )
                    }
                } else {
                    actionId = database.actionDao().insert(
                        Action(
                            scenarioId = scenarioId,
                            type = ActionExecutor.ACTION_TYPE_SCREENSHOT,
                            params = "{}",
                            executionOrder = 1,
                            isActive = true
                        )
                    )
                }
                runOnUiThread {
                    Toast.makeText(this@ScreenshotActionSetupActivity,
                        "Ekran görüntüsü eylemi kaydedildi", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@ScreenshotActionSetupActivity,
                        "Hata: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
