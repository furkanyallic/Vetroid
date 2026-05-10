package com.example.vetroid.receiver

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.vetroid.data.AppDatabase
import com.example.vetroid.executor.ActionExecutor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TimeTriggerReceiver : BroadcastReceiver() {

    companion object {
        const val TAG = "TimeTriggerReceiver"
        const val ACTION_TIME_TRIGGER = "com.example.vetroid.TIME_TRIGGER"
        const val EXTRA_TRIGGER_ID = "trigger_id"
        const val EXTRA_DAY_OF_WEEK = "day_of_week"
        const val EXTRA_YEAR = "year"
        const val EXTRA_MONTH = "month"
        const val EXTRA_DAY = "day"
        const val DAY_OF_WEEK_ONCE = 0 // Tek seferlik trigger'ı belirtir
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "🔔 TIME TRIGGER RECEIVER ÇALIŞTI - Intent: ${intent.action}")
        val triggerId = intent.getLongExtra(EXTRA_TRIGGER_ID, 0)
        val dayOfWeek = intent.getIntExtra(EXTRA_DAY_OF_WEEK, -1)

        Log.d(TAG, "Time trigger tetiklendi! triggerId: $triggerId, dayOfWeek: $dayOfWeek")

        if (triggerId > 0) {
            executeActionsForTimeTrigger(context, triggerId)

            // Tek seferlik mi? (dayOfWeek == 0)
            if (dayOfWeek == DAY_OF_WEEK_ONCE) {
                Log.d(TAG, "⏰ Tek seferlik alarm tetiklendi - trigger pasifleştirilecek")
                deactivateTrigger(context, triggerId)
            } else if (dayOfWeek > 0) {
                // Tekrarlayan - bir sonraki haftaya yeniden kur
                rescheduleAlarm(context, triggerId, dayOfWeek)
            }
        }
    }

    private fun deactivateTrigger(context: Context, triggerId: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = AppDatabase.getDatabase(context)
                val trigger = database.triggerDao().getTriggerById(triggerId)

                if (trigger != null) {
                    val updated = trigger.copy(isActive = false)
                    database.triggerDao().update(updated)
                    Log.d(TAG, "✅ Trigger pasifleştirildi: $triggerId")
                }

                // Alarm'ı iptal et
                val timeTriggerManager = com.example.vetroid.trigger.TimeTriggerManager(context)
                timeTriggerManager.cancelOneTimeAlarm(triggerId)

            } catch (e: Exception) {
                Log.e(TAG, "Trigger pasifleştirme hatası: ${e.message}", e)
            }
        }
    }

    private fun rescheduleAlarm(context: Context, triggerId: Long, dayOfWeek: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = AppDatabase.getDatabase(context)
                val trigger = database.triggerDao().getTriggerById(triggerId)

                if (trigger == null || !trigger.isActive) {
                    Log.d(TAG, "Trigger aktif değil, alarm yeniden kurulmuyor")
                    return@launch
                }

                val params = com.example.vetroid.data.TimeParams.fromJson(trigger.params)
                val schedule = params.schedule?.find { it.day == dayOfWeek }

                if (schedule != null) {
                    val timeTriggerManager = com.example.vetroid.trigger.TimeTriggerManager(context)
                    timeTriggerManager.scheduleAlarm(triggerId, schedule.day, schedule.hour, schedule.minute)
                    Log.d(TAG, "Alarm bir sonraki haftaya yeniden kuruldu")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Alarm yeniden kurma hatası: ${e.message}", e)
            }
        }
    }

    private fun executeActionsForTimeTrigger(context: Context, triggerId: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = AppDatabase.getDatabase(context)

                // Trigger'ı bul
                val trigger = database.triggerDao().getTriggerById(triggerId)
                if (trigger == null) {
                    Log.e(TAG, "Trigger bulunamadı: $triggerId")
                    return@launch
                }

                // Senaryonun active olup olmadığını kontrol et
                val scenario = database.scenarioDao().getScenarioById(trigger.scenarioId)
                if (scenario == null || !scenario.isActive) {
                    Log.d(TAG, "Senaryo aktif değil veya bulunamadı")
                    return@launch
                }

                Log.d(TAG, "Senaryo bulundu: ${scenario.name}, action'lar aranıyor...")

                // Senaryonun action'larını çek
                val actions = database.actionDao().getActionsByScenarioSync(scenario.id)
                Log.d(TAG, "${actions.size} action bulundu")

                // Action'ları çalıştır
                val executor = ActionExecutor(context)
                for (action in actions) {
                    if (action.isActive) {
                        Log.d(TAG, "Action çalıştırılıyor: ${action.type}")
                        executor.executeAction(action)
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "❌ Action çalıştırma hatası: ${e.message}", e)
            }
        }
    }
}