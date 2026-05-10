package com.example.vetroid.executor

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.util.Log
import com.example.vetroid.data.Action
import com.example.vetroid.data.ActionParams

class ActionExecutor(private val context: Context) {

    companion object {
        const val TAG = "ActionExecutor"
        const val ACTION_TYPE_SILENT = "SILENT_MODE"
        const val ACTION_TYPE_APP_LAUNCH = "APP_LAUNCH"
        const val ACTION_TYPE_APP_CLOSE = "APP_CLOSE"
    }

    fun executeAction(action: Action) {
        Log.d(TAG, "Executing action: ${action.type}")

        when (action.type) {
            ACTION_TYPE_SILENT -> executeSilentMode(action.params)
            ACTION_TYPE_APP_LAUNCH -> executeAppLaunch(action.params)
            ACTION_TYPE_APP_CLOSE -> executeAppClose(action.params)
            else -> Log.w(TAG, "Unknown action type: ${action.type}")
        }
    }

    private fun executeSilentMode(paramsJson: String) {
        try {
            val params = ActionParams.fromJson(paramsJson)
            val mode = params.mode

            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

            when (mode) {
                "SILENT" -> {
                    audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
                    Log.d(TAG, "Phone switched to silent mode")
                    showNotification("Sessize Alindi", "Telefon sessiz moda gecti")
                }
                "NORMAL" -> {
                    audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
                    Log.d(TAG, "Phone switched to normal mode")
                    showNotification("Ses Acildi", "Telefon normal moda gecti")
                }
                "VIBRATE" -> {
                    audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE
                    Log.d(TAG, "Phone switched to vibrate mode")
                    showNotification("Titresim Modu", "Telefon titresim moduna gecti")
                }
                else -> Log.w(TAG, "Unknown ringer mode: $mode")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Silent mode error: ${e.message}", e)
        }
    }

    private fun executeAppLaunch(paramsJson: String) {
        try {
            val params = ActionParams.fromJson(paramsJson)
            val packageName = params.packageName

            if (packageName.isNullOrEmpty()) {
                Log.e(TAG, "Package name is empty, app cannot be launched")
                return
            }

            val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                Log.d(TAG, "App launched: $packageName")
                showNotification("Uygulama Acildi", "Uygulama baslatildi: $packageName")
            } else {
                Log.e(TAG, "No launch intent found for app: $packageName")
                showNotification("Uygulama Acilamadi", "Uygulama baslatilamadi: $packageName")
            }
        } catch (e: Exception) {
            Log.e(TAG, "App launch error: ${e.message}", e)
            showNotification("Uygulama Acilamadi", e.message ?: "Uygulama baslatilirken hata olustu")
        }
    }

    private fun executeAppClose(paramsJson: String) {
        try {
            val params = ActionParams.fromJson(paramsJson)
            val packageName = params.packageName

            if (packageName.isNullOrEmpty()) {
                Log.e(TAG, "Package name is empty, app cannot be closed")
                return
            }

            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
            activityManager.killBackgroundProcesses(packageName)
            Log.d(TAG, "App close requested: $packageName")
            showNotification("Uygulama Kapatildi", "Uygulama durduruldu: $packageName")
        } catch (e: Exception) {
            Log.e(TAG, "App close error: ${e.message}", e)
        }
    }

    private fun showNotification(title: String, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

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

        val notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(context, "vetroid_actions")
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(context)
        }
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(Notification.PRIORITY_DEFAULT)
            .build()

        notificationManager.notify(1, notification)
    }
}
