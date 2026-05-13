package com.example.vetroid

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.example.vetroid.data.AppDatabase
import com.example.vetroid.data.Constraint
import com.example.vetroid.executor.ConstraintChecker
import com.google.android.material.button.MaterialButton
import com.google.android.material.radiobutton.MaterialRadioButton
import kotlinx.coroutines.launch
import org.json.JSONObject

class WifiConstraintSetupActivity : BaseActivity() {

    private var scenarioId: Long = 0
    private var constraintId: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wifi_constraint_setup)

        scenarioId   = intent.getLongExtra("scenario_id", 0)
        constraintId = intent.getLongExtra("constraint_id", 0)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        if (constraintId > 0) {
            lifecycleScope.launch {
                val c = AppDatabase.getDatabase(this@WifiConstraintSetupActivity)
                    .constraintDao().getConstraintById(constraintId)
                c?.let {
                    val requireConnected = JSONObject(it.params).optBoolean("requireConnected", true)
                    if (!requireConnected) {
                        findViewById<MaterialRadioButton>(R.id.rbWifiDisconnected).isChecked = true
                    }
                }
            }
        }

        findViewById<MaterialButton>(R.id.btnSave).setOnClickListener {
            val requireConnected = findViewById<MaterialRadioButton>(R.id.rbWifiConnected).isChecked
            val params = JSONObject().apply { put("requireConnected", requireConnected) }.toString()
            saveConstraint(params)
        }
    }

    private fun saveConstraint(params: String) {
        val db = AppDatabase.getDatabase(this)
        lifecycleScope.launch {
            if (constraintId > 0) {
                val existing = db.constraintDao().getConstraintById(constraintId)
                existing?.let { db.constraintDao().update(it.copy(params = params, isActive = true)) }
            } else {
                db.constraintDao().insert(
                    Constraint(scenarioId = scenarioId, type = ConstraintChecker.TYPE_WIFI, params = params)
                )
            }
            runOnUiThread {
                Toast.makeText(this@WifiConstraintSetupActivity, "Kısıtlama kaydedildi", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}
