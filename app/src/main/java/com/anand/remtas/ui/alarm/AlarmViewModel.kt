package com.anand.remtas.ui.alarm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

data class AlarmItem(
    val time: String,
    val name: String = "Alarm",
    val selectedDays: Set<Int> = emptySet(),
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val snoozeEnabled: Boolean = true
)

class AlarmViewModel : ViewModel() {
    private val _alarms = MutableLiveData<MutableList<AlarmItem>>(mutableListOf())
    val alarms: LiveData<MutableList<AlarmItem>> get() = _alarms

    private val _alarmName = MutableLiveData("Morning Run")
    val alarmName: LiveData<String> get() = _alarmName

    private val _soundName = MutableLiveData("Chime Time")
    val soundName: LiveData<String> get() = _soundName

    private val _vibrationPattern = MutableLiveData("Basic call")
    val vibrationPattern: LiveData<String> get() = _vibrationPattern

    private val _snoozeSettings = MutableLiveData("5 minutes, 3 times")
    val snoozeSettings: LiveData<String> get() = _snoozeSettings

    fun addAlarm(alarm: AlarmItem) {
        val currentList = _alarms.value ?: mutableListOf()
        currentList.add(alarm)
        _alarms.value = currentList
    }

    fun addAlarm(time: String) {
        val currentList = _alarms.value ?: mutableListOf()
        currentList.add(AlarmItem(time = time))
        _alarms.value = currentList
    }

    fun setAlarmName(name: String) { _alarmName.value = name }
    fun setSoundName(sound: String) { _soundName.value = sound }
    fun setVibrationPattern(pattern: String) { _vibrationPattern.value = pattern }
    fun setSnoozeSettings(settings: String) { _snoozeSettings.value = settings }

    fun setSoundEnabled(enabled: Boolean) {}
    fun setVibrationEnabled(enabled: Boolean) {}
    fun setSnoozeEnabled(enabled: Boolean) {}
}
