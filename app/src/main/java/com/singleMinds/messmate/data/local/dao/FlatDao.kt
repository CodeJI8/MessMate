package com.singleminds.messmate.data.local.dao

import androidx.room.*
import com.singleminds.messmate.data.local.entity.Flat
import kotlinx.coroutines.flow.Flow

@Dao
interface FlatDao {
    @Query("SELECT * FROM flats WHERE id = 'default_flat' LIMIT 1")
    fun getFlatFlow(): Flow<Flat?>

    @Query("SELECT * FROM flats WHERE id = 'default_flat' LIMIT 1")
    fun getFlat(): Flat?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdate(flat: Flat)
}
