package com.example.vetroid.data

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
        fun fromJson(json: String): ActionParams {
            val obj = JSONObject(json)
            return ActionParams(
                mode = obj.optString("mode", null).takeIf { it.isNotEmpty() },
                wifiEnabled = if (obj.has("wifiEnabled")) obj.getBoolean("wifiEnabled") else null,
                packageName = obj.optString("packageName", null).takeIf { it.isNotEmpty() },
                notificationTitle = obj.optString("notificationTitle", null).takeIf { it.isNotEmpty() },
                notificationMessage = obj.optString("notificationMessage", null).takeIf { it.isNotEmpty() }
            )
        }
    }
}