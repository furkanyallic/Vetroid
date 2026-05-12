package com.example.vetroid

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.vetroid.data.Scenario
import com.google.android.material.switchmaterial.SwitchMaterial

class MacroAdapter(
    private val onToggle: (Scenario, Boolean) -> Unit,
    private val onDelete: (Scenario) -> Unit,
    private val onClick: (Scenario) -> Unit
) : RecyclerView.Adapter<MacroAdapter.ViewHolder>() {

    private var items: List<MacroItem> = emptyList()

    data class MacroItem(
        val scenario: Scenario,
        val triggerType: String?,
        val constraintType: String?,
        val actionType: String?
    )

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivDelete: ImageView = view.findViewById(R.id.iv_delete)
        val tvName: TextView = view.findViewById(R.id.tv_name)
        val tvTrigger: TextView = view.findViewById(R.id.tv_trigger)
        val tvConstraint: TextView = view.findViewById(R.id.tv_constraint)
        val tvAction: TextView = view.findViewById(R.id.tv_action)
        val switchActive: SwitchMaterial = view.findViewById(R.id.switch_active)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_macro, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.tvName.text = item.scenario.name
        holder.tvTrigger.text = "T: ${item.triggerType ?: "Yok"}"
        holder.tvConstraint.text = "K: ${item.constraintType ?: "Yok"}"
        holder.tvAction.text = "E: ${item.actionType ?: "Yok"}"

        holder.switchActive.isChecked = item.scenario.isActive

        holder.switchActive.setOnCheckedChangeListener { _, isChecked ->
            onToggle(item.scenario, isChecked)
        }

        holder.ivDelete.setOnClickListener {
            onDelete(item.scenario)
        }

        holder.itemView.setOnClickListener {
            onClick(item.scenario)
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateList(newItems: List<MacroItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    companion object {
        fun mapTriggerType(type: String?): String {
            return when (type) {
                "TIME" -> "Zaman"
                "GEOFENCE" -> "Konum"
                else -> "Yok"
            }
        }

        fun mapActionType(type: String?): String {
            return when (type) {
                "SILENT_MODE" -> "Sessize Al"
                "SMS_SEND" -> "SMS Gönder"
                "NOTIFICATION" -> "Bildirim Göster"
                "APP_LAUNCH" -> "Uygulama Aç"
                "APP_CLOSE" -> "Uygulama Kapat"
                else -> "Yok"
            }
        }
    }
}