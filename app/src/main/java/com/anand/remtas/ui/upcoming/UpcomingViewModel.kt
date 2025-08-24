package com.anand.remtas.ui.upcoming

import android.app.Application
import androidx.lifecycle.*
import com.anand.remtas.ui.localDB.AlarmDatabase
import com.anand.remtas.ui.localDB.AlarmEntity
import kotlinx.coroutines.launch

class UpcomingViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AlarmDatabase.getDatabase(application).alarmDao()

    // Expose alarms list as LiveData (auto updates UI when DB changes)
    val alarms: LiveData<List<AlarmEntity>> = dao.getAllAlarms()

    // Get only enabled alarms using MediatorLiveData
    private val _enabledAlarms = MediatorLiveData<List<AlarmEntity>>()
    val enabledAlarms: LiveData<List<AlarmEntity>> = _enabledAlarms

    // Get upcoming alarms (sorted by time) using MediatorLiveData
    private val _upcomingAlarms = MediatorLiveData<List<AlarmEntity>>()
    val upcomingAlarms: LiveData<List<AlarmEntity>> = _upcomingAlarms

    init {
        // Set up MediatorLiveData sources
        _enabledAlarms.addSource(alarms) { alarmList ->
            _enabledAlarms.value = alarmList.filter { it.isEnabled }
        }

        _upcomingAlarms.addSource(alarms) { alarmList ->
            _upcomingAlarms.value = alarmList.sortedBy { alarm ->
                // Convert time string to comparable format for sorting
                val timeParts = alarm.time.split(":")
                val hour = timeParts[0].toIntOrNull() ?: 0
                val minute = timeParts[1].toIntOrNull() ?: 0
                hour * 60 + minute // Convert to minutes for easy sorting
            }
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

    // Get count of active alarms using MediatorLiveData
    private val _activeAlarmCount = MediatorLiveData<Int>()
    fun getActiveAlarmCount(): LiveData<Int> {
        _activeAlarmCount.addSource(alarms) { alarmList ->
            _activeAlarmCount.value = alarmList.count { it.isEnabled }
        }
        return _activeAlarmCount
    }
}