package com.example.vetroid.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "execution_logs")
data class ExecutionLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val scenarioId: Long,
    val scenarioName: String,
    val triggerType: String,   // "TIME", "GEOFENCE", "BATTERY", "SYSTEM", "MANUAL"
    val timestamp: Long = System.currentTimeMillis(),
    val success: Boolean = true,
    val note: String = ""
)
