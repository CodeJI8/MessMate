package com.singleminds.messmate.data.local.dao

import androidx.room.*
import com.singleminds.messmate.data.local.entity.Month
import kotlinx.coroutines.flow.Flow

@Dao
interface MonthDao {
    @Query("SELECT * FROM months ORDER BY startTimestamp DESC")
    fun getAllMonthsFlow(): Flow<List<Month>>

    @Query("SELECT * FROM months ORDER BY startTimestamp DESC")
    fun getAllMonths(): List<Month>

    @Query("SELECT * FROM months WHERE id = :id LIMIT 1")
    fun getMonthById(id: String): Month?

    @Query("SELECT * FROM months WHERE isClosed = 0 ORDER BY startTimestamp DESC LIMIT 1")
    fun getCurrentMonthFlow(): Flow<Month?>

    @Query("SELECT * FROM months WHERE isClosed = 0 ORDER BY startTimestamp DESC LIMIT 1")
    fun getCurrentMonth(): Month?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdate(month: Month)
}
