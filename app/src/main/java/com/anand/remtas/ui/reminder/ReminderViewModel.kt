package com.anand.remtas.ui.reminder

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.anand.remtas.ui.localDB.AppDatabase
import com.anand.remtas.ui.localDB.Reminder
import com.anand.remtas.ui.localDB.ReminderRepository
import kotlinx.coroutines.launch
import java.util.Date


class ReminderViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ReminderRepository

    private val _reminderSaved = MutableLiveData<Boolean>()
    val reminderSaved: LiveData<Boolean> = _reminderSaved

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    init {
        val reminderDao = AppDatabase.getDatabase(application).reminderDao()
        repository = ReminderRepository(reminderDao)
    }

    fun saveReminder(
        title: String,
        description: String,
        dateTime: Date,
        priority: String,
        category: String
    ) {
        if (title.isBlank()) {
            _errorMessage.value = "Please enter a title for the reminder"
            return
        }

        if (dateTime.before(Date())) {
            _errorMessage.value = "Please select a future date and time"
            return
        }

        viewModelScope.launch {
            try {
                val reminder = Reminder(
                    title = title.trim(),
                    description = description.trim(),
                    dateTime = dateTime,
                    priority = priority,
                    category = category.trim().ifEmpty { "General" }
                )

                repository.insertReminder(reminder)
                _reminderSaved.value = true
            } catch (e: Exception) {
                _errorMessage.value = "Failed to save reminder: ${e.message}"
            }
        }
    }

    fun resetSaveStatus() {
        _reminderSaved.value = false
    }

    fun clearErrorMessage() {
        _errorMessage.value = ""
    }
}