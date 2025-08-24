package com.anand.remtas.ui.alarm

import android.app.Application
import androidx.lifecycle.*
import com.anand.remtas.ui.localDB.AlarmDatabase
import com.anand.remtas.ui.localDB.AlarmEntity
import kotlinx.coroutines.launch

class AlarmViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AlarmDatabase.getDatabase(application).alarmDao()

    // Expose alarms list as LiveData (auto updates UI when DB changes)
    val alarms: LiveData<List<AlarmEntity>> = dao.getAllAlarms()

    // Insert new alarm
    fun addAlarm(alarm: AlarmEntity) {
        viewModelScope.launch {
            dao.insert(alarm)
        }
    }

    // Update an existing alarm
    fun updateAlarm(alarm: AlarmEntity) {
        viewModelScope.launch {
            dao.update(alarm)
        }
    }

    // Delete an alarm
    fun deleteAlarm(alarm: AlarmEntity) {
        viewModelScope.launch {
            dao.delete(alarm)
        }
    }

    // Toggle alarm enabled/disabled
    fun toggleAlarm(alarm: AlarmEntity, isEnabled: Boolean) {
        viewModelScope.launch {
            val updatedAlarm = alarm.copy(isEnabled = isEnabled)
            dao.update(updatedAlarm)
        }
    }
}