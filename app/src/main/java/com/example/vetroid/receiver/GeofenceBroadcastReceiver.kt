package com.example.vetroid.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.vetroid.data.AppDatabase
import com.example.vetroid.executor.ActionExecutor
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    companion object {
        const val TAG = "GeofenceReceiver"
        const val ACTION_GEOFENCE_TRIGGER = "com.example.vetroid.GEOFENCE_TRIGGER"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent) ?: return

        if (geofencingEvent.hasError()) {
            val errorMessage = GeofenceStatusCodes.getStatusCodeString(geofencingEvent.errorCode)
            Log.e(TAG, "Geofencing error: $errorMessage")
            return
        }

        val geofenceTransition = geofencingEvent.geofenceTransition
        val triggeringGeofences = geofencingEvent.triggeringGeofences ?: return

        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
            geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

            val transitionType = when (geofenceTransition) {
                Geofence.GEOFENCE_TRANSITION_ENTER -> "ENTER"
                Geofence.GEOFENCE_TRANSITION_EXIT -> "EXIT"
                else -> "UNKNOWN"
            }

            for (geofence in triggeringGeofences) {
                val geofenceId = geofence.requestId
                Log.d(TAG, "Geofence triggered: $geofenceId, transition: $transitionType")

                // Action'ları çalıştır
                executeActions(context, geofenceId, transitionType)
            }
        } else {
            Log.w(TAG, "Invalid geofence transition type: $geofenceTransition")
        }
    }

    private fun executeActions(context: Context, geofenceId: String, transitionType: String) {
        // Geofence ID'den trigger ID'yi çıkar (format: "geofence_1")
        val triggerId = geofenceId.removePrefix("geofence_").toLongOrNull()
        if (triggerId == null) {
            Log.e(TAG, "Geofence ID parse hatası: $geofenceId")
            return
        }

        Log.d(TAG, "Trigger ID: $triggerId bulunuyor...")

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