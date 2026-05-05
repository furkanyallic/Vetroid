package com.example.vetroid.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TriggerDao {
    @Query("SELECT * FROM triggers WHERE scenarioId = :scenarioId")
    fun getTriggersByScenario(scenarioId: Long): Flow<List<Trigger>>

    @Query("SELECT * FROM triggers WHERE scenarioId = :scenarioId")
    suspend fun getTriggersByScenarioSync(scenarioId: Long): List<Trigger>

    @Query("SELECT * FROM triggers WHERE isActive = 1")
    fun getActiveTriggers(): Flow<List<Trigger>>

    @Query("SELECT * FROM triggers WHERE isActive = 1 AND type = :type")
    suspend fun getActiveTriggersByType(type: String): List<Trigger>

    @Query("SELECT * FROM triggers WHERE id = :id")
    suspend fun getTriggerById(id: Long): Trigger?

    @Query("SELECT * FROM triggers WHERE scenarioId = :scenarioId AND type = :type ORDER BY id DESC LIMIT 1")
    suspend fun getTriggerByScenarioAndType(scenarioId: Long, type: String): Trigger?

    @Insert
    suspend fun insert(trigger: Trigger): Long

    @Update
    suspend fun update(trigger: Trigger)

    @Delete
    suspend fun delete(trigger: Trigger)

    @Query("DELETE FROM triggers WHERE scenarioId = :scenarioId")
    suspend fun deleteByScenario(scenarioId: Long)
}