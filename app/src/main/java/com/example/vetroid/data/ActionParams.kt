package com.example.vetroid.data

import android.util.Log
import org.json.JSONObject

data class ActionParams(
    val mode: String? = null,
    val wifiEnabled: Boolean? = null,
    val packageName: String? = null,
    val notificationTitle: String? = null,
    val notificationMessage: String? = null,
    val smsPhoneNumber: String? = null,
    val smsContactName: String? = null,
    val smsMessage: String? = null
) {
    fun toJson(): String {
        return JSONObject().apply {
            mode?.let { put("mode", it) }
            wifiEnabled?.let { put("wifiEnabled", it) }
            packageName?.let { put("packageName", it) }
            notificationTitle?.let { put("notificationTitle", it) }
            notificationMessage?.let { put("notificationMessage", it) }
            smsPhoneNumber?.let { put("smsPhoneNumber", it) }
            smsContactName?.let { put("smsContactName", it) }
            smsMessage?.let { put("smsMessage", it) }
        }.toString()
    }

    companion object {
        fun fromJson(json: String?): ActionParams {
            if (json.isNullOrEmpty()) return ActionParams()

            return try {
                val obj = JSONObject(json)
                ActionParams(
                    mode = obj.optString("mode").takeIf { it.isNotEmpty() },
                    wifiEnabled = if (obj.has("wifiEnabled")) obj.getBoolean("wifiEnabled") else null,
                    packageName = obj.optString("packageName").takeIf { it.isNotEmpty() },
                    notificationTitle = obj.optString("notificationTitle").takeIf { it.isNotEmpty() },
                    notificationMessage = obj.optString("notificationMessage").takeIf { it.isNotEmpty() },
                    smsPhoneNumber = obj.optString("smsPhoneNumber").takeIf { it.isNotEmpty() },
                    smsContactName = obj.optString("smsContactName").takeIf { it.isNotEmpty() },
                    smsMessage = obj.optString("smsMessage").takeIf { it.isNotEmpty() }
                )
            } catch (e: Exception) {
                Log.e("ActionParams", "JSON parse hatası: ${e.message}")
                ActionParams()
            }
        }
    }
}