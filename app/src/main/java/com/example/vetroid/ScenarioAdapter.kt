package com.example.vetroid

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.vetroid.data.Scenario
import com.example.vetroid.databinding.ItemScenarioBinding

class ScenarioAdapter(
    private val onToggle: (Scenario, Boolean) -> Unit,
    private val onDelete: (Scenario) -> Unit,
    private val onEdit: (Scenario) -> Unit,
    private val onTest: (Scenario) -> Unit
) : ListAdapter<Scenario, ScenarioAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(private val binding: ItemScenarioBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(scenario: Scenario) {
            binding.tvName.text = scenario.name
            binding.tvStatus.text = if (scenario.isActive) "Aktif" else "Pasif"

            // Listener'ı önce kaldır — rebind sırasında sahte tetiklenmeyi önler
            binding.switchActive.setOnCheckedChangeListener(null)
            binding.switchActive.isChecked = scenario.isActive
            binding.switchActive.setOnCheckedChangeListener { _, isChecked ->
                onToggle(scenario, isChecked)
            }

            binding.btnDelete.setOnClickListener {
                onDelete(scenario)
            }

            binding.btnTest.setOnClickListener {
                onTest(scenario)
            }

            // Kartın tamamına dokunmak düzenleme ekranını açar
            binding.root.setOnClickListener {
                onEdit(scenario)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemScenarioBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<Scenario>() {
        override fun areItemsTheSame(oldItem: Scenario, newItem: Scenario) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Scenario, newItem: Scenario) =
            oldItem == newItem
    }
}
