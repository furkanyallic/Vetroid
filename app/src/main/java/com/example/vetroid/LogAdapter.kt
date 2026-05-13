package com.example.vetroid

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.vetroid.data.ExecutionLog
import com.example.vetroid.databinding.ItemLogBinding
import java.util.Calendar

class LogAdapter : ListAdapter<ExecutionLog, LogAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(private val binding: ItemLogBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(log: ExecutionLog) {
            binding.tvScenarioName.text = log.scenarioName
            binding.tvTriggerType.text = triggerLabel(log.triggerType)
            binding.tvTime.text = formatTimestamp(log.timestamp)

            if (log.success) {
                binding.viewIndicator.setBackgroundColor(Color.parseColor("#4CAF50"))
                binding.tvStatus.text = "Başarılı"
                binding.tvStatus.setTextColor(Color.parseColor("#4CAF50"))
            } else {
                binding.viewIndicator.setBackgroundColor(Color.parseColor("#FF9800"))
                binding.tvStatus.text = "Atlandı"
                binding.tvStatus.setTextColor(Color.parseColor("#FF9800"))
            }

            if (log.note.isNotEmpty()) {
                binding.tvNote.text = log.note
                binding.tvNote.visibility = android.view.View.VISIBLE
            } else {
                binding.tvNote.visibility = android.view.View.GONE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLogBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<ExecutionLog>() {
        override fun areItemsTheSame(oldItem: ExecutionLog, newItem: ExecutionLog) =
            oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: ExecutionLog, newItem: ExecutionLog) =
            oldItem == newItem
    }

    private fun triggerLabel(type: String) = when (type) {
        "TIME"     -> "Saat Tetikleyici"
        "GEOFENCE" -> "Konum Tetikleyici"
        "BATTERY"  -> "Pil Tetikleyici"
        "SYSTEM"   -> "Sistem Tetikleyici"
        "MANUAL"   -> "Manuel Test"
        else       -> type
    }

    private fun formatTimestamp(ts: Long): String {
        val cal = Calendar.getInstance().apply { timeInMillis = ts }
        val now = Calendar.getInstance()
        val time = "%02d:%02d".format(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))

        val sameDay = cal.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                      cal.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)
        val yesterday = run {
            val y = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
            cal.get(Calendar.YEAR) == y.get(Calendar.YEAR) &&
            cal.get(Calendar.DAY_OF_YEAR) == y.get(Calendar.DAY_OF_YEAR)
        }
        val within7Days = (System.currentTimeMillis() - ts) < 7L * 24 * 60 * 60 * 1000

        return when {
            sameDay    -> "Bugün $time"
            yesterday  -> "Dün $time"
            within7Days -> {
                val days = arrayOf("Paz", "Pzt", "Sal", "Çar", "Per", "Cum", "Cmt")
                "${days[cal.get(Calendar.DAY_OF_WEEK) - 1]} $time"
            }
            else -> {
                val months = arrayOf("Oca","Şub","Mar","Nis","May","Haz","Tem","Ağu","Eyl","Eki","Kas","Ara")
                "${cal.get(Calendar.DAY_OF_MONTH)} ${months[cal.get(Calendar.MONTH)]} $time"
            }
        }
    }
}
