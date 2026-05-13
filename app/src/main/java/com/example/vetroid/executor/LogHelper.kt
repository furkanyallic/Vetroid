package com.example.vetroid.executor

import com.example.vetroid.data.AppDatabase
import com.example.vetroid.data.ExecutionLog

object LogHelper {
    suspend fun log(
        db: AppDatabase,
        scenarioId: Long,
        scenarioName: String,
        triggerType: String,
        success: Boolean,
        note: String = ""
    ) {
        db.executionLogDao().insert(
            ExecutionLog(
                scenarioId = scenarioId,
                scenarioName = scenarioName,
                triggerType = triggerType,
                success = success,
                note = note
            )
        )
    }
}
