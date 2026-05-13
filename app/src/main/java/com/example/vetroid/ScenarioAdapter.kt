package com.example.vetroid

import android.view.LayoutInflater
import android.view.View
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
) : ListAdapter<ScenarioAdapter.ScenarioItem, ScenarioAdapter.ViewHolder>(DiffCallback()) {

    data class ScenarioItem(
        val scenario: Scenario,
        val triggerType: String?,
        val constraintType: String?,
        val actionType: String?
    )

    inner class ViewHolder(private val binding: ItemScenarioBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ScenarioItem) {
            val scenario = item.scenario
            binding.tvName.text = scenario.name
            binding.tvStatus.text = if (scenario.isActive) "Aktif" else "Pasif"

            val triggerLabel = mapTriggerType(item.triggerType)
            val constraintLabel = mapConstraintType(item.constraintType)
            val actionLabel = mapActionType(item.actionType)

            binding.tvTrigger.text = "T: $triggerLabel"
            binding.tvConstraint.text = "K: $constraintLabel"
            binding.tvAction.text = "E: $actionLabel"

            binding.tvTrigger.visibility = if (triggerLabel == null) View.GONE else View.VISIBLE
            binding.tvConstraint.visibility = if (constraintLabel == null) View.GONE else View.VISIBLE
            binding.tvAction.visibility = if (actionLabel == null) View.GONE else View.VISIBLE

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

    class DiffCallback : DiffUtil.ItemCallback<ScenarioItem>() {
        override fun areItemsTheSame(oldItem: ScenarioItem, newItem: ScenarioItem) =
            oldItem.scenario.id == newItem.scenario.id

        override fun areContentsTheSame(oldItem: ScenarioItem, newItem: ScenarioItem) =
            oldItem == newItem
    }

    companion object {
        fun mapTriggerType(type: String?): String? = when (type) {
            "GEOFENCE" -> "Konum"
            "TIME" -> "Zaman"
            "BATTERY" -> "Pil"
            "APP_USAGE" -> "Uygulama"
            "SHAKE" -> "Sallama"
            null -> null
            else -> type
        }

        fun mapConstraintType(type: String?): String? = when (type) {
            "TIME_WINDOW" -> "Zaman Aralığı"
            "WIFI" -> "Wi-Fi"
            "CHARGING" -> "Şarj"
            null -> null
            else -> type
        }

        fun mapActionType(type: String?): String? = when (type) {
            "SILENT_MODE" -> "Ses modu"
            "WIFI" -> "Wi-Fi"
            "BLUETOOTH" -> "Bluetooth"
            "BRIGHTNESS" -> "Parlaklık"
            "APP_LAUNCH" -> "Uygulama aç"
            "NOTIFICATION" -> "Bildirim"
            "SMS_SEND" -> "SMS gönder"
            "SCREENSHOT" -> "Ekran görüntüsü"
            null -> null
            else -> type
        }
    }
}
