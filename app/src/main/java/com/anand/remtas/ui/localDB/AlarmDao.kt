package com.anand.remtas.ui.localDB
import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface AlarmDao {
    @Query("SELECT * FROM alarms")
    fun getAllAlarms(): LiveData<List<AlarmEntity>>

    @Insert
    suspend fun insert(alarm: AlarmEntity)

    @Update
    suspend fun update(alarm: AlarmEntity)

    @Delete
    suspend fun delete(alarm: AlarmEntity)
}