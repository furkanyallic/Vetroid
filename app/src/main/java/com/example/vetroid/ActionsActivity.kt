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
    private var actionId: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_actions)

        scenarioId = intent.getLongExtra("scenario_id", 0)
        Log.d("ActionsActivity", "scenarioId: $scenarioId")

        // Bu senaryoya ait en son SILENT_MODE action'ı bul
        loadLastAction()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)!!) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Ses kartı - Sessiz Mod
        findViewById<MaterialCardView>(R.id.cardSes).setOnClickListener {
            Log.d("ActionsActivity", "Ses kartı tıklandı, actionId: $actionId")
            val intent = Intent(this, ActionSetupActivity::class.java)
            intent.putExtra("scenario_id", scenarioId)
            intent.putExtra("action_id", actionId)
            startActivity(intent)
        }

        // Diğer kartlar (ileride doldurulacak)
        findViewById<MaterialCardView>(R.id.cardWifi).setOnClickListener { }
        findViewById<MaterialCardView>(R.id.cardUygulama).setOnClickListener { }
        findViewById<MaterialCardView>(R.id.cardBildirim).setOnClickListener { }
    }

    private fun loadLastAction() {
        val database = AppDatabase.getDatabase(this)
        lifecycleScope.launch {
            val actions = database.actionDao().getActionsByScenarioSync(scenarioId)
            val lastSilentAction = actions.lastOrNull { it.type == "SILENT_MODE" }
            actionId = lastSilentAction?.id ?: 0
            Log.d("ActionsActivity", "En son SILENT actionId: $actionId")
        }


    }
}