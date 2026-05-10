package com.example.vetroid

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.vetroid.data.AppDatabase
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch

class ActionsActivity : AppCompatActivity() {

    private var scenarioId: Long = 0
    private var silentActionId: Long = 0
    private var appActionId: Long = 0
    private var notificationActionId: Long = 0
    private var smsActionId: Long = 0

    companion object {
        private const val TAG = "ActionsActivity"
        private const val ACTION_TYPE_SILENT = "SILENT_MODE"
        private const val ACTION_TYPE_NOTIFICATION = "NOTIFICATION"
        private const val ACTION_TYPE_SMS_SEND = "SMS_SEND"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_actions)

        scenarioId = intent.getLongExtra("scenario_id", 0)
        Log.d(TAG, "scenarioId: $scenarioId")

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)!!) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        loadLastActions()
    }

    private fun setupListeners() {
        findViewById<MaterialCardView>(R.id.cardSes).setOnClickListener {
            Log.d(TAG, "Silent card clicked, actionId: $silentActionId")
            val intent = Intent(this, ActionSetupActivity::class.java)
            intent.putExtra("scenario_id", scenarioId)
            intent.putExtra("action_id", silentActionId)
            startActivity(intent)
        }

        findViewById<MaterialCardView>(R.id.cardWifi).setOnClickListener { }

        findViewById<MaterialCardView>(R.id.cardUygulama).setOnClickListener {
            Log.d(TAG, "App card clicked, actionId: $appActionId")
            val intent = Intent(this, AppActionSetupActivity::class.java)
            intent.putExtra("scenario_id", scenarioId)
            intent.putExtra("action_id", appActionId)
            startActivity(intent)
        }

        findViewById<MaterialCardView>(R.id.cardBildirim).setOnClickListener {
            Log.d(TAG, "Notification card clicked, actionId: $notificationActionId")
            val intent = Intent(this, NotificationActionSetupActivity::class.java)
            intent.putExtra("scenario_id", scenarioId)
            intent.putExtra("action_id", notificationActionId)
            startActivity(intent)
        }

        findViewById<MaterialCardView>(R.id.cardSms).setOnClickListener {
            Log.d(TAG, "SMS card clicked, actionId: $smsActionId")
            val intent = Intent(this, SmsActionSetupActivity::class.java)
            intent.putExtra("scenario_id", scenarioId)
            intent.putExtra("action_id", smsActionId)
            startActivity(intent)
        }
    }

    private fun loadLastActions() {
        val database = AppDatabase.getDatabase(this)
        lifecycleScope.launch {
            val actions = database.actionDao().getActionsByScenarioSync(scenarioId)
            silentActionId = actions.lastOrNull { it.type == ACTION_TYPE_SILENT }?.id ?: 0
            appActionId = actions.lastOrNull { it.type == AppActionSetupActivity.ACTION_TYPE_APP_LAUNCH }?.id ?: 0
            notificationActionId = actions.lastOrNull { it.type == ACTION_TYPE_NOTIFICATION }?.id ?: 0
            smsActionId = actions.lastOrNull { it.type == ACTION_TYPE_SMS_SEND }?.id ?: 0
            Log.d(TAG, "Last actions - silent: $silentActionId, app: $appActionId, notification: $notificationActionId, sms: $smsActionId")
        }
    }
}