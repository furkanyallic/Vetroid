package com.example.vetroid.executor

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.util.Log
import com.example.vetroid.data.Action
import com.example.vetroid.data.ActionParams

class ActionExecutor(private val context: Context) {

    companion object {
        const val TAG = "ActionExecutor"
        const val ACTION_TYPE_SILENT = "SILENT_MODE"
    }

    fun executeAction(action: Action) {
        Log.d(TAG, "Action yürütülüyor: ${action.type}")

        when (action.type) {
            ACTION_TYPE_SILENT -> executeSilentMode(action.params)
            // Diğer action tipleri buraya eklenecek
            else -> Log.w(TAG, "Bilinmeyen action tipi: ${action.type}")
        }
    }

    private fun executeSilentMode(paramsJson: String) {
        try {
            val params = ActionParams.fromJson(paramsJson)
            val mode = params.mode

            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

            when (mode) {
                "SILENT" -> {
                    // Sessiz moda geç
                    audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
                    Log.d(TAG, "✅ Telefon sessize alındı")
                    showNotification("Sessize Alındı", "Telefon sessiz moda geçti")
                }
                "NORMAL" -> {
                    // Normal moda geç
                    audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
                    Log.d(TAG, "✅ Telefon sesten çıkarıldı")
                    showNotification("Sesten Çıkarıldı", "Telefon normal moda geçti")
                }
                "VIBRATE" -> {
                    // Titreşim moduna geç
                    audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE
                    Log.d(TAG, "✅ Telefon titreşim moduna alındı")
                    showNotification("Titreşim Modu", "Telefon titreşim moda geçti")
                }
                else -> Log.w(TAG, "Bilinmeyen mod: $mode")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Sessiz mod hatası: ${e.message}", e)
        }
    }

    private fun showNotification(title: String, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Android 8.0+ için kanal oluştur
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "vetroid_actions",
                "Vetroid Eylemleri",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Otomasyon eylem bildirimleri"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = android.app.Notification.Builder(context, "vetroid_actions")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(android.app.Notification.PRIORITY_DEFAULT)
            .build()

        notificationManager.notify(1, notification)
    }
}