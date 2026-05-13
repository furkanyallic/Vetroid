package com.example.vetroid

import android.Manifest
import android.content.ContentResolver
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.vetroid.data.Action
import com.example.vetroid.data.ActionParams
import com.example.vetroid.data.AppDatabase
import kotlinx.coroutines.launch

class SmsActionSetupActivity : BaseActivity() {

    private lateinit var database: AppDatabase
    private var scenarioId: Long = 0
    private var actionId: Long = 0

    private var selectedPhoneNumber: String? = null
    private var selectedContactName: String? = null

    companion object {
        private const val TAG = "SmsActionSetup"
        const val ACTION_TYPE_SMS_SEND = "SMS_SEND"
    }

    private val contactPickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { contactUri ->
                val phoneNumber = getPhoneNumberFromUri(contactUri)
                val contactName = getContactNameFromUri(contactUri)

                if (phoneNumber != null) {
                    selectedPhoneNumber = phoneNumber
                    selectedContactName = contactName
                    updateContactDisplay(contactName)
                    Log.d(TAG, "Kişi seçildi: $contactName - $phoneNumber")
                } else {
                    Toast.makeText(this, "Bu kişinin telefon numarası yok", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openContactPicker()
        } else {
            Toast.makeText(this, "Kişi erişim izni gerekli", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sms_setup)

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
        // Kişi seç butonu
        findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_select_contact).setOnClickListener {
            checkContactsPermissionAndOpenPicker()
        }

        // Kaydet butonu
        findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_save).setOnClickListener {
            saveAction()
        }

        // Mesaj değişikliğinde önizleme güncelle
        findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_message).setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                updatePreview()
            }
        }
    }

    private fun setupListeners() {
        findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_message).addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updatePreview()
            }
        })
    }

    private fun checkContactsPermissionAndOpenPicker() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED -> {
                openContactPicker()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS) -> {
                Toast.makeText(this, "Kişi seçmek için izin gerekli", Toast.LENGTH_LONG).show()
                permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
            }
            else -> {
                permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
            }
        }
    }

    private fun openContactPicker() {
        val intent = android.content.Intent(android.content.Intent.ACTION_PICK).apply {
            type = android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE
        }
        contactPickerLauncher.launch(intent)
    }

    private fun getPhoneNumberFromUri(contactUri: Uri): String? {
        val contentResolver: ContentResolver = contentResolver

        val projection = arrayOf(
            android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER,
            android.provider.ContactsContract.CommonDataKinds.Phone.HAS_PHONE_NUMBER
        )

        var cursor: Cursor? = null
        try {
            cursor = contentResolver.query(contactUri, projection, null, null, null)

            if (cursor != null && cursor.moveToFirst()) {
                val hasPhone = cursor.getInt(cursor.getColumnIndexOrThrow(android.provider.ContactsContract.CommonDataKinds.Phone.HAS_PHONE_NUMBER))
                if (hasPhone > 0) {
                    val phoneNumber = cursor.getString(cursor.getColumnIndexOrThrow(android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER))
                    return phoneNumber?.replace(" ", "")?.replace("-", "")?.replace("(", "")?.replace(")", "")
                }
            }
        } finally {
            cursor?.close()
        }

        // Alternatif yöntem - doğrudan Phone.CONTENT_TYPE ile
        val phoneUri = android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        cursor = contentResolver.query(
            contactUri,
            arrayOf(android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER),
            null,
            null,
            null
        )

        return cursor?.use {
            if (it.moveToFirst()) {
                it.getString(it.getColumnIndexOrThrow(android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER))?.replace(" ", "")
            } else null
        }
    }

    private fun getContactNameFromUri(contactUri: Uri): String? {
        val contentResolver: ContentResolver = contentResolver

        var cursor: Cursor? = null
        try {
            cursor = contentResolver.query(
                contactUri,
                arrayOf(android.provider.ContactsContract.Contacts.DISPLAY_NAME),
                null,
                null,
                null
            )

            return cursor?.use {
                if (it.moveToFirst()) {
                    it.getString(it.getColumnIndexOrThrow(android.provider.ContactsContract.Contacts.DISPLAY_NAME))
                } else null
            }
        } finally {
            cursor?.close()
        }
    }

    private fun updateContactDisplay(name: String?) {
        findViewById<com.google.android.material.textview.MaterialTextView>(R.id.tv_selected_contact).text =
            name ?: "Kişi seçilmedi"
        findViewById<com.google.android.material.textview.MaterialTextView>(R.id.tv_selected_contact).setTextColor(
            if (name != null) ContextCompat.getColor(this, R.color.black)
            else ContextCompat.getColor(this, R.color.gray)
        )
    }

    private fun updatePreview() {
        val message = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_message).text?.toString() ?: ""
        findViewById<android.widget.TextView>(R.id.tv_preview_message).text =
            if (message.isEmpty()) "SMS mesajınız burada görünecek..." else message
    }

    private fun loadExistingAction() {
        if (actionId > 0) {
            lifecycleScope.launch {
                val action = database.actionDao().getActionById(actionId)
                action?.let {
                    val params = ActionParams.fromJson(it.params)
                    params.smsContactName?.let { name ->
                        selectedContactName = name
                        updateContactDisplay(name)
                    }
                    params.smsMessage?.let { msg ->
                        findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_message).setText(msg)
                    }
                    updatePreview()
                    Log.d(TAG, "Mevcut SMS action yüklendi: ${it.id}")
                }
            }
        } else {
            lifecycleScope.launch {
                val actions = database.actionDao().getActionsByScenarioSync(scenarioId)
                val existingSmsAction = actions.lastOrNull { it.type == ACTION_TYPE_SMS_SEND }
                existingSmsAction?.let {
                    actionId = it.id
                    val params = ActionParams.fromJson(it.params)
                    params.smsContactName?.let { name ->
                        selectedContactName = name
                        updateContactDisplay(name)
                    }
                    params.smsMessage?.let { msg ->
                        findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_message).setText(msg)
                    }
                    updatePreview()
                    Log.d(TAG, "Senaryodaki mevcut SMS action yüklendi: ${it.id}")
                }
            }
        }
    }

    private fun validateForm(): Boolean {
        var isValid = true

        // Kişi seçili mi?
        if (selectedPhoneNumber == null) {
            Toast.makeText(this, "Lütfen bir kişi seçin", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        // Mesaj girildi mi?
        val message = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_message).text?.toString()?.trim()
        if (message.isNullOrEmpty()) {
            findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.til_message).error = "Mesaj girilmesi zorunludur"
            isValid = false
        } else {
            findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.til_message).error = null
        }

        return isValid
    }

    private fun saveAction() {
        if (!validateForm()) {
            return
        }

        val message = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_message).text?.toString()?.trim()

        val params = ActionParams(
            smsPhoneNumber = selectedPhoneNumber,
            smsContactName = selectedContactName,
            smsMessage = message
        )

        Log.d(TAG, "Kaydedilecek params: ${params.toJson()}")

        lifecycleScope.launch {
            try {
                if (actionId > 0) {
                    val existingAction = database.actionDao().getActionById(actionId)
                    existingAction?.let {
                        val updated = it.copy(
                            type = ACTION_TYPE_SMS_SEND,
                            params = params.toJson(),
                            isActive = true
                        )
                        database.actionDao().update(updated)
                        Log.d(TAG, "✅ SMS action güncellendi: ${it.id}")
                    }
                } else {
                    val action = Action(
                        scenarioId = scenarioId,
                        type = ACTION_TYPE_SMS_SEND,
                        params = params.toJson(),
                        executionOrder = 1,
                        isActive = true
                    )
                    actionId = database.actionDao().insert(action)
                    Log.d(TAG, "✅ Yeni SMS action oluşturuldu: $actionId")
                }

                runOnUiThread {
                    Toast.makeText(this@SmsActionSetupActivity, "SMS eylemi kaydedildi", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Hata: ${e.message}", e)
                runOnUiThread {
                    Toast.makeText(this@SmsActionSetupActivity, "Hata: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}