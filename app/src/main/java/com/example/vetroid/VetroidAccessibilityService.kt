package com.example.vetroid

import android.accessibilityservice.AccessibilityService
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.annotation.RequiresApi

class VetroidAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "VetroidA11yService"

        @Volatile
        var instance: VetroidAccessibilityService? = null

        fun isEnabled(): Boolean = instance != null

        fun requestScreenshot(): Boolean {
            val svc = instance ?: return false
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return false
            svc.doTakeScreenshot()
            return true
        }

        fun requestWifiToggle(enable: Boolean): Boolean {
            val svc = instance ?: return false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                svc.toggleWifiViaQuickSettings(enable)
                return true
            }
            return false
        }
    }

    override fun onServiceConnected() {
        instance = this
        Log.d(TAG, "Erişilebilirlik servisi bağlandı")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit
    override fun onInterrupt() = Unit

    override fun onUnbind(intent: android.content.Intent?): Boolean {
        instance = null
        return super.onUnbind(intent)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun toggleWifiViaQuickSettings(enable: Boolean) {
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (wifiManager.isWifiEnabled == enable) {
            Log.d(TAG, "Wi-Fi zaten istenen durumda: $enable")
            return
        }
        performGlobalAction(GLOBAL_ACTION_QUICK_SETTINGS)
        Handler(Looper.getMainLooper()).postDelayed({
            val root = rootInActiveWindow ?: run {
                Log.w(TAG, "Quick Settings açılamadı, rootInActiveWindow null")
                return@postDelayed
            }
            val keywords = listOf("Wi-Fi", "WiFi", "WLAN", "Wifi")
            var clicked = false
            outer@ for (keyword in keywords) {
                val nodes = root.findAccessibilityNodeInfosByText(keyword)
                for (node in nodes) {
                    val target = findClickableAncestor(node)
                    if (target != null && target.isClickable) {
                        target.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        clicked = true
                        Log.d(TAG, "Wi-Fi tile tıklandı (keyword: $keyword)")
                        break@outer
                    }
                }
            }
            root.recycle()
            if (clicked) {
                Handler(Looper.getMainLooper()).postDelayed({
                    performGlobalAction(GLOBAL_ACTION_BACK)
                }, 400)
            } else {
                Log.w(TAG, "Wi-Fi tile bulunamadı, Quick Settings kapatılıyor")
                performGlobalAction(GLOBAL_ACTION_BACK)
            }
        }, 700)
    }

    private fun findClickableAncestor(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        var current: AccessibilityNodeInfo? = node
        repeat(6) {
            if (current?.isClickable == true) return current
            current = current?.parent
        }
        return null
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun doTakeScreenshot() {
        takeScreenshot(
            android.view.Display.DEFAULT_DISPLAY,
            mainExecutor,
            object : TakeScreenshotCallback {
                override fun onSuccess(screenshot: ScreenshotResult) {
                    val hardwareBitmap = Bitmap.wrapHardwareBuffer(
                        screenshot.hardwareBuffer,
                        screenshot.colorSpace
                    )
                    // Hardware buffer'ı kapatmadan ÖNCE software kopyasını al
                    val softBitmap = hardwareBitmap?.copy(Bitmap.Config.ARGB_8888, false)
                    hardwareBitmap?.recycle()
                    screenshot.hardwareBuffer.close()

                    if (softBitmap == null) {
                        Log.e(TAG, "Bitmap kopyalanamadı — null döndü")
                        showServiceNotification("Ekran Görüntüsü Alınamadı", "Bitmap oluşturulamadı")
                        return
                    }
                    // Disk I/O main thread'de çalışmamalı
                    Thread { saveToGallery(softBitmap) }.start()
                }

                override fun onFailure(errorCode: Int) {
                    Log.e(TAG, "Ekran görüntüsü alınamadı, hata kodu: $errorCode")
                    showServiceNotification("Ekran Görüntüsü Alınamadı", "Hata kodu: $errorCode")
                }
            }
        )
    }

    private fun saveToGallery(bitmap: Bitmap) {
        try {
            val filename = "Vetroid_${System.currentTimeMillis()}.png"
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.RELATIVE_PATH,
                    "${Environment.DIRECTORY_PICTURES}/Vetroid")
            }
            val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            if (uri == null) {
                Log.e(TAG, "MediaStore URI null — kayıt başarısız")
                showServiceNotification("Ekran Görüntüsü", "Galeriye kaydedilemedi")
                return
            }
            contentResolver.openOutputStream(uri)?.use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            Log.d(TAG, "Ekran görüntüsü kaydedildi: $filename")
            showServiceNotification("Ekran Görüntüsü Kaydedildi", "Pictures/Vetroid klasörüne eklendi")
        } catch (e: Exception) {
            Log.e(TAG, "Galeriye kaydetme hatası: ${e.message}", e)
            showServiceNotification("Ekran Görüntüsü", "Kaydetme hatası: ${e.message}")
        } finally {
            bitmap.recycle()
        }
    }

    private fun showServiceNotification(title: String, message: String) {
        val notifManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "vetroid_actions", "Vetroid Eylemleri", NotificationManager.IMPORTANCE_DEFAULT
            )
            notifManager.createNotificationChannel(channel)
        }
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, "vetroid_actions")
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
        }
        val notif = builder
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .build()
        notifManager.notify(2, notif)
    }
}
