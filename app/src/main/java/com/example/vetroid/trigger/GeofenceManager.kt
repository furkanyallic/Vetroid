package com.example.vetroid.trigger

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.vetroid.data.GeofenceParams
import com.example.vetroid.receiver.GeofenceBroadcastReceiver
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices

class GeofenceManager(private val context: Context) {

    private val geofencingClient: GeofencingClient = LocationServices.getGeofencingClient(context)

    private fun getGeofencePendingIntent(): PendingIntent {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }

    fun addGeofence(
        geofenceId: String,
        params: GeofenceParams,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val geofence = Geofence.Builder()
            .setRequestId(geofenceId)
            .setCircularRegion(
                params.latitude,
                params.longitude,
                params.radius
            )
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(params.getTransitionTypeInt())
            .build()

        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        try {
            geofencingClient.addGeofences(geofencingRequest, getGeofencePendingIntent())
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { onFailure(it) }
        } catch (e: SecurityException) {
            onFailure(e)
        }
    }

    fun removeGeofence(
        geofenceId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        geofencingClient.removeGeofences(listOf(geofenceId))
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun removeAllGeofences(
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        geofencingClient.removeGeofences(getGeofencePendingIntent())
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }
}