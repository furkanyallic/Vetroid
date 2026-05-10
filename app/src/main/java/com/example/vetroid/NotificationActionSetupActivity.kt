package com.example.vetroid

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.example.vetroid.data.Action
import com.example.vetroid.data.ActionParams
import com.example.vetroid.data.AppDatabase
import kotlinx.coroutines.launch

class NotificationActionSetupActivity : AppCompatActivity() {

    private lateinit var database: AppDatabase
    private var scenarioId: Long = 0
    private var actionId: Long = 0

    private lateinit var etTitle: com.google.android.material.textfield.TextInputEditText
    private lateinit var etMessage: com.google.android.material.textfield.TextInputEditText
    private lateinit var tvPreviewTitle: android.widget.TextView
    private lateinit var tvPreviewMessage: android.widget.TextView

    companion object {
        private const val TAG = "NotificationSetup"
        const val ACTION_TYPE_NOTIFICATION = "NOTIFICATION"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification_setup)

        database = AppDatabase.getDatabase(this)
        scenarioId = intent.getLongExtra("scenario_id", 0)
        actionId = intent.getLongExtra("action_id", 0)

        Log.d(TAG, "scenarioId: $scenarioId, actionId: $actionId")

        setupToolbar()
        setupViews()
        setupListeners()
        loadExistingAction()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupViews() {
        etTitle = findViewById(R.id.et_title)
        etMessage = findViewById(R.id.et_message)
        tvPreviewTitle = findViewById(R.id.tv_preview_title)
        tvPreviewMessage = findViewById(R.id.tv_preview_message)
    }

    private fun setupListeners() {
        // Mesaj değiştiğinde önizlemeyi güncelle
        etMessage.addTextChangedListener { text ->
            updatePreview()
            validateForm()
        }

        // Başlık değiştiğinde önizlemeyi güncelle
        etTitle.addTextChangedListener { text ->
            updatePreview()
        }

        // Kaydet butonu
        findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_save).setOnClickListener {
            saveAction()
        }
    }

    private fun updatePreview() {
        val title = etTitle.text?.toString()?.trim()
        val message = etMessage.text?.toString()?.trim()

        tvPreviewTitle.text = if (title.isNullOrEmpty()) "Bildirim Başlığı" else title
        tvPreviewMessage.text = if (message.isNullOrEmpty()) "Bildirim mesajınız burada görünecek..." else message
    }

    private fun validateForm(): Boolean {
        val message = etMessage.text?.toString()?.trim()

        if (message.isNullOrEmpty()) {
            findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.til_message).error = "Mesaj girilmesi zorunludur"
            return false
        } else {
            findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.til_message).error = null
            return true
        }
    }

    private fun loadExistingAction() {
        if (actionId > 0) {
            lifecycleScope.launch {
                val action = database.actionDao().getActionById(actionId)
                action?.let {
                    val params = ActionParams.fromJson(it.params)
                    etTitle.setText(params.notificationTitle ?: "")
                    etMessage.setText(params.notificationMessage ?: "")
                    updatePreview()
                    Log.d(TAG, "Mevcut action yüklendi: ${it.id}")
                }
            }
        } else {
            // scenarioId ile mevcut action'ı ara
            lifecycleScope.launch {
                val actions = database.actionDao().getActionsByScenarioSync(scenarioId)
                val existingNotificationAction = actions.lastOrNull { it.type == ACTION_TYPE_NOTIFICATION }
                existingNotificationAction?.let {
                    actionId = it.id
                    val params = ActionParams.fromJson(it.params)
                    etTitle.setText(params.notificationTitle ?: "")
                    etMessage.setText(params.notificationMessage ?: "")
                    updatePreview()
                    Log.d(TAG, "Senaryodaki mevcut action yüklendi: ${it.id}")
                }
            }
        }
    }

    private fun saveAction() {
        if (!validateForm()) {
            return
        }

        val title = etTitle.text?.toString()?.trim()
        val message = etMessage.text?.toString()?.trim()

        val params = ActionParams(
            notificationTitle = title,
            notificationMessage = message
        )

        Log.d(TAG, "Kaydedilecek params: ${params.toJson()}")

        lifecycleScope.launch {
            try {
                if (actionId > 0) {
                    // Güncelle
                    val existingAction = database.actionDao().getActionById(actionId)
                    existingAction?.let {
                        val updated = it.copy(
                            type = ACTION_TYPE_NOTIFICATION,
                            params = params.toJson(),
                            isActive = true
                        )
                        database.actionDao().update(updated)
                        Log.d(TAG, "✅ Bildirim action güncellendi: ${it.id}")
                    }
                } else {
                    // Yeni oluştur
                    val action = Action(
                        scenarioId = scenarioId,
                        type = ACTION_TYPE_NOTIFICATION,
                        params = params.toJson(),
                        executionOrder = 1,
                        isActive = true
                    )
                    actionId = database.actionDao().insert(action)
                    Log.d(TAG, "✅ Yeni bildirim action oluşturuldu: $actionId")
                }

                runOnUiThread {
                    Toast.makeText(this@NotificationActionSetupActivity, "Bildirim eylemi kaydedildi", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Hata: ${e.message}", e)
                runOnUiThread {
                    Toast.makeText(this@NotificationActionSetupActivity, "Hata: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}