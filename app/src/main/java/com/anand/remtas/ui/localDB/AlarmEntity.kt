package com.anand.remtas.ui.localDB

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val time: String,
    val name: String,
    val selectedDays: String, // save as comma-separated: "1,2,3"
    val soundEnabled: Boolean,
    val vibrationEnabled: Boolean,
    val snoozeEnabled: Boolean,
    val date: String? = null, // Optional date field for specific date alarms
    val isEnabled: Boolean = true // Whether the alarm is active/enabled
)