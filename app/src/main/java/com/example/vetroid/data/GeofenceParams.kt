package com.example.vetroid.data

import org.json.JSONObject

data class GeofenceParams(
    val latitude: Double,
    val longitude: Double,
    val radius: Float,
    val transitionType: String // "ENTER", "EXIT", "BOTH"
) {
    fun toJson(): String {
        return JSONObject().apply {
            put("latitude", latitude)
            put("longitude", longitude)
            put("radius", radius)
            put("transition", transitionType)
        }.toString()
    }

    companion object {
        fun fromJson(json: String): GeofenceParams {
            val obj = JSONObject(json)
            return GeofenceParams(
                latitude = obj.getDouble("latitude"),
                longitude = obj.getDouble("longitude"),
                radius = obj.getDouble("radius").toFloat(),
                transitionType = obj.getString("transition")
            )
        }
    }

    fun getTransitionTypeInt(): Int {
        return when (transitionType) {
            "ENTER" -> 1
            "EXIT" -> 2
            "BOTH" -> 3
            else -> 1
        }
    }
}