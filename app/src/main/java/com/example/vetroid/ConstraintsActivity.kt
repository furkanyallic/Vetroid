package com.example.vetroid

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.example.vetroid.data.AppDatabase
import com.example.vetroid.executor.ConstraintChecker
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch

class ConstraintsActivity : BaseActivity() {

    private var scenarioId: Long = 0
    private var timeWindowConstraintId: Long = 0
    private var wifiConstraintId: Long = 0
    private var chargingConstraintId: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_constraints)

        scenarioId = intent.getLongExtra("scenario_id", 0)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        loadLastConstraints()

        findViewById<MaterialCardView>(R.id.cardSaatAraligi).setOnClickListener {
            startActivity(Intent(this, TimeWindowConstraintSetupActivity::class.java).apply {
                putExtra("scenario_id", scenarioId)
                putExtra("constraint_id", timeWindowConstraintId)
            })
        }

        findViewById<MaterialCardView>(R.id.cardWifi).setOnClickListener {
            startActivity(Intent(this, WifiConstraintSetupActivity::class.java).apply {
                putExtra("scenario_id", scenarioId)
                putExtra("constraint_id", wifiConstraintId)
            })
        }

        findViewById<MaterialCardView>(R.id.cardSarj).setOnClickListener {
            startActivity(Intent(this, ChargingConstraintSetupActivity::class.java).apply {
                putExtra("scenario_id", scenarioId)
                putExtra("constraint_id", chargingConstraintId)
            })
        }
    }

    override fun onResume() {
        super.onResume()
        loadLastConstraints()
    }

    private fun loadLastConstraints() {
        lifecycleScope.launch {
            val constraints = AppDatabase.getDatabase(this@ConstraintsActivity)
                .constraintDao().getConstraintsByScenarioSync(scenarioId)
            timeWindowConstraintId = constraints.lastOrNull { it.type == ConstraintChecker.TYPE_TIME_WINDOW }?.id ?: 0
            wifiConstraintId       = constraints.lastOrNull { it.type == ConstraintChecker.TYPE_WIFI }?.id ?: 0
            chargingConstraintId   = constraints.lastOrNull { it.type == ConstraintChecker.TYPE_CHARGING }?.id ?: 0
            Log.d("ConstraintsActivity", "timeWindow:$timeWindowConstraintId wifi:$wifiConstraintId charging:$chargingConstraintId")
            runOnUiThread { updateCardStates() }
        }
    }

    private fun updateCardStates() {
        val savedColor  = Color.parseColor("#C8E6C9") // açık yeşil — kayıtlı
        val defaultColor = Color.parseColor("#E8E8E8") // gri — boş

        findViewById<MaterialCardView>(R.id.cardSaatAraligi)
            .setCardBackgroundColor(if (timeWindowConstraintId > 0) savedColor else defaultColor)
        findViewById<MaterialCardView>(R.id.cardWifi)
            .setCardBackgroundColor(if (wifiConstraintId > 0) savedColor else defaultColor)
        findViewById<MaterialCardView>(R.id.cardSarj)
            .setCardBackgroundColor(if (chargingConstraintId > 0) savedColor else defaultColor)
    }
}
