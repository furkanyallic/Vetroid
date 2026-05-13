package com.example.vetroid.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.vetroid.data.AppDatabase
import com.example.vetroid.data.TimeParams
import com.example.vetroid.trigger.TimeTriggerManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    companion object {
        const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        Log.d(TAG, "Boot tamamlandı, alarmlar yeniden kuruluyor...")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = AppDatabase.getDatabase(context)
                val triggers = database.triggerDao().getActiveTriggersByType("TIME")
                val manager = TimeTriggerManager(context)

                Log.d(TAG, "${triggers.size} aktif TIME trigger bulundu")

                for (trigger in triggers) {
                    val scenario = database.scenarioDao().getScenarioById(trigger.scenarioId)
                    if (scenario == null || !scenario.isActive) continue

                    val params = TimeParams.fromJson(trigger.params)

                    when (params.type) {
                        "RECURRING" -> {
                            params.schedule?.forEach { schedule ->
                                manager.scheduleAlarm(trigger.id, schedule.day, schedule.hour, schedule.minute)
                                Log.d(TAG, "Recurring alarm yeniden kuruldu: trigger=${trigger.id}, gün=${schedule.day}")
                            }
                        }
                        "ONCE" -> {
                            val year = params.year
                            val month = params.month
                            val day = params.day
                            val hour = params.hour
                            val minute = params.minute
                            if (year != null && month != null && day != null && hour != null && minute != null) {
                                manager.scheduleOneTimeAlarm(trigger.id, year, month, day, hour, minute)
                                Log.d(TAG, "One-time alarm yeniden kuruldu: trigger=${trigger.id}")
                            }
                        }
                    }
                }

                Log.d(TAG, "Tüm alarmlar başarıyla yeniden kuruldu")
            } catch (e: Exception) {
                Log.e(TAG, "Boot sonrası alarm kurma hatası: ${e.message}", e)
            }
        }
    }
}
