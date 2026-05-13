package com.example.vetroid.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ConstraintDao {
    @Query("SELECT * FROM constraints WHERE scenarioId = :scenarioId")
    fun getConstraintsByScenario(scenarioId: Long): Flow<List<Constraint>>

    @Query("SELECT * FROM constraints WHERE scenarioId = :scenarioId")
    suspend fun getConstraintsByScenarioSync(scenarioId: Long): List<Constraint>

    @Query("SELECT * FROM constraints WHERE id = :id")
    suspend fun getConstraintById(id: Long): Constraint?

    @Query("SELECT * FROM constraints WHERE scenarioId = :scenarioId AND type = :type ORDER BY id DESC LIMIT 1")
    suspend fun getConstraintByScenarioAndType(scenarioId: Long, type: String): Constraint?

    @Insert
    suspend fun insert(constraint: Constraint): Long

    @Update
    suspend fun update(constraint: Constraint)

    @Delete
    suspend fun delete(constraint: Constraint)

    @Query("DELETE FROM constraints WHERE scenarioId = :scenarioId")
    suspend fun deleteByScenario(scenarioId: Long)
}
