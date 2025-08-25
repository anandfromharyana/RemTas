package com.anand.remtas.ui.upcoming

 import com.anand.remtas.ui.localDB.AlarmEntity
 import com.anand.remtas.ui.localDB.Reminder
 import java.util.Date

sealed class UpcomingItem {
    abstract val id: Int
    abstract val title: String
    abstract val time: String
    abstract val isEnabled: Boolean
    abstract val sortTime: Long

    data class AlarmItem(
        val alarm: AlarmEntity,
        override val id: Int = alarm.id,
        override val title: String = alarm.name,
        override val time: String = alarm.time,
        override val isEnabled: Boolean = alarm.isEnabled,
        override val sortTime: Long = parseTimeToMillis(alarm.time)
    ) : UpcomingItem()

    data class ReminderItem(
        val reminder: Reminder,
        override val id: Int = reminder.id,
        override val title: String = reminder.title,
        override val time: String = formatReminderTime(reminder.dateTime),
        override val isEnabled: Boolean = !reminder.isCompleted,
        override val sortTime: Long = reminder.dateTime.time
    ) : UpcomingItem()

    companion object {
        private fun parseTimeToMillis(timeString: String): Long {
            val parts = timeString.split(":")
            val hour = parts[0].toIntOrNull() ?: 0
            val minute = parts[1].toIntOrNull() ?: 0
            return (hour * 60L + minute) * 60000L // Convert to milliseconds for today
        }

        private fun formatReminderTime(date: Date): String {
            val calendar = java.util.Calendar.getInstance()
            calendar.time = date
            return String.format("%02d:%02d",
                calendar.get(java.util.Calendar.HOUR_OF_DAY),
                calendar.get(java.util.Calendar.MINUTE)
            )
        }
    }
}