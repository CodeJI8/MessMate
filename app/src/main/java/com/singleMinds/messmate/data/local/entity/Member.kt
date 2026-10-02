package com.singleminds.messmate.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "members")
data class Member(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val colorIndex: Int = 0,
    val joinDate: Long = System.currentTimeMillis(),
    val leaveDate: Long? = null,
    val isActive: Boolean = true
)
