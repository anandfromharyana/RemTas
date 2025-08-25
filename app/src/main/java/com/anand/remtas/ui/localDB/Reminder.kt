package com.anand.remtas.ui.localDB

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String,
    val dateTime: Date,
    val isCompleted: Boolean = false,
    val priority: String = "Medium", // High, Medium, Low
    val category: String = "General",
    val createdAt: Date = Date()
)