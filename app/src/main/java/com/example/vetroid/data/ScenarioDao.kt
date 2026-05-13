package com.example.vetroid.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ScenarioDao {
    @Query("SELECT * FROM scenarios ORDER BY createdAt DESC")
    fun getAllScenarios(): Flow<List<Scenario>>

    @Query("SELECT * FROM scenarios WHERE isActive = 1")
    fun getActiveScenarios(): Flow<List<Scenario>>

    @Query("SELECT * FROM scenarios WHERE id = :id")
    suspend fun getScenarioById(id: Long): Scenario?

    @Query("SELECT * FROM scenarios WHERE name = :name LIMIT 1")
    suspend fun getScenarioByName(name: String): Scenario?

    @Insert
    suspend fun insert(scenario: Scenario): Long

    @Update
    suspend fun update(scenario: Scenario)

    @Delete
    suspend fun delete(scenario: Scenario)

    @Query("UPDATE scenarios SET isActive = :isActive WHERE id = :id")
    suspend fun setActive(id: Long, isActive: Boolean)
}