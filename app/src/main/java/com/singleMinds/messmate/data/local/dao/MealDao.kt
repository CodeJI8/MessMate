package com.singleminds.messmate.data.local.dao

import androidx.room.*
import com.singleminds.messmate.data.local.entity.MealEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface MealDao {
    @Query("SELECT * FROM meal_entries WHERE date >= :startTimestamp AND date <= :endTimestamp")
    fun getMealsForMonthFlow(startTimestamp: Long, endTimestamp: Long): Flow<List<MealEntry>>

    @Query("SELECT * FROM meal_entries WHERE date >= :startTimestamp AND date <= :endTimestamp")
    fun getMealsForMonth(startTimestamp: Long, endTimestamp: Long): List<MealEntry>

    @Query("SELECT * FROM meal_entries WHERE memberId = :memberId AND date = :date LIMIT 1")
    fun getMealForMemberAndDate(memberId: String, date: Long): MealEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdate(entry: MealEntry)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdateAll(entries: List<MealEntry>)

    @Delete
    fun delete(entry: MealEntry)
}
