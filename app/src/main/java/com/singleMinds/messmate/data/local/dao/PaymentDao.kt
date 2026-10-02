package com.singleminds.messmate.data.local.dao

import androidx.room.*
import com.singleminds.messmate.data.local.entity.Payment
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payments WHERE date >= :startTimestamp AND date <= :endTimestamp ORDER BY date DESC")
    fun getPaymentsForMonthFlow(startTimestamp: Long, endTimestamp: Long): Flow<List<Payment>>

    @Query("SELECT * FROM payments WHERE date >= :startTimestamp AND date <= :endTimestamp ORDER BY date DESC")
    fun getPaymentsForMonth(startTimestamp: Long, endTimestamp: Long): List<Payment>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPayment(payment: Payment)

    @Delete
    fun deletePayment(payment: Payment)
}
