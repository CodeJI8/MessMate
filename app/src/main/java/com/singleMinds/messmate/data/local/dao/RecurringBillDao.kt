package com.singleminds.messmate.data.local.dao

import androidx.room.*
import com.singleminds.messmate.data.local.entity.RecurringBill
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringBillDao {
    @Query("SELECT * FROM recurring_bills WHERE isActive = 1 ORDER BY title ASC")
    fun getActiveRecurringBillsFlow(): Flow<List<RecurringBill>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdate(bill: RecurringBill)

    @Delete
    fun delete(bill: RecurringBill)
}
