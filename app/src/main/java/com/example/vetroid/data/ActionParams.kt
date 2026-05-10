package com.example.vetroid.data

import android.util.Log
import org.json.JSONObject

data class ActionParams(
    val mode: String? = null,           // "SILENT", "NORMAL", "VIBRATE"
    val wifiEnabled: Boolean? = null,   // true = aç, false = kapa
    val packageName: String? = null,   // Uygulama paket adı
    val notificationTitle: String? = null,
    val notificationMessage: String? = null
) {
    fun toJson(): String {
        return JSONObject().apply {
            mode?.let { put("mode", it) }
            wifiEnabled?.let { put("wifiEnabled", it) }
            packageName?.let { put("packageName", it) }
            notificationTitle?.let { put("notificationTitle", it) }
            notificationMessage?.let { put("notificationMessage", it) }
        }.toString()
    }

    companion object {
        fun fromJson(json: String?): ActionParams {
            if (json.isNullOrEmpty()) return ActionParams()

            return try {
                val obj = JSONObject(json)
                ActionParams(
                    // optString(key) anahtar yoksa "" (boş string) döner, null dönmez.
                    // takeIf ile boş değilse değerini alırız, boşsa null kalır.
                    mode = obj.optString("mode").takeIf { it.isNotEmpty() },
                    wifiEnabled = if (obj.has("wifiEnabled")) obj.getBoolean("wifiEnabled") else null,
                    packageName = obj.optString("packageName").takeIf { it.isNotEmpty() },
                    notificationTitle = obj.optString("notificationTitle").takeIf { it.isNotEmpty() },
                    notificationMessage = obj.optString("notificationMessage").takeIf { it.isNotEmpty() }
                )
            } catch (e: Exception) {
                Log.e("ActionParams", "❌ JSON parse hatası: ${e.message}")
                ActionParams()
            }
        }
    }
}



