package com.example.projectmobileapplications.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {
    @Query("SELECT * FROM schedule_items")
    fun getAllItems(): Flow<List<ScheduleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ScheduleEntity)

    @Delete
    suspend fun delete(item: ScheduleEntity)

    @Query("DELETE FROM schedule_items WHERE id = :id")
    suspend fun deleteById(id: String)
}
