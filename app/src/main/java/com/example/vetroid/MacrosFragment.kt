package com.example.vetroid

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vetroid.data.AppDatabase
import com.example.vetroid.data.Scenario
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MacrosFragment : Fragment() {

    private lateinit var database: AppDatabase
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MacroAdapter

    companion object {
        private const val TAG = "MacrosFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_macros, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = AppDatabase.getDatabase(requireContext())

        setupRecyclerView()
        observeScenarios()
    }

    private fun setupRecyclerView() {
        recyclerView = view?.findViewById(R.id.rv_macros) ?: return
        adapter = MacroAdapter(
            onToggle = { scenario, isActive ->
                toggleScenarioActive(scenario, isActive)
            },
            onDelete = { scenario ->
                showDeleteConfirmation(scenario)
            },
            onClick = { scenario ->
                openScenarioForEdit(scenario)
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
    }

    private fun observeScenarios() {
        viewLifecycleOwner.lifecycleScope.launch {
            database.scenarioDao().getAllScenarios().collectLatest { scenarios ->
                Log.d(TAG, "Senaryo sayısı: ${scenarios.size}")
                loadMacroItems(scenarios)
            }
        }
    }

    private suspend fun loadMacroItems(scenarios: List<Scenario>) {
        val items = scenarios.map { scenario ->
            val triggers = database.triggerDao().getTriggersByScenarioSync(scenario.id)
            val actions = database.actionDao().getActionsByScenarioSync(scenario.id)

            val triggerType = triggers.lastOrNull()?.type
            val actionType = actions.lastOrNull()?.type

            MacroAdapter.MacroItem(
                scenario = scenario,
                triggerType = MacroAdapter.mapTriggerType(triggerType),
                constraintType = null,
                actionType = MacroAdapter.mapActionType(actionType)
            )
        }

        activity?.runOnUiThread {
            adapter.updateList(items)
        }
    }

    private fun toggleScenarioActive(scenario: Scenario, isActive: Boolean) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val updated = scenario.copy(isActive = isActive)
                database.scenarioDao().update(updated)
                Log.d(TAG, "Scenario ${scenario.id} active: $isActive")
            } catch (e: Exception) {
                Log.e(TAG, "Toggle error: ${e.message}")
            }
        }
    }

    private fun showDeleteConfirmation(scenario: Scenario) {
        AlertDialog.Builder(requireContext())
            .setTitle("Makroyu Sil")
            .setMessage("${scenario.name} silinecek. Emin misiniz?")
            .setPositiveButton("Sil") { _, _ ->
                deleteScenario(scenario)
            }
            .setNegativeButton("İptal", null)
            .show()
    }

    private fun deleteScenario(scenario: Scenario) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                database.scenarioDao().delete(scenario)
                Log.d(TAG, "Scenario silindi: ${scenario.id}")
            } catch (e: Exception) {
                Log.e(TAG, "Silme hatası: ${e.message}")
            }
        }
    }

    private fun openScenarioForEdit(scenario: Scenario) {
        val intent = Intent(requireContext(), AddMacroActivity::class.java)
        intent.putExtra("scenario_id", scenario.id)
        startActivity(intent)
    }
}