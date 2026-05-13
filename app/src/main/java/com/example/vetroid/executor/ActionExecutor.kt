package com.example.vetroid.executor

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import android.telephony.SmsManager
import android.util.Log
import com.example.vetroid.VetroidAccessibilityService
import com.example.vetroid.data.Action
import com.example.vetroid.data.ActionParams

class ActionExecutor(private val context: Context) {

    companion object {
        const val TAG = "ActionExecutor"
        const val ACTION_TYPE_SILENT = "SILENT_MODE"
        const val ACTION_TYPE_WIFI = "WIFI"
        const val ACTION_TYPE_BLUETOOTH = "BLUETOOTH"
        const val ACTION_TYPE_BRIGHTNESS = "BRIGHTNESS"
        const val ACTION_TYPE_APP_LAUNCH = "APP_LAUNCH"
        const val ACTION_TYPE_APP_CLOSE = "APP_CLOSE"
        const val ACTION_TYPE_NOTIFICATION = "NOTIFICATION"
        const val ACTION_TYPE_SMS_SEND = "SMS_SEND"
        const val ACTION_TYPE_SCREENSHOT = "SCREENSHOT"
    }

    fun executeAction(action: Action) {
        Log.d(TAG, "Executing action: ${action.type}")

        when (action.type) {
            ACTION_TYPE_SILENT -> executeSilentMode(action.params)
            ACTION_TYPE_WIFI -> executeWifi(action.params)
            ACTION_TYPE_BLUETOOTH -> executeBluetooth(action.params)
            ACTION_TYPE_BRIGHTNESS -> executeBrightness(action.params)
            ACTION_TYPE_APP_LAUNCH -> executeAppLaunch(action.params)
            ACTION_TYPE_APP_CLOSE -> executeAppClose(action.params)
            ACTION_TYPE_NOTIFICATION -> executeNotification(action.params)
            ACTION_TYPE_SMS_SEND -> executeSms(action.params)
            ACTION_TYPE_SCREENSHOT -> executeScreenshot()
            else -> Log.w(TAG, "Unknown action type: ${action.type}")
        }
    }

