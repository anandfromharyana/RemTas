package com.anand.remtas.ui.localDB

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ReminderDao {


    @Query("SELECT * FROM reminders ORDER BY dateTime ASC")
    fun getAllReminders(): LiveData<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE dateTime >= :currentTime AND isCompleted = 0 ORDER BY dateTime ASC")
    fun getUpcomingReminders(currentTime: Long): LiveData<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE id = :id")
    suspend fun getReminderById(id: Int): Reminder?

    @Insert
    suspend fun insertReminder(reminder: Reminder): Long

    @Update
    suspend fun updateReminder(reminder: Reminder)

    @Delete
    suspend fun deleteReminder(reminder: Reminder)

    @Query("UPDATE reminders SET isCompleted = :isCompleted WHERE id = :id")
    suspend fun updateReminderStatus(id: Int, isCompleted: Boolean)

    @Query("SELECT * FROM reminders WHERE isCompleted = 1 ORDER BY dateTime DESC")
    fun getCompletedReminders(): LiveData<List<Reminder>>

    @Query("DELETE FROM reminders WHERE isCompleted = 1")
    suspend fun deleteCompletedReminders()
}