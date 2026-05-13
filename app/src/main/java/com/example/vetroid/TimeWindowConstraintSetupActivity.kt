package com.example.vetroid

import android.os.Bundle
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.example.vetroid.data.AppDatabase
import com.example.vetroid.data.Constraint
import com.example.vetroid.executor.ConstraintChecker
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import org.json.JSONObject

class TimeWindowConstraintSetupActivity : BaseActivity() {

    private var scenarioId: Long = 0
    private var constraintId: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_time_window_constraint_setup)

        scenarioId   = intent.getLongExtra("scenario_id", 0)
        constraintId = intent.getLongExtra("constraint_id", 0)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        val pickerStart = findViewById<TimePicker>(R.id.timePickerStart)
        val pickerEnd   = findViewById<TimePicker>(R.id.timePickerEnd)
        pickerStart.setIs24HourView(true)
        pickerEnd.setIs24HourView(true)

        // Kaydedilmiş değeri yükle
        if (constraintId > 0) {
            lifecycleScope.launch {
                val c = AppDatabase.getDatabase(this@TimeWindowConstraintSetupActivity)
                    .constraintDao().getConstraintById(constraintId)
                c?.let {
                    val p = JSONObject(it.params)
                    pickerStart.hour   = p.getInt("startHour")
                    pickerStart.minute = p.getInt("startMinute")
                    pickerEnd.hour     = p.getInt("endHour")
                    pickerEnd.minute   = p.getInt("endMinute")
                }
            }
        }

        findViewById<MaterialButton>(R.id.btnSave).setOnClickListener {
            val params = JSONObject().apply {
                put("startHour",   pickerStart.hour)
                put("startMinute", pickerStart.minute)
                put("endHour",     pickerEnd.hour)
                put("endMinute",   pickerEnd.minute)
            }.toString()
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
                    Constraint(scenarioId = scenarioId, type = ConstraintChecker.TYPE_TIME_WINDOW, params = params)
                )
            }
            runOnUiThread {
                Toast.makeText(this@TimeWindowConstraintSetupActivity, "Kısıtlama kaydedildi", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}
