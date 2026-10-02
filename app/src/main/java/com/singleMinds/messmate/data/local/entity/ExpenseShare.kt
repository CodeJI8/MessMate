package com.singleminds.messmate.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "expense_shares")
data class ExpenseShare(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val expenseId: String,
    val memberId: String,
    val shareAmountMinor: Long,
    val weightOrValue: Double = 1.0
)
