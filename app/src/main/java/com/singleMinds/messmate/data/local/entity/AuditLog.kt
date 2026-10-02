package com.singleminds.messmate.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "audit_logs")
data class AuditLog(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val entity: String,
    val entityId: String,
    val action: String,
    val oldValJson: String? = null,
    val newValJson: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
