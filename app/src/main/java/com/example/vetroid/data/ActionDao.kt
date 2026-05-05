package com.example.vetroid.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ActionDao {
    @Query("SELECT * FROM actions WHERE scenarioId = :scenarioId ORDER BY executionOrder ASC")
    fun getActionsByScenario(scenarioId: Long): Flow<List<Action>>

    @Query("SELECT * FROM actions WHERE scenarioId = :scenarioId ORDER BY executionOrder ASC")
    suspend fun getActionsByScenarioSync(scenarioId: Long): List<Action>

    @Query("SELECT * FROM actions WHERE id = :id")
    suspend fun getActionById(id: Long): Action?

    @Query("SELECT * FROM actions WHERE scenarioId = :scenarioId AND type = :type ORDER BY id DESC LIMIT 1")
    suspend fun getActionByScenarioAndType(scenarioId: Long, type: String): Action?

    @Insert
    suspend fun insert(action: Action): Long

    @Update
    suspend fun update(action: Action)

    @Delete
    suspend fun delete(action: Action)

    @Query("DELETE FROM actions WHERE scenarioId = :scenarioId")
    suspend fun deleteByScenario(scenarioId: Long)
}