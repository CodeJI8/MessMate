package com.singleminds.messmate.data.local.dao

import androidx.room.*
import com.singleminds.messmate.data.local.entity.AuditLog
import kotlinx.coroutines.flow.Flow

@Dao
interface AuditDao {
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC")
    fun getAllLogsFlow(): Flow<List<AuditLog>>

    @Query("SELECT * FROM audit_logs WHERE entityId = :entityId ORDER BY timestamp DESC")
    fun getLogsForEntityFlow(entityId: String): Flow<List<AuditLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertLog(log: AuditLog)
}
