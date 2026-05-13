package com.example.vetroid.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ExecutionLogDao {

    @Query("SELECT * FROM execution_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<ExecutionLog>>

    @Insert
    suspend fun insert(log: ExecutionLog)

    @Query("DELETE FROM execution_logs WHERE timestamp < :cutoff")
    suspend fun deleteOlderThan(cutoff: Long)

    @Query("DELETE FROM execution_logs")
    suspend fun deleteAll()
}
