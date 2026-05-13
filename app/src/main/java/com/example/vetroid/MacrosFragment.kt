package com.example.vetroid

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.vetroid.data.AppDatabase
import com.example.vetroid.data.ExecutionLog
import com.example.vetroid.databinding.FragmentMacrosBinding
import com.example.vetroid.executor.ActionExecutor
import kotlinx.coroutines.launch

class MacrosFragment : Fragment() {

    private var _binding: FragmentMacrosBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMacrosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val database = AppDatabase.getDatabase(requireContext())

        val adapter = ScenarioAdapter(
            onToggle = { scenario, isActive ->
                lifecycleScope.launch {
                    database.scenarioDao().setActive(scenario.id, isActive)
                }
            },
            onDelete = { scenario ->
                lifecycleScope.launch {
                    database.scenarioDao().delete(scenario)
                }
            },
            onEdit = { scenario ->
                val intent = Intent(requireContext(), AddMacroActivity::class.java).apply {
                    putExtra("scenario_id", scenario.id)
                }
                startActivity(intent)
            },
            onTest = { scenario ->
                lifecycleScope.launch {
                    val actions = database.actionDao().getActionsByScenarioSync(scenario.id)
                    if (actions.isEmpty()) {
                        Toast.makeText(requireContext(), "Bu makroda eylem yok", Toast.LENGTH_SHORT).show()
                        return@launch
                    }
                    val executor = ActionExecutor(requireContext())
                    actions.forEach { executor.executeAction(it) }
                    database.executionLogDao().insert(
                        ExecutionLog(
                            scenarioId = scenario.id,
                            scenarioName = scenario.name,
                            triggerType = "MANUAL",
                            success = true
                        )
                    )
                    Toast.makeText(requireContext(), "\"${scenario.name}\" test edildi", Toast.LENGTH_SHORT).show()
                }
            }
        )

        binding.rvScenarios.layoutManager = LinearLayoutManager(requireContext())
        binding.rvScenarios.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                database.scenarioDao().getAllScenarios().collect { scenarios ->
                    adapter.submitList(scenarios)
                    val isEmpty = scenarios.isEmpty()
                    binding.layoutEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE
                    binding.rvScenarios.visibility = if (isEmpty) View.GONE else View.VISIBLE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
