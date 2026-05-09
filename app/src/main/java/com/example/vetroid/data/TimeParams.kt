package com.example.vetroid.data

import org.json.JSONArray
import org.json.JSONObject

data class TimeSchedule(
    val day: Int,      // 1=Pazartesi, 7=Pazar
    val hour: Int,
    val minute: Int
)

data class TimeParams(
    val type: String,  // "ONCE" veya "RECURRING"
    val year: Int? = null,
    val month: Int? = null,
    val day: Int? = null,
    val hour: Int? = null,
    val minute: Int? = null,
    val schedule: List<TimeSchedule>? = null
) {
    fun toJson(): String {
        val obj = JSONObject().apply {
            put("type", type)
            year?.let { put("year", it) }
            month?.let { put("month", it) }
            day?.let { put("day", it) }
            hour?.let { put("hour", it) }
            minute?.let { put("minute", it) }
            schedule?.let { list ->
                val array = JSONArray()
                list.forEach { scheduleItem ->
                    val itemObj = JSONObject().apply {
                        put("day", scheduleItem.day)
                        put("hour", scheduleItem.hour)
                        put("minute", scheduleItem.minute)
                    }
                    array.put(itemObj)
                }
                put("schedule", array)
            }
        }
        return obj.toString()
    }

    companion object {
        fun fromJson(json: String): TimeParams {
            val obj = JSONObject(json)
            val type = obj.getString("type")

            if (type == "RECURRING" && obj.has("schedule")) {
                val scheduleArray = obj.getJSONArray("schedule")
                val scheduleList = mutableListOf<TimeSchedule>()
                for (i in 0 until scheduleArray.length()) {
                    val item = scheduleArray.getJSONObject(i)
                    scheduleList.add(
                        TimeSchedule(
                            day = item.getInt("day"),
                            hour = item.getInt("hour"),
                            minute = item.getInt("minute")
                        )
                    )
                }
                return TimeParams(
                    type = type,
                    schedule = scheduleList
                )
            }

            return TimeParams(
                type = type,
                year = if (obj.has("year")) obj.getInt("year") else null,
                month = if (obj.has("month")) obj.getInt("month") else null,
                day = if (obj.has("day")) obj.getInt("day") else null,
                hour = if (obj.has("hour")) obj.getInt("hour") else null,
                minute = if (obj.has("minute")) obj.getInt("minute") else null
            )
        }
    }
}