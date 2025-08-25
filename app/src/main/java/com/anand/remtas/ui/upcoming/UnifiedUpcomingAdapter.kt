package com.anand.remtas.ui.upcoming

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.anand.remtas.databinding.ItemAlarmBinding
import com.anand.remtas.databinding.ItemReminderBinding
import java.text.SimpleDateFormat
import java.util.*

class UnifiedUpcomingAdapter(
    private val onAlarmToggle: (UpcomingItem.AlarmItem, Boolean) -> Unit,
    private val onAlarmClick: (UpcomingItem.AlarmItem) -> Unit,
    private val onReminderClick: (UpcomingItem.ReminderItem) -> Unit,
    private val onReminderChecked: (UpcomingItem.ReminderItem, Boolean) -> Unit
) : ListAdapter<UpcomingItem, RecyclerView.ViewHolder>(UpcomingItemDiffCallback()) {

    companion object {
        private const val VIEW_TYPE_ALARM = 0
        private const val VIEW_TYPE_REMINDER = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is UpcomingItem.AlarmItem -> VIEW_TYPE_ALARM
            is UpcomingItem.ReminderItem -> VIEW_TYPE_REMINDER
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_ALARM -> {
                val binding = ItemAlarmBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                AlarmViewHolder(binding)
            }
            VIEW_TYPE_REMINDER -> {
                val binding = ItemReminderBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                ReminderViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is UpcomingItem.AlarmItem -> (holder as AlarmViewHolder).bind(item)
            is UpcomingItem.ReminderItem -> (holder as ReminderViewHolder).bind(item)
        }
    }

    inner class AlarmViewHolder(private val binding: ItemAlarmBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(alarmItem: UpcomingItem.AlarmItem) {
            val alarm = alarmItem.alarm
            binding.apply {
                textAlarmTime.text = alarm.time
                textAlarmName.text = alarm.name
                textAlarmDays.text = if (alarm.selectedDays.isNotEmpty()) {
                    "Days: ${alarm.selectedDays}"
                } else {
                    "Date: ${alarm.date}"
                }

                alarmToggle.isChecked = alarm.isEnabled
                textSound.text = "Sound: ${if (alarm.soundEnabled) "On" else "Off"}"
                textVibration.text = "Vibration: ${if (alarm.vibrationEnabled) "On" else "Off"}"
                textSnooze.text = "Snooze: ${if (alarm.snoozeEnabled) "On" else "Off"}"

                alarmToggle.setOnCheckedChangeListener { _, isChecked ->
                    onAlarmToggle(alarmItem, isChecked)
                }

                root.setOnClickListener {
                    onAlarmClick(alarmItem)
                }
            }
        }
    }

    inner class ReminderViewHolder(private val binding: ItemReminderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val dateFormatter = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())

        fun bind(reminderItem: UpcomingItem.ReminderItem) {
            val reminder = reminderItem.reminder
            binding.apply {
                tvTitle.text = reminder.title
                tvDescription.text = reminder.description
                tvDateTime.text = dateFormatter.format(reminder.dateTime)
                tvCategory.text = reminder.category
                tvPriority.text = reminder.priority
                cbCompleted.isChecked = reminder.isCompleted

                // Show/hide description
                if (reminder.description.isBlank()) {
                    tvDescription.visibility = View.GONE
                } else {
                    tvDescription.visibility = View.VISIBLE
                }

                // Set priority indicator color
                when (reminder.priority) {
                    "High" -> {
                        priorityIndicator.setBackgroundColor(
                            binding.root.context.getColor(android.R.color.holo_red_light)
                        )
                    }
                    "Medium" -> {
                        priorityIndicator.setBackgroundColor(
                            binding.root.context.getColor(android.R.color.holo_orange_light)
                        )
                    }
                    "Low" -> {
                        priorityIndicator.setBackgroundColor(
                            binding.root.context.getColor(android.R.color.holo_green_light)
                        )
                    }
                }

                root.setOnClickListener {
                    onReminderClick(reminderItem)
                }

                cbCompleted.setOnCheckedChangeListener { _, isChecked ->
                    onReminderChecked(reminderItem, isChecked)
                }
            }
        }
    }

    class UpcomingItemDiffCallback : DiffUtil.ItemCallback<UpcomingItem>() {
        override fun areItemsTheSame(oldItem: UpcomingItem, newItem: UpcomingItem): Boolean {
            return when {
                oldItem is UpcomingItem.AlarmItem && newItem is UpcomingItem.AlarmItem ->
                    oldItem.alarm.id == newItem.alarm.id
                oldItem is UpcomingItem.ReminderItem && newItem is UpcomingItem.ReminderItem ->
                    oldItem.reminder.id == newItem.reminder.id
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: UpcomingItem, newItem: UpcomingItem): Boolean {
            return when {
                oldItem is UpcomingItem.AlarmItem && newItem is UpcomingItem.AlarmItem ->
                    oldItem.alarm == newItem.alarm
                oldItem is UpcomingItem.ReminderItem && newItem is UpcomingItem.ReminderItem ->
                    oldItem.reminder == newItem.reminder
                else -> false
            }
        }
    }
}