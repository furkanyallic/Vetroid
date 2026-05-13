package com.example.vetroid

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.example.vetroid.data.Action
import com.example.vetroid.data.ActionParams
import com.example.vetroid.data.AppDatabase
import com.example.vetroid.data.Constraint
import com.example.vetroid.data.GeofenceParams
import com.example.vetroid.data.Scenario
import com.example.vetroid.data.TimeParams
import com.example.vetroid.data.Trigger
import com.example.vetroid.executor.ConstraintChecker
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class AddMacroActivity : BaseActivity() {

    private lateinit var database: AppDatabase
    private lateinit var prefs: SharedPreferences
    private var scenarioId: Long = 0
    private var isNewScenario = false

    companion object {
        private const val PREFS_NAME = "vetroid_prefs"
        private const val KEY_SCENARIO_ID = "current_scenario_id"
        private const val KEY_MACRO_NAME = "current_macro_name"
        private const val KEY_GEOFENCE_TRIGGER_ID = "current_geofence_trigger_id"
        private const val KEY_TIME_TRIGGER_ID = "current_time_trigger_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_macro)

        database = AppDatabase.getDatabase(this)
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        // Intent'ten gelen scenario_id düzenleme modunu gösterir
        val intentScenarioId = intent.getLongExtra("scenario_id", 0L)
        isNewScenario = intentScenarioId == 0L
        scenarioId = if (intentScenarioId > 0L) {
            prefs.edit().putLong(KEY_SCENARIO_ID, intentScenarioId).apply()
            intentScenarioId
        } else {
            prefs.getLong(KEY_SCENARIO_ID, 0)
        }

        // Toolbar Kurulumu
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            confirmExit()
        }

        // Donanım/sistem geri tuşunda da onay sor
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                confirmExit()
            }
        })

        // Makro İsmini Al
        val etMacroName = findViewById<TextInputEditText>(R.id.etMacroName)

        // Düzenleme modunda mevcut ismi göster
        if (scenarioId > 0) {
            lifecycleScope.launch {
                val scenario = database.scenarioDao().getScenarioById(scenarioId)
                scenario?.let { etMacroName.setText(it.name) }
            }
        }

        // Kaydet butonu
        val btnSaveMacro = findViewById<MaterialButton>(R.id.btnSaveMacro)
        if (!isNewScenario) btnSaveMacro.text = "Değişiklikleri Kaydet"

        btnSaveMacro.setOnClickListener {
            saveMacroAndExit(etMacroName)
        }

        // Alt ekranlara geçiş butonları
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

    private fun confirmExit() {
        val etMacroName = findViewById<TextInputEditText>(R.id.etMacroName)
        val macroName = etMacroName.text?.toString()?.trim().orEmpty()

        // İsim boşsa direkt çık (yeni senaryo taslağını temizle)
        if (macroName.isEmpty()) {
            discardAndExit()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Değişiklikleri Kaydet")
            .setMessage("Makroyu kaydetmek ister misiniz?")
            .setPositiveButton("Kaydet") { _, _ ->
                saveMacroAndExit(etMacroName)
            }
            .setNegativeButton("Kaydetme") { _, _ ->
                discardAndExit()
            }
            .setNeutralButton("İptal", null)
            .show()
    }

    private fun saveMacroAndExit(etMacroName: TextInputEditText) {
        val macroName = etMacroName.text?.toString()?.trim()
        if (macroName.isNullOrEmpty()) {
            Toast.makeText(this, "Lütfen makro ismi girin", Toast.LENGTH_SHORT).show()
            return
        }
        lifecycleScope.launch {
            val conflict = database.scenarioDao().getScenarioByName(macroName)
            if (conflict != null && conflict.id != scenarioId) {
                runOnUiThread {
                    Toast.makeText(this@AddMacroActivity, "Bu isimde bir makro zaten var", Toast.LENGTH_SHORT).show()
                }
                return@launch
            }
            if (scenarioId == 0L) {
                scenarioId = database.scenarioDao().insert(Scenario(name = macroName, isActive = true))
            } else {
                database.scenarioDao().getScenarioById(scenarioId)?.let {
                    database.scenarioDao().update(it.copy(name = macroName))
                }
            }
            clearPrefs()
            runOnUiThread {
                Toast.makeText(this@AddMacroActivity, "Makro kaydedildi", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun discardAndExit() {
        if (isNewScenario && scenarioId > 0) {
            // Yeni oluşturulan taslağı ve bağlı kayıtları sil
            lifecycleScope.launch {
                database.scenarioDao().getScenarioById(scenarioId)?.let {
                    database.scenarioDao().delete(it)
                }
                clearPrefs()
                runOnUiThread { finish() }
            }
        } else {
            clearPrefs()
            finish()
        }
    }

    private fun clearPrefs() {
        prefs.edit()
            .remove(KEY_SCENARIO_ID)
            .remove(KEY_MACRO_NAME)
            .remove(KEY_GEOFENCE_TRIGGER_ID)
            .remove(KEY_TIME_TRIGGER_ID)
            .apply()
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
                // İsim çakışması kontrolü
                val nameConflict = database.scenarioDao().getScenarioByName(macroName)
                if (nameConflict != null && nameConflict.id != scenarioId) {
                    runOnUiThread {
                        Toast.makeText(this@AddMacroActivity, "Bu isimde bir makro zaten var", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

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

    override fun onResume() {
        super.onResume()
        // Yeni senaryo akışında scenarioId, navigasyon sırasında prefs'e kaydedilir;
        // buradan güncel değeri alıyoruz
        val latestId = prefs.getLong(KEY_SCENARIO_ID, 0)
        if (latestId > 0) scenarioId = latestId

        if (scenarioId > 0) {
            loadTriggersAndActions()
        }
    }

    private fun loadTriggersAndActions() {
        lifecycleScope.launch {
            val triggers = database.triggerDao().getTriggersByScenarioSync(scenarioId)
            val actions = database.actionDao().getActionsByScenarioSync(scenarioId)
            val constraints = database.constraintDao().getConstraintsByScenarioSync(scenarioId)
            runOnUiThread {
                updateTriggerDisplay(triggers)
                updateActionDisplay(actions)
                updateConstraintDisplay(constraints)
            }
        }
    }

    private fun updateTriggerDisplay(triggers: List<Trigger>) {
        val tvNoTriggers = findViewById<TextView>(R.id.tvNoTriggers)
        val llTriggerItems = findViewById<LinearLayout>(R.id.llTriggerItems)

        if (triggers.isEmpty()) {
            tvNoTriggers.visibility = View.VISIBLE
            llTriggerItems.visibility = View.GONE
        } else {
            tvNoTriggers.visibility = View.GONE
            llTriggerItems.visibility = View.VISIBLE
            llTriggerItems.removeAllViews()
            triggers.forEach { trigger ->
                llTriggerItems.addView(buildTriggerRow(trigger))
            }
        }
    }

    private fun buildTriggerRow(trigger: Trigger): android.view.View {
        val px = (resources.displayMetrics.density * 8).toInt()
        val row = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            setPadding(px * 2, px / 2, px, px / 2)
        }

        val label = TextView(this).apply {
            text = "• ${triggerLabel(trigger)}"
            textSize = 14f
            setTextColor(android.graphics.Color.WHITE)
            layoutParams = android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val btnDelete = ImageView(this).apply {
            setImageResource(android.R.drawable.ic_menu_delete)
            setColorFilter(android.graphics.Color.WHITE)
            layoutParams = android.widget.LinearLayout.LayoutParams(px * 3, px * 3)
            setOnClickListener { deleteTrigger(trigger.id) }
        }

        row.addView(label)
        row.addView(btnDelete)
        return row
    }

    private fun deleteTrigger(triggerId: Long) {
        lifecycleScope.launch {
            val trigger = database.triggerDao().getTriggerById(triggerId)
            trigger?.let { database.triggerDao().delete(it) }
            loadTriggersAndActions()
        }
    }

    private fun updateActionDisplay(actions: List<Action>) {
        val tvNoActions = findViewById<TextView>(R.id.tvNoActions)
        val llActionItems = findViewById<LinearLayout>(R.id.llActionItems)

        if (actions.isEmpty()) {
            tvNoActions.visibility = View.VISIBLE
            llActionItems.visibility = View.GONE
        } else {
            tvNoActions.visibility = View.GONE
            llActionItems.visibility = View.VISIBLE
            llActionItems.removeAllViews()
            actions.forEach { action ->
                llActionItems.addView(buildActionRow(action))
            }
        }
    }

    private fun buildActionRow(action: Action): android.view.View {
        val px = (resources.displayMetrics.density * 8).toInt()
        val row = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            setPadding(px * 2, px / 2, px, px / 2)
        }

        val label = TextView(this).apply {
            text = "• ${actionLabel(action)}"
            textSize = 14f
            setTextColor(android.graphics.Color.WHITE)
            layoutParams = android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val btnDelete = ImageView(this).apply {
            setImageResource(android.R.drawable.ic_menu_delete)
            setColorFilter(android.graphics.Color.WHITE)
            layoutParams = android.widget.LinearLayout.LayoutParams(px * 3, px * 3)
            setOnClickListener { deleteAction(action.id) }
        }

        row.addView(label)
        row.addView(btnDelete)
        return row
    }

    private fun deleteAction(actionId: Long) {
        lifecycleScope.launch {
            val action = database.actionDao().getActionById(actionId)
            action?.let { database.actionDao().delete(it) }
            loadTriggersAndActions()
        }
    }

    private fun updateConstraintDisplay(constraints: List<Constraint>) {
        val tvNoConstraints = findViewById<TextView>(R.id.tvNoConstraints)
        val llConstraintItems = findViewById<LinearLayout>(R.id.llConstraintItems)

        if (constraints.isEmpty()) {
            tvNoConstraints.visibility = View.VISIBLE
            llConstraintItems.visibility = View.GONE
        } else {
            tvNoConstraints.visibility = View.GONE
            llConstraintItems.visibility = View.VISIBLE
            llConstraintItems.removeAllViews()
            constraints.forEach { constraint ->
                llConstraintItems.addView(buildConstraintRow(constraint))
            }
        }
    }

    private fun buildConstraintRow(constraint: Constraint): android.view.View {
        val px = (resources.displayMetrics.density * 8).toInt()
        val row = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            setPadding(px * 2, px / 2, px, px / 2)
        }

        val label = TextView(this).apply {
            text = "• ${constraintLabel(constraint)}"
            textSize = 14f
            setTextColor(android.graphics.Color.WHITE)
            layoutParams = android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val btnDelete = ImageView(this).apply {
            setImageResource(android.R.drawable.ic_menu_delete)
            setColorFilter(android.graphics.Color.WHITE)
            layoutParams = android.widget.LinearLayout.LayoutParams(px * 3, px * 3)
            setOnClickListener { deleteConstraint(constraint.id) }
        }

        row.addView(label)
        row.addView(btnDelete)
        return row
    }

    private fun deleteConstraint(constraintId: Long) {
        lifecycleScope.launch {
            val constraint = database.constraintDao().getConstraintById(constraintId)
            constraint?.let { database.constraintDao().delete(it) }
            loadTriggersAndActions()
        }
    }

    private fun constraintLabel(constraint: Constraint): String {
        return when (constraint.type) {
            ConstraintChecker.TYPE_TIME_WINDOW -> try {
                val json = org.json.JSONObject(constraint.params)
                val sh = json.getInt("startHour"); val sm = json.getInt("startMinute")
                val eh = json.getInt("endHour"); val em = json.getInt("endMinute")
                "Saat aralığı: %02d:%02d–%02d:%02d".format(sh, sm, eh, em)
            } catch (e: Exception) { "Saat aralığı kısıtlaması" }

            ConstraintChecker.TYPE_WIFI -> try {
                val required = org.json.JSONObject(constraint.params).getBoolean("requireConnected")
                if (required) "Wi-Fi'ye bağlıyken" else "Wi-Fi'ye bağlı değilken"
            } catch (e: Exception) { "Wi-Fi kısıtlaması" }

            ConstraintChecker.TYPE_CHARGING -> try {
                val required = org.json.JSONObject(constraint.params).getBoolean("requireCharging")
                if (required) "Şarjdayken" else "Şarjda değilken"
            } catch (e: Exception) { "Şarj kısıtlaması" }

            else -> constraint.type
        }
    }

    private fun triggerLabel(trigger: Trigger): String {
        return when (trigger.type) {
            "GEOFENCE" -> try {
                val p = GeofenceParams.fromJson(trigger.params)
                val dir = when (p.transitionType) {
                    "ENTER" -> "Giriş"; "EXIT" -> "Çıkış"; else -> "Giriş/Çıkış"
                }
                "Konum ($dir, ${p.radius.toInt()}m)"
            } catch (e: Exception) { "Konum tetikleyicisi" }

            "TIME" -> try {
                val p = TimeParams.fromJson(trigger.params)
                if (p.type == "RECURRING") "Saat (${p.schedule?.size ?: 0} gün seçildi)"
                else "Tarih/Saat (bir kez)"
            } catch (e: Exception) { "Saat tetikleyicisi" }

            "BATTERY" -> "Pil tetikleyicisi"
            "APP_USAGE" -> try {
                val name = org.json.JSONObject(trigger.params).optString("appName", "")
                "Uygulama açıldı: $name"
            } catch (e: Exception) { "Uygulama tetikleyicisi" }
            else -> trigger.type
        }
    }

    private fun actionLabel(action: Action): String {
        return when (action.type) {
            "SILENT_MODE" -> try {
                val p = ActionParams.fromJson(action.params)
                val m = when (p.mode) { "SILENT" -> "Sessiz"; "NORMAL" -> "Normal"; "VIBRATE" -> "Titreşim"; else -> "" }
                "Ses modu: $m"
            } catch (e: Exception) { "Ses modu değiştir" }

            "WIFI" -> try {
                val p = ActionParams.fromJson(action.params)
                "Wi-Fi: ${if (p.wifiEnabled == true) "Aç" else "Kapat"}"
            } catch (e: Exception) { "Wi-Fi değiştir" }

            "BLUETOOTH" -> try {
                val p = ActionParams.fromJson(action.params)
                "Bluetooth: ${if (p.bluetoothEnabled == true) "Aç" else "Kapat"}"
            } catch (e: Exception) { "Bluetooth değiştir" }

            "BRIGHTNESS" -> try {
                val p = ActionParams.fromJson(action.params)
                val lvl = when (p.brightness) {
                    -1 -> "Otomatik"; 77 -> "Düşük"; 153 -> "Orta"; 255 -> "Yüksek"; else -> "${p.brightness}"
                }
                "Parlaklık: $lvl"
            } catch (e: Exception) { "Parlaklık değiştir" }

            "APP_LAUNCH" -> try {
                val p = ActionParams.fromJson(action.params)
                "Uygulama aç: ${p.packageName ?: ""}"
            } catch (e: Exception) { "Uygulama aç" }

            "NOTIFICATION" -> try {
                val p = ActionParams.fromJson(action.params)
                "Bildirim: ${p.notificationTitle ?: ""}"
            } catch (e: Exception) { "Bildirim göster" }

            "SMS_SEND" -> try {
                val p = ActionParams.fromJson(action.params)
                "SMS: ${p.smsContactName ?: p.smsPhoneNumber ?: ""}"
            } catch (e: Exception) { "SMS gönder" }

            else -> action.type
        }
    }
}
