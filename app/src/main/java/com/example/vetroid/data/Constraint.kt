package com.example.vetroid.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "constraints",
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
data class Constraint(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val scenarioId: Long,
    val type: String,   // "TIME_WINDOW", "WIFI", "CHARGING"
    val params: String, // JSON
    val isActive: Boolean = true
)
