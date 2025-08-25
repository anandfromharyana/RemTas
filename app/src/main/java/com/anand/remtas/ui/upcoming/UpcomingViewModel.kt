package com.anand.remtas.ui.upcoming

import android.app.Application
import androidx.lifecycle.*
import com.anand.remtas.ui.localDB.AlarmDatabase
import com.anand.remtas.ui.localDB.AlarmEntity
import com.anand.remtas.ui.localDB.AppDatabase
import com.anand.remtas.ui.localDB.Reminder
import com.anand.remtas.ui.localDB.ReminderRepository
import kotlinx.coroutines.launch

class UpcomingViewModel(application: Application) : AndroidViewModel(application) {

    // Alarm Database
    private val alarmDao = AlarmDatabase.getDatabase(application).alarmDao()

    // Reminder Database
    private val reminderDao = AppDatabase.getDatabase(application).reminderDao()
    private val reminderRepository = ReminderRepository(reminderDao)

    // Original alarm data
    val alarms: LiveData<List<AlarmEntity>> = alarmDao.getAllAlarms()
    val enabledAlarms: LiveData<List<AlarmEntity>> = alarmDao.getAllAlarms().map { alarms ->
        alarms.filter { it.isEnabled }
    }

    // Reminder data
    val upcomingReminders: LiveData<List<Reminder>> = reminderRepository.getUpcomingReminders()

    // Combined upcoming items
    private val _upcomingItems = MediatorLiveData<List<UpcomingItem>>()
    val upcomingItems: LiveData<List<UpcomingItem>> = _upcomingItems

    init {
        setupCombinedData()
    }

    private fun setupCombinedData() {
        _upcomingItems.addSource(alarms) {
            combineData()
        }
        _upcomingItems.addSource(upcomingReminders) {
            combineData()
        }
    }

    private fun combineData() {
        val alarmList = alarms.value ?: emptyList()
        val reminderList = upcomingReminders.value ?: emptyList()

        val combinedList = mutableListOf<UpcomingItem>()

        // Add alarms
        alarmList.forEach { alarm ->
            combinedList.add(UpcomingItem.AlarmItem(alarm))
        }

        // Add reminders
        reminderList.forEach { reminder ->
            combinedList.add(UpcomingItem.ReminderItem(reminder))
        }

        // Sort by time (earliest first)
        val sortedList = combinedList.sortedWith { item1, item2 ->
            when {
                item1 is UpcomingItem.ReminderItem && item2 is UpcomingItem.AlarmItem -> {
                    // For reminders vs alarms, compare actual datetime with today's alarm time
                    val reminderTime = item1.reminder.dateTime.time
                    val todayCalendar = java.util.Calendar.getInstance()
                    val alarmTimeParts = item2.alarm.time.split(":")
                    todayCalendar.set(java.util.Calendar.HOUR_OF_DAY, alarmTimeParts[0].toInt())
                    todayCalendar.set(java.util.Calendar.MINUTE, alarmTimeParts[1].toInt())
                    todayCalendar.set(java.util.Calendar.SECOND, 0)
                    todayCalendar.set(java.util.Calendar.MILLISECOND, 0)

                    reminderTime.compareTo(todayCalendar.timeInMillis)
                }
                item1 is UpcomingItem.AlarmItem && item2 is UpcomingItem.ReminderItem -> {
                    // For alarms vs reminders
                    val todayCalendar = java.util.Calendar.getInstance()
                    val alarmTimeParts = item1.alarm.time.split(":")
                    todayCalendar.set(java.util.Calendar.HOUR_OF_DAY, alarmTimeParts[0].toInt())
                    todayCalendar.set(java.util.Calendar.MINUTE, alarmTimeParts[1].toInt())
                    todayCalendar.set(java.util.Calendar.SECOND, 0)
                    todayCalendar.set(java.util.Calendar.MILLISECOND, 0)

                    val reminderTime = item2.reminder.dateTime.time
                    todayCalendar.timeInMillis.compareTo(reminderTime)
                }
                item1 is UpcomingItem.AlarmItem && item2 is UpcomingItem.AlarmItem -> {
                    // Compare alarm times
                    item1.sortTime.compareTo(item2.sortTime)
                }
                item1 is UpcomingItem.ReminderItem && item2 is UpcomingItem.ReminderItem -> {
                    // Compare reminder times
                    item1.reminder.dateTime.compareTo(item2.reminder.dateTime)
                }
                else -> 0
            }
        }

        _upcomingItems.value = sortedList
    }

    // Alarm operations
    fun updateAlarm(alarm: AlarmEntity) {
        viewModelScope.launch {
            alarmDao.update(alarm)
        }
    }

    fun deleteAlarm(alarm: AlarmEntity) {
        viewModelScope.launch {
            alarmDao.delete(alarm)
        }
    }

    fun toggleAlarm(alarm: AlarmEntity, isEnabled: Boolean) {
        viewModelScope.launch {
            val updatedAlarm = alarm.copy(isEnabled = isEnabled)
            alarmDao.update(updatedAlarm)
        }
    }

    // Reminder operations
    fun updateReminderStatus(reminder: Reminder, isCompleted: Boolean) {
        viewModelScope.launch {
            reminderRepository.updateReminderStatus(reminder.id, isCompleted)
        }
    }

    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch {
            reminderRepository.deleteReminder(reminder)
        }
    }

    // Get count of active items
    fun getActiveItemCount(): LiveData<Int> {
        return upcomingItems.map { items ->
            items.count { it.isEnabled }
        }
    }
}