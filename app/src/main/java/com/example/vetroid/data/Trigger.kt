package com.example.vetroid.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "triggers",
    foreignKeys = [
        ForeignKey(
            entity = Scenario::class,
            parentColumns = ["id"],
            childColumns = ["scenarioId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("scenarioId")]
)
data class Trigger(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val scenarioId: Long,
    val type: String, // "GEOFENCE", "TIME", "BATTERY", "APP", "SHAKE"
    val params: String, // JSON string: {"latitude": 39.92, "longitude": 32.85, "radius": 200, "transition": "ENTER"}
    val isActive: Boolean = true
)