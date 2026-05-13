package com.example.vetroid.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.vetroid.data.AppDatabase
import com.example.vetroid.executor.ActionExecutor
import com.example.vetroid.executor.ConstraintChecker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

// Pil, şarj ve kulaklık sistem olaylarını dinler.
// Manifest'te kayıtlı olduğundan uygulama kapalıyken de tetiklenir.
class SystemEventReceiver : BroadcastReceiver() {

    companion object {
        const val TAG = "SystemEventReceiver"

        // BatteryTriggerSetupActivity ile ortak sabitler — params JSON'da "eventType" key'i ile saklanır
        const val EVENT_CHARGING_CONNECTED    = "CHARGING_CONNECTED"
        const val EVENT_CHARGING_DISCONNECTED = "CHARGING_DISCONNECTED"
        const val EVENT_BATTERY_LOW           = "BATTERY_LOW"
        const val EVENT_HEADSET_PLUGGED       = "HEADSET_PLUGGED"
        const val EVENT_HEADSET_UNPLUGGED     = "HEADSET_UNPLUGGED"
    }

    override fun onReceive(context: Context, intent: Intent) {
        // Android'in gönderdiği Intent action'ını Vetroid'in iç event tipine çevir
        val eventType = when (intent.action) {
            Intent.ACTION_POWER_CONNECTED    -> EVENT_CHARGING_CONNECTED
            Intent.ACTION_POWER_DISCONNECTED -> EVENT_CHARGING_DISCONNECTED
            Intent.ACTION_BATTERY_LOW        -> EVENT_BATTERY_LOW
            Intent.ACTION_HEADSET_PLUG       -> {
                // Kulaklık intent'i hem takma hem çıkarma için aynı action'ı kullanır.
                // "state" extra'sı: 1 = takıldı, 0 = çıkarıldı
                val state = intent.getIntExtra("state", -1)
                when (state) {
                    1 -> EVENT_HEADSET_PLUGGED
                    0 -> EVENT_HEADSET_UNPLUGGED
                    else -> return  // Bilinmeyen durum, işleme
                }
            }
            else -> return
        }

        Log.d(TAG, "Sistem olayı: $eventType")
        executeMatchingScenarios(context, eventType)
    }

    private fun executeMatchingScenarios(context: Context, eventType: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = AppDatabase.getDatabase(context)

                // Tüm aktif BATTERY tetikleyicilerini çek
                val triggers = database.triggerDao().getActiveTriggersByType("BATTERY")
                Log.d(TAG, "${triggers.size} aktif BATTERY trigger bulundu")

                val executor = ActionExecutor(context)

                for (trigger in triggers) {
                    // Bu trigger'ın eventType'ı gelen olayla eşleşiyor mu?
                    val triggerEventType = JSONObject(trigger.params).optString("eventType")
                    if (triggerEventType != eventType) continue

                    // Senaryonun aktif olup olmadığını kontrol et
                    val scenario = database.scenarioDao().getScenarioById(trigger.scenarioId)
                    if (scenario == null || !scenario.isActive) continue

                    Log.d(TAG, "Senaryo eşleşti: ${scenario.name}, kısıtlamalar kontrol ediliyor")

                    val constraints = database.constraintDao().getConstraintsByScenarioSync(scenario.id)
                    if (!ConstraintChecker(context).checkAll(constraints)) {
                        Log.d(TAG, "Kısıtlama karşılanmadı, senaryo atlandı")
                        continue
                    }

                    val actions = database.actionDao().getActionsByScenarioSync(scenario.id)
                    for (action in actions) {
                        if (action.isActive) executor.executeAction(action)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Senaryo çalıştırma hatası: ${e.message}", e)
            }
        }
    }
}
