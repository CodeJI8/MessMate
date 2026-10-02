package com.singleminds.messmate.data.local.dao

import androidx.room.*
import com.singleminds.messmate.data.local.entity.Member
import kotlinx.coroutines.flow.Flow

@Dao
interface MemberDao {
    @Query("SELECT * FROM members ORDER BY name ASC")
    fun getAllMembersFlow(): Flow<List<Member>>

    @Query("SELECT * FROM members WHERE isActive = 1 ORDER BY name ASC")
    fun getActiveMembersFlow(): Flow<List<Member>>

    @Query("SELECT * FROM members WHERE id = :id LIMIT 1")
    fun getMemberById(id: String): Member?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(member: Member)

    @Update
    fun update(member: Member)

    @Delete
    fun delete(member: Member)
}
