package com.anand.remtas.ui.alarm

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anand.remtas.databinding.ItemAlarmBinding
import com.anand.remtas.ui.localDB.AlarmEntity
import java.text.SimpleDateFormat
import java.util.*

class AlarmAdapter : RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder>() {

    private var items: List<AlarmEntity> = emptyList()
    private var onAlarmToggleListener: ((AlarmEntity, Boolean) -> Unit)? = null
    private var onAlarmClickListener: ((AlarmEntity) -> Unit)? = null

    fun submitList(newList: List<AlarmEntity>) {
        items = newList
        notifyDataSetChanged()
    }

    fun getCurrentList(): List<AlarmEntity> = items

    fun setOnAlarmToggleListener(listener: (AlarmEntity, Boolean) -> Unit) {
        onAlarmToggleListener = listener
    }

    fun setOnAlarmClickListener(listener: (AlarmEntity) -> Unit) {
        onAlarmClickListener = listener
    }

    class AlarmViewHolder(val binding: ItemAlarmBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {
        val binding = ItemAlarmBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AlarmViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
        val item = items[position]

        // Show alarm time & name
        holder.binding.textAlarmTime.text = formatTime(item.time)
        holder.binding.textAlarmName.text = item.name

        // Show selected days or specific date
        val daysText = if (item.date != null) {
            // Show specific date
            try {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val displayFormat = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault())
                val parsedDate = dateFormat.parse(item.date)

                val today = Calendar.getInstance()
                val alarmDate = Calendar.getInstance()
                alarmDate.time = parsedDate ?: Date()

                when {
                    isSameDay(alarmDate, today) -> "Today"
                    isTomorrow(alarmDate, today) -> "Tomorrow"
                    else -> displayFormat.format(parsedDate ?: Date())
                }
            } catch (e: Exception) {
                item.date
            }
        } else if (item.selectedDays.isNotEmpty()) {
            // Show recurring days
            formatSelectedDays(item.selectedDays)
        } else {
            "One-time"
        }

        holder.binding.textAlarmDays.text = daysText

        // Show status of toggles
        holder.binding.textSound.text = if (item.soundEnabled) "Sound: On" else "Sound: Off"
        holder.binding.textVibration.text = if (item.vibrationEnabled) "Vibration: On" else "Vibration: Off"
        holder.binding.textSnooze.text = if (item.snoozeEnabled) "Snooze: On" else "Snooze: Off"

        // Set alarm toggle state
        holder.binding.alarmToggle.isChecked = item.isEnabled

        // Handle toggle clicks
        holder.binding.alarmToggle.setOnCheckedChangeListener { _, isChecked ->
            onAlarmToggleListener?.invoke(item, isChecked)
        }

        // Handle item clicks
        holder.itemView.setOnClickListener {
            onAlarmClickListener?.invoke(item)
        }
    }

    private fun formatTime(time24: String): String {
        try {
            val parts = time24.split(":")
            val hour = parts[0].toInt()
            val minute = parts[1].toInt()

            val hour12 = when {
                hour == 0 -> 12
                hour > 12 -> hour - 12
                else -> hour
            }

            val amPm = if (hour < 12) "AM" else "PM"

            return String.format("%d:%02d %s", hour12, minute, amPm)
        } catch (e: Exception) {
            return time24
        }
    }

    private fun formatSelectedDays(selectedDays: String): String {
        if (selectedDays.isEmpty()) return "None"

        val dayMap = mapOf(
            0 to "Sun", 1 to "Mon", 2 to "Tue", 3 to "Wed",
            4 to "Thu", 5 to "Fri", 6 to "Sat"
        )

        val days = selectedDays.split(",")
            .mapNotNull { it.toIntOrNull() }
            .sorted()

        return when {
            days.size == 7 -> "Everyday"
            days == listOf(1, 2, 3, 4, 5) -> "Weekdays"
            days == listOf(0, 6) -> "Weekends"
            else -> days.mapNotNull { dayMap[it] }.joinToString(", ")
        }
    }

    private fun isSameDay(date1: Calendar, date2: Calendar): Boolean {
        return date1.get(Calendar.YEAR) == date2.get(Calendar.YEAR) &&
                date1.get(Calendar.DAY_OF_YEAR) == date2.get(Calendar.DAY_OF_YEAR)
    }

    private fun isTomorrow(date1: Calendar, date2: Calendar): Boolean {
        val tomorrow = date2.clone() as Calendar
        tomorrow.add(Calendar.DAY_OF_MONTH, 1)
        return isSameDay(date1, tomorrow)
    }

    override fun getItemCount() = items.size
}
