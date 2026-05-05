package com.example.vetroid.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "actions",
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
data class Action(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val scenarioId: Long,
    val type: String, // "SMS", "WIFI", "BLUETOOTH", "BRIGHTNESS", "VOLUME", "NOTIFICATION"
    val params: String, // JSON string: {"phoneNumber": "05321234567", "message": "Evdesin", "wifiState": true}
    val executionOrder: Int = 0,
    val isActive: Boolean = true
)