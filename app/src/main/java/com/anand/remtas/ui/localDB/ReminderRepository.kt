package com.anand.remtas.ui.localDB

import androidx.lifecycle.LiveData


class ReminderRepository(private val reminderDao: ReminderDao) {

    fun getAllReminders(): LiveData<List<Reminder>> = reminderDao.getAllReminders()

    fun getUpcomingReminders(): LiveData<List<Reminder>> =
        reminderDao.getUpcomingReminders(System.currentTimeMillis())

    suspend fun getReminderById(id: Int): Reminder? = reminderDao.getReminderById(id)

    suspend fun insertReminder(reminder: Reminder): Long = reminderDao.insertReminder(reminder)

    suspend fun updateReminder(reminder: Reminder) = reminderDao.updateReminder(reminder)

    suspend fun deleteReminder(reminder: Reminder) = reminderDao.deleteReminder(reminder)

    suspend fun updateReminderStatus(id: Int, isCompleted: Boolean) =
        reminderDao.updateReminderStatus(id, isCompleted)

    fun getCompletedReminders(): LiveData<List<Reminder>> = reminderDao.getCompletedReminders()

    suspend fun deleteCompletedReminders() = reminderDao.deleteCompletedReminders()
}