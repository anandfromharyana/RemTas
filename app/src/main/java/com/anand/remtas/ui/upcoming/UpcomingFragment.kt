package com.anand.remtas.ui.upcoming

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anand.remtas.R
import com.anand.remtas.databinding.FragmentUpcomingBinding
import com.anand.remtas.ui.alarm.AlarmFragment
import com.anand.remtas.ui.reminder.ReminderFragment

class UpcomingFragment : Fragment() {

    private var _binding: FragmentUpcomingBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: UpcomingViewModel
    private lateinit var adapter: UnifiedUpcomingAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
        ).get(UpcomingViewModel::class.java)

        _binding = FragmentUpcomingBinding.inflate(inflater, container, false)

        setupRecyclerView()
        setupClickListeners()
        observeUpcomingItems()

        return binding.root
    }

    private fun setupRecyclerView() {
        adapter = UnifiedUpcomingAdapter(
            onAlarmToggle = { alarmItem, isEnabled ->
                viewModel.toggleAlarm(alarmItem.alarm, isEnabled)
            },
            onAlarmClick = { alarmItem ->
                editAlarm(alarmItem.alarm)
            },
            onReminderClick = { reminderItem ->
                editReminder(reminderItem)
            },
            onReminderChecked = { reminderItem, isCompleted ->
                viewModel.updateReminderStatus(reminderItem.reminder, isCompleted)
                if (isCompleted) {
                    Toast.makeText(context, "Reminder completed!", Toast.LENGTH_SHORT).show()
                }
            }
        )

        binding.alarmsRecyclerView.apply {
            this.adapter = this@UpcomingFragment.adapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        setupSwipeToDelete()
    }

    private fun setupSwipeToDelete() {
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val item = adapter.currentList[position]

                when (item) {
                    is UpcomingItem.AlarmItem -> showDeleteAlarmConfirmationDialog(item)
                    is UpcomingItem.ReminderItem -> showDeleteReminderConfirmationDialog(item)
                }
            }
        })

        itemTouchHelper.attachToRecyclerView(binding.alarmsRecyclerView)
    }

    private fun setupClickListeners() {
        binding.fabAddAlarm.setOnClickListener {
            showCreateOptionsDialog()
        }
    }

    private fun showCreateOptionsDialog() {
        val options = arrayOf("Create Alarm", "Create Reminder")
        AlertDialog.Builder(requireContext())
            .setTitle("What would you like to create?")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> navigateToAlarmFragment()
                    1 -> navigateToReminderFragment()
                }
            }
            .show()
    }

    private fun observeUpcomingItems() {
        viewModel.upcomingItems.observe(viewLifecycleOwner) { items ->
            adapter.submitList(items)
            updateEmptyState(items.isEmpty())
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            binding.emptyState.visibility = View.VISIBLE
            binding.alarmsRecyclerView.visibility = View.GONE
        } else {
            binding.emptyState.visibility = View.GONE
            binding.alarmsRecyclerView.visibility = View.VISIBLE
        }
    }

    private fun navigateToAlarmFragment() {
        val alarmFragment = AlarmFragment()
        parentFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment_activity_main, alarmFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun navigateToReminderFragment() {
        val reminderFragment = ReminderFragment()
        parentFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment_activity_main, reminderFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun editAlarm(alarm: com.anand.remtas.ui.localDB.AlarmEntity) {
        val alarmFragment = AlarmFragment().apply {
            arguments = Bundle().apply {
                putInt("alarm_id", alarm.id)
                putString("alarm_time", alarm.time)
                putString("alarm_name", alarm.name)
                putString("alarm_days", alarm.selectedDays)
                putBoolean("alarm_sound", alarm.soundEnabled)
                putBoolean("alarm_vibration", alarm.vibrationEnabled)
                putBoolean("alarm_snooze", alarm.snoozeEnabled)
                putString("alarm_date", alarm.date)
                putBoolean("alarm_enabled", alarm.isEnabled)
            }
        }

        parentFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment_activity_main, alarmFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun editReminder(reminderItem: UpcomingItem.ReminderItem) {
        // For now, just show details. You can implement edit functionality later
        val reminder = reminderItem.reminder
        Toast.makeText(
            context,
            "Reminder: ${reminder.title}\nTime: ${java.text.SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", java.util.Locale.getDefault()).format(reminder.dateTime)}",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun showDeleteAlarmConfirmationDialog(alarmItem: UpcomingItem.AlarmItem) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Alarm")
            .setMessage("Are you sure you want to delete the alarm \"${alarmItem.alarm.name}\"?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteAlarm(alarmItem.alarm)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                adapter.notifyDataSetChanged()
            }
            .setOnCancelListener {
                adapter.notifyDataSetChanged()
            }
            .show()
    }

    private fun showDeleteReminderConfirmationDialog(reminderItem: UpcomingItem.ReminderItem) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Reminder")
            .setMessage("Are you sure you want to delete the reminder \"${reminderItem.reminder.title}\"?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteReminder(reminderItem.reminder)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                adapter.notifyDataSetChanged()
            }
            .setOnCancelListener {
                adapter.notifyDataSetChanged()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}