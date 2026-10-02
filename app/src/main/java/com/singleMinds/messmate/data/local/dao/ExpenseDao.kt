package com.singleminds.messmate.data.local.dao

import androidx.room.*
import com.singleminds.messmate.data.local.entity.Expense
import com.singleminds.messmate.data.local.entity.ExpenseShare
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses WHERE date >= :startTimestamp AND date <= :endTimestamp ORDER BY date DESC")
    fun getExpensesForMonthFlow(startTimestamp: Long, endTimestamp: Long): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE date >= :startTimestamp AND date <= :endTimestamp ORDER BY date DESC")
    fun getExpensesForMonth(startTimestamp: Long, endTimestamp: Long): List<Expense>

    @Query("SELECT * FROM expenses WHERE id = :id LIMIT 1")
    fun getExpenseById(id: String): Expense?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertExpense(expense: Expense)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertShares(shares: List<ExpenseShare>)

    @Query("SELECT * FROM expense_shares WHERE expenseId = :expenseId")
    fun getSharesForExpense(expenseId: String): List<ExpenseShare>

    @Query("SELECT * FROM expense_shares")
    fun getAllShares(): List<ExpenseShare>

    @Update
    fun updateExpense(expense: Expense)

    @Delete
    fun deleteExpense(expense: Expense)

    @Query("DELETE FROM expense_shares WHERE expenseId = :expenseId")
    fun deleteSharesForExpense(expenseId: String)
}
