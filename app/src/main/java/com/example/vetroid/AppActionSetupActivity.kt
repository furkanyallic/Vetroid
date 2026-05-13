package com.example.vetroid

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import com.example.vetroid.data.Action
import com.example.vetroid.data.ActionParams
import com.example.vetroid.data.AppDatabase
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class AppActionSetupActivity : BaseActivity() {

    private lateinit var database: AppDatabase
    private var scenarioId: Long = 0
    private var actionId: Long = 0
    private var selectedPackageName: String? = null
    private var selectedAppName: String? = null

    private val appPickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data ?: return@registerForActivityResult
            val packageName = data.getStringExtra(AppPickerActivity.EXTRA_PACKAGE_NAME)
            val appName = data.getStringExtra(AppPickerActivity.EXTRA_APP_NAME)

            if (packageName.isNullOrBlank()) {
                Log.w(TAG, "Selected app package is empty")
                return@registerForActivityResult
            }

            selectedPackageName = packageName
            selectedAppName = appName ?: getAppName(packageName)
            showSelectedApp()

            Log.d(TAG, "Selected app: $selectedAppName, package: $selectedPackageName")
        }
    }

    companion object {
        const val ACTION_TYPE_APP_LAUNCH = "APP_LAUNCH"
        private const val TAG = "AppActionSetupActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_action_setup)

        database = AppDatabase.getDatabase(this)
        scenarioId = intent.getLongExtra("scenario_id", 0)
        actionId = intent.getLongExtra("action_id", 0)

        Log.d(TAG, "scenarioId: $scenarioId, actionId: $actionId")

        setupToolbar()
        setupListeners()
        loadExistingAction()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupListeners() {
        findViewById<MaterialButton>(R.id.btn_app_launch).setOnClickListener {
            appPickerLauncher.launch(Intent(this, AppPickerActivity::class.java))
        }

        findViewById<MaterialButton>(R.id.btn_save).setOnClickListener {
            saveAppLaunchAction()
        }
    }

    private fun loadExistingAction() {
        if (actionId <= 0) return

        lifecycleScope.launch {
            val action = database.actionDao().getActionById(actionId)
            if (action?.type != ACTION_TYPE_APP_LAUNCH) return@launch

            val params = ActionParams.fromJson(action.params)
            val packageName = params.packageName ?: return@launch

            selectedPackageName = packageName
            selectedAppName = getAppName(packageName)
            showSelectedApp()
        }
    }

    private fun showSelectedApp() {
        findViewById<ConstraintLayout>(R.id.selected_app_container).visibility = View.VISIBLE
        findViewById<TextView>(R.id.tv_selected_app_name).text = selectedAppName ?: selectedPackageName
        findViewById<TextView>(R.id.tv_selected_package).text = selectedPackageName
        findViewById<MaterialButton>(R.id.btn_save).isEnabled = true
    }

    private fun getAppName(packageName: String): String {
        return try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            packageName
        }
    }

    private fun saveAppLaunchAction() {
        val packageName = selectedPackageName

        if (scenarioId <= 0) {
            Toast.makeText(this, "Senaryo bulunamadi", Toast.LENGTH_SHORT).show()
            Log.w(TAG, "Cannot save app action without scenarioId")
            return
        }

        if (packageName.isNullOrBlank()) {
            Toast.makeText(this, "Lutfen uygulama secin", Toast.LENGTH_SHORT).show()
            Log.w(TAG, "Cannot save app action without selected package")
            return
        }

        val params = ActionParams(packageName = packageName)

        lifecycleScope.launch {
            try {
                val actionToUpdate = if (actionId > 0) {
                    database.actionDao().getActionById(actionId)
                } else {
                    database.actionDao().getActionByScenarioAndType(scenarioId, ACTION_TYPE_APP_LAUNCH)
                }

                if (actionToUpdate != null) {
                    val updated = actionToUpdate.copy(
                        type = ACTION_TYPE_APP_LAUNCH,
                        params = params.toJson(),
                        isActive = true
                    )
                    database.actionDao().update(updated)
                    actionId = updated.id
                    Log.d(TAG, "App launch action updated: ${updated.id}")
                } else {
                    val action = Action(
                        scenarioId = scenarioId,
                        type = ACTION_TYPE_APP_LAUNCH,
                        params = params.toJson(),
                        executionOrder = 1,
                        isActive = true
                    )
                    actionId = database.actionDao().insert(action)
                    Log.d(TAG, "App launch action inserted: $actionId")
                }

                runOnUiThread {
                    Toast.makeText(this@AppActionSetupActivity, "Uygulama acma eylemi kaydedildi", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                Log.e(TAG, "App launch action save error: ${e.message}", e)
                runOnUiThread {
                    Toast.makeText(this@AppActionSetupActivity, "Hata: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