    private fun executeSilentMode(paramsJson: String) {
        try {
            val params = ActionParams.fromJson(paramsJson)
            val mode = params.mode
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val notifManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // SILENT modu Android 6+ için "Rahatsız Etme" (DND) erişimi gerektirir
            if (mode == "SILENT" && !notifManager.isNotificationPolicyAccessGranted) {
                Log.w(TAG, "DND erişimi yok — ayarlar ekranı açılıyor")
                showNotification(
                    "İzin Gerekli",
                    "Sessiz mod için 'Rahatsız Etme' erişimine izin verin. Bildrime tıklayın."
                )
                val intent = Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return
            }

            when (mode) {
                "SILENT" -> {
                    audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
                    Log.d(TAG, "Phone switched to silent mode")
                    showNotification("Sessize Alındı", "Telefon sessiz moda geçti")
                }
                "NORMAL" -> {
                    audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
                    Log.d(TAG, "Phone switched to normal mode")
                    showNotification("Ses Açıldı", "Telefon normal moda geçti")
                }
                "VIBRATE" -> {
                    audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE
                    Log.d(TAG, "Phone switched to vibrate mode")
                    showNotification("Titreşim Modu", "Telefon titreşim moduna geçti")
                }
                else -> Log.w(TAG, "Unknown ringer mode: $mode")
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Silent mode izin hatası: ${e.message}", e)
            showNotification("İzin Hatası", "Ses modu değiştirmek için 'Rahatsız Etme' iznini verin")
        } catch (e: Exception) {
            Log.e(TAG, "Silent mode error: ${e.message}", e)
        }
    }

    private fun executeWifi(paramsJson: String) {
        try {
            val params = ActionParams.fromJson(paramsJson)
            val enable = params.wifiEnabled ?: return

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+: erişilebilirlik servisi üzerinden Quick Settings'ten toggle
                val handled = VetroidAccessibilityService.requestWifiToggle(enable)
                if (handled) {
                    val label = if (enable) "Wi-Fi Açılıyor" else "Wi-Fi Kapatılıyor"
                    showNotification(label, "Vetroid tarafından değiştiriliyor...")
                    Log.d(TAG, "Wi-Fi toggle via accessibility: $enable")
                } else {
                    // Servis aktif değil, ayarlar panelini aç
                    val panelIntent = Intent(Settings.Panel.ACTION_WIFI)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(panelIntent)
                    val label = if (enable) "Wi-Fi Açma" else "Wi-Fi Kapatma"
                    showNotification(label, "Erişilebilirlik servisi aktif değil. Ayarlar > Erişilebilirlik > Vetroid'i etkinleştirin.")
                    Log.d(TAG, "Wi-Fi panel opened (accessibility service inactive)")
                }
            } else {
                @Suppress("DEPRECATION")
                val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
                @Suppress("DEPRECATION")
                wifiManager.setWifiEnabled(enable)
                val label = if (enable) "Wi-Fi Açıldı" else "Wi-Fi Kapatıldı"
                showNotification(label, "Vetroid tarafından değiştirildi")
                Log.d(TAG, "Wi-Fi changed: $enable")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Wi-Fi error: ${e.message}", e)
            showNotification("Wi-Fi Hatası", e.message ?: "Bilinmeyen hata")
        }
    }

    private fun executeBluetooth(paramsJson: String) {
        try {
            val params = ActionParams.fromJson(paramsJson)
            val enable = params.bluetoothEnabled ?: return

            val bluetoothAdapter = (context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)?.adapter
            if (bluetoothAdapter == null) {
                Log.e(TAG, "Bluetooth desteklenmiyor")
                return
            }

            @Suppress("DEPRECATION")
            val result = if (enable) bluetoothAdapter.enable() else bluetoothAdapter.disable()

            if (result) {
                val label = if (enable) "Bluetooth Açıldı" else "Bluetooth Kapatıldı"
                showNotification(label, "Vetroid tarafından değiştirildi")
                Log.d(TAG, "Bluetooth changed: $enable")
            } else {
                showNotification("Bluetooth Değiştirilemedi", "Lütfen Bluetooth iznini kontrol edin")
                Log.w(TAG, "Bluetooth toggle returned false")
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Bluetooth izin hatası: ${e.message}", e)
            showNotification("Bluetooth Değiştirilemedi", "Bluetooth izni gerekli")
        } catch (e: Exception) {
            Log.e(TAG, "Bluetooth error: ${e.message}", e)
        }
    }

    private fun executeBrightness(paramsJson: String) {
        try {
            val params = ActionParams.fromJson(paramsJson)
            val brightness = params.brightness ?: return

            if (!Settings.System.canWrite(context)) {
                showNotification("Parlaklık Değiştirilemedi", "Sistem ayarlarını değiştirme izni gerekli")
                Log.w(TAG, "WRITE_SETTINGS permission not granted")
                return
            }

            val resolver = context.contentResolver

            if (brightness == -1) {
                Settings.System.putInt(resolver, Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC)
                showNotification("Parlaklık", "Otomatik parlaklık modu açıldı")
            } else {
                Settings.System.putInt(resolver, Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL)
                Settings.System.putInt(resolver, Settings.System.SCREEN_BRIGHTNESS, brightness)
                val percent = (brightness * 100 / 255)
                showNotification("Parlaklık", "Parlaklık %$percent olarak ayarlandı")
            }

            Log.d(TAG, "Brightness set to: $brightness")
        } catch (e: Exception) {
            Log.e(TAG, "Brightness error: ${e.message}", e)
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

    private fun executeNotification(paramsJson: String) {
        try {
            val params = ActionParams.fromJson(paramsJson)
            val title = params.notificationTitle ?: "Vetroid Bildirim"
            val message = params.notificationMessage ?: ""

            if (message.isEmpty()) {
                Log.w(TAG, "Notification message is empty, skipping")
                return
            }

            showNotification(title, message)
            Log.d(TAG, "✅ Bildirim gösterildi: $title - $message")
        } catch (e: Exception) {
            Log.e(TAG, "Notification error: ${e.message}", e)
        }
    }

    private fun executeSms(paramsJson: String) {
        try {
            val params = ActionParams.fromJson(paramsJson)
            val phoneNumber = params.smsPhoneNumber
            val message = params.smsMessage
            val contactName = params.smsContactName

            if (phoneNumber.isNullOrEmpty()) {
                Log.e(TAG, "SMS phone number is empty, skipping")
                showNotification("SMS Gonderilemedi", "Telefon numarasi yok")
                return
            }

            if (message.isNullOrEmpty()) {
                Log.e(TAG, "SMS message is empty, skipping")
                return
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val smsManager = context.getSystemService(SmsManager::class.java)
                if (message.length > 160) {
                    val parts = smsManager.divideMessage(message)
                    smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null, null)
                } else {
                    smsManager.sendTextMessage(phoneNumber, null, message, null, null)
                }
            } else {
                @Suppress("DEPRECATION")
                val smsManager = SmsManager.getDefault()
                if (message.length > 160) {
                    val parts = smsManager.divideMessage(message)
                    smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null, null)
                } else {
                    smsManager.sendTextMessage(phoneNumber, null, message, null, null)
                }
            }

            val displayName = contactName ?: "Bilinmeyen"
            Log.d(TAG, "✅ SMS gonderildi: $displayName - $message")
            showNotification("SMS Gonderildi", "Alici: $displayName")

        } catch (e: Exception) {
            Log.e(TAG, "SMS gonderme hatasi: ${e.message}", e)
            showNotification("SMS Gonderilemedi", e.message ?: "Bilinmeyen hata")
        }
    }

    private fun executeScreenshot() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            Log.w(TAG, "Ekran görüntüsü Android 11+ gerektirir")
            showNotification("Ekran Görüntüsü", "Bu özellik Android 11+ gerektirir")
            return
        }
        val success = VetroidAccessibilityService.requestScreenshot()
        if (success) {
            Log.d(TAG, "✅ Ekran görüntüsü isteği gönderildi")
            // Kayıt sonucu bildirimi VetroidAccessibilityService'den gelecek
        } else {
            Log.w(TAG, "Erişilebilirlik servisi aktif değil")
            showNotification(
                "Ekran Görüntüsü Alınamadı",
                "Ayarlar > Erişilebilirlik > Vetroid servisini etkinleştirin"
            )
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
