package com.example.vetroid

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.example.vetroid.data.Action
import com.example.vetroid.data.ActionParams
import com.example.vetroid.data.AppDatabase
import kotlinx.coroutines.launch

class ActionSetupActivity : BaseActivity() {

    private lateinit var database: AppDatabase
    private var scenarioId: Long = 0
    private var actionId: Long = 0

    companion object {
        const val ACTION_TYPE_SILENT = "SILENT_MODE"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_action_setup)

        database = AppDatabase.getDatabase(this)
        scenarioId = intent.getLongExtra("scenario_id", 0)
        actionId = intent.getLongExtra("action_id", 0)
        
        Log.d("ActionSetupActivity", "scenarioId: $scenarioId, actionId: $actionId")

        setupToolbar()
        setupListeners()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupListeners() {
        findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_save).setOnClickListener {
            saveAction()
        }
    }

    private fun saveAction() {
        val mode = if (findViewById<com.google.android.material.radiobutton.MaterialRadioButton>(R.id.rb_silent).isChecked) {
            "SILENT"
        } else {
            "NORMAL"
        }

        val params = ActionParams(mode = mode)

        lifecycleScope.launch {
            try {
                if (actionId > 0) {
                    // UPDATE - mevcut action'ı güncelle
                    val existingAction = database.actionDao().getActionById(actionId)
                    existingAction?.let {
                        val updated = it.copy(
                            params = params.toJson(),
                            isActive = true
                        )
                        database.actionDao().update(updated)
                        Log.d("ActionSetupActivity", "✅ Action güncellendi: $actionId")
                    }
                } else {
                    // INSERT - yeni action oluştur
                    val action = Action(
                        scenarioId = scenarioId,
                        type = ACTION_TYPE_SILENT,
                        params = params.toJson(),
                        executionOrder = 0
                    )
                    actionId = database.actionDao().insert(action)
                    Log.d("ActionSetupActivity", "✅ Yeni Action oluşturuldu: $actionId")
                }

                runOnUiThread {
                    Toast.makeText(this@ActionSetupActivity, "Eylem kaydedildi", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                Log.e("ActionSetupActivity", "❌ Hata: ${e.message}", e)
                runOnUiThread {
                    Toast.makeText(this@ActionSetupActivity, "Hata: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

        }

    }
}

