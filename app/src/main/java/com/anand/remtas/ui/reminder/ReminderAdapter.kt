package com.anand.remtas.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.anand.remtas.R

import com.anand.remtas.databinding.ItemReminderBinding
import com.anand.remtas.ui.localDB.Reminder
import java.text.SimpleDateFormat
import java.util.*

class ReminderAdapter(
    private val onReminderClick: (Reminder) -> Unit,
    private val onReminderChecked: (Reminder, Boolean) -> Unit
) : ListAdapter<Reminder, ReminderAdapter.ReminderViewHolder>(ReminderDiffCallback()) {

    private val dateFormatter = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderViewHolder {
        val binding = ItemReminderBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ReminderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ReminderViewHolder(private val binding: ItemReminderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(reminder: Reminder) {
            binding.apply {
                tvTitle.text = reminder.title
                tvDescription.text = reminder.description
                tvDateTime.text = dateFormatter.format(reminder.dateTime)
                tvCategory.text = reminder.category
                tvPriority.text = reminder.priority
                cbCompleted.isChecked = reminder.isCompleted

                // Show/hide description based on content
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

                // Set click listeners
                root.setOnClickListener {
                    onReminderClick(reminder)
                }

                cbCompleted.setOnCheckedChangeListener { _, isChecked ->
                    onReminderChecked(reminder, isChecked)
                }
            }
        }
    }

    class ReminderDiffCallback : DiffUtil.ItemCallback<Reminder>() {
        override fun areItemsTheSame(oldItem: Reminder, newItem: Reminder): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Reminder, newItem: Reminder): Boolean {
            return oldItem == newItem
        }
    }
}