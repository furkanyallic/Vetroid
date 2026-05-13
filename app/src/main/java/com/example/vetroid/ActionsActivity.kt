package com.example.vetroid

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.example.vetroid.data.AppDatabase
import com.example.vetroid.executor.ActionExecutor
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch

class ActionsActivity : BaseActivity() {

    private var scenarioId: Long = 0
    private var silentActionId: Long = 0
    private var wifiActionId: Long = 0
    private var bluetoothActionId: Long = 0
    private var brightnessActionId: Long = 0
    private var appActionId: Long = 0
    private var notificationActionId: Long = 0
    private var smsActionId: Long = 0
    private var screenshotActionId: Long = 0

    companion object {
        private const val TAG = "ActionsActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_actions)

        scenarioId = intent.getLongExtra("scenario_id", 0)
        Log.d(TAG, "scenarioId: $scenarioId")

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        loadLastActions()
    }

    private fun setupListeners() {
        findViewById<MaterialCardView>(R.id.cardSes).setOnClickListener {
            startSetup(ActionSetupActivity::class.java, silentActionId)
        }

        findViewById<MaterialCardView>(R.id.cardWifi).setOnClickListener {
            val intent = Intent(this, WifiBluetoothSetupActivity::class.java).apply {
                putExtra("scenario_id", scenarioId)
                putExtra("action_id", wifiActionId)
                putExtra("action_type", ActionExecutor.ACTION_TYPE_WIFI)
            }
            startActivity(intent)
        }

        findViewById<MaterialCardView>(R.id.cardBluetooth).setOnClickListener {
            val intent = Intent(this, WifiBluetoothSetupActivity::class.java).apply {
                putExtra("scenario_id", scenarioId)
                putExtra("action_id", bluetoothActionId)
                putExtra("action_type", ActionExecutor.ACTION_TYPE_BLUETOOTH)
            }
            startActivity(intent)
        }

        findViewById<MaterialCardView>(R.id.cardParlaklik).setOnClickListener {
            val intent = Intent(this, BrightnessActionSetupActivity::class.java).apply {
                putExtra("scenario_id", scenarioId)
                putExtra("action_id", brightnessActionId)
            }
            startActivity(intent)
        }

        findViewById<MaterialCardView>(R.id.cardUygulama).setOnClickListener {
            val intent = Intent(this, AppActionSetupActivity::class.java).apply {
                putExtra("scenario_id", scenarioId)
                putExtra("action_id", appActionId)
            }
            startActivity(intent)
        }

        findViewById<MaterialCardView>(R.id.cardBildirim).setOnClickListener {
            val intent = Intent(this, NotificationActionSetupActivity::class.java).apply {
                putExtra("scenario_id", scenarioId)
                putExtra("action_id", notificationActionId)
            }
            startActivity(intent)
        }

        findViewById<MaterialCardView>(R.id.cardSms).setOnClickListener {
            val intent = Intent(this, SmsActionSetupActivity::class.java).apply {
                putExtra("scenario_id", scenarioId)
                putExtra("action_id", smsActionId)
            }
            startActivity(intent)
        }

        findViewById<MaterialCardView>(R.id.cardEkranGoruntus).setOnClickListener {
            val intent = Intent(this, ScreenshotActionSetupActivity::class.java).apply {
                putExtra("scenario_id", scenarioId)
                putExtra("action_id", screenshotActionId)
            }
            startActivity(intent)
        }
    }

    private fun startSetup(activityClass: Class<*>, actionId: Long) {
        val intent = Intent(this, activityClass).apply {
            putExtra("scenario_id", scenarioId)
            putExtra("action_id", actionId)
        }
        startActivity(intent)
    }

    private fun loadLastActions() {
        val database = AppDatabase.getDatabase(this)
        lifecycleScope.launch {
            val actions = database.actionDao().getActionsByScenarioSync(scenarioId)
            silentActionId = actions.lastOrNull { it.type == ActionExecutor.ACTION_TYPE_SILENT }?.id ?: 0
            wifiActionId = actions.lastOrNull { it.type == ActionExecutor.ACTION_TYPE_WIFI }?.id ?: 0
            bluetoothActionId = actions.lastOrNull { it.type == ActionExecutor.ACTION_TYPE_BLUETOOTH }?.id ?: 0
            brightnessActionId = actions.lastOrNull { it.type == ActionExecutor.ACTION_TYPE_BRIGHTNESS }?.id ?: 0
            appActionId = actions.lastOrNull { it.type == AppActionSetupActivity.ACTION_TYPE_APP_LAUNCH }?.id ?: 0
            notificationActionId = actions.lastOrNull { it.type == ActionExecutor.ACTION_TYPE_NOTIFICATION }?.id ?: 0
            smsActionId = actions.lastOrNull { it.type == ActionExecutor.ACTION_TYPE_SMS_SEND }?.id ?: 0
            screenshotActionId = actions.lastOrNull { it.type == ActionExecutor.ACTION_TYPE_SCREENSHOT }?.id ?: 0
            Log.d(TAG, "Actions yüklendi — silent:$silentActionId wifi:$wifiActionId bt:$bluetoothActionId brightness:$brightnessActionId")
        }
    }
}
