package com.anand.remtas.ui.upcoming

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.anand.remtas.R
import com.anand.remtas.databinding.FragmentUpcomingBinding
import com.anand.remtas.ui.alarm.AlarmAdapter
import com.anand.remtas.ui.alarm.AlarmFragment
import com.anand.remtas.ui.localDB.AlarmEntity

class UpcomingFragment : Fragment() {

    private var _binding: FragmentUpcomingBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: UpcomingViewModel
    private lateinit var adapter: AlarmAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application))
            .get(UpcomingViewModel::class.java)

        _binding = FragmentUpcomingBinding.inflate(inflater, container, false)

        setupRecyclerView()
        setupClickListeners()
        observeAlarms()

        return binding.root
    }

    private fun setupRecyclerView() {
        adapter = AlarmAdapter()

        // Set up click listeners for the adapter
        adapter.setOnAlarmToggleListener { alarm, isEnabled ->
            viewModel.toggleAlarm(alarm, isEnabled)
        }

        adapter.setOnAlarmClickListener { alarm ->
            editAlarm(alarm)
        }

        binding.alarmsRecyclerView.apply {
            this.adapter = this@UpcomingFragment.adapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        // Add swipe to delete functionality
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
                val alarm = adapter.getCurrentList()[position]
                showDeleteConfirmationDialog(alarm)
            }
        })

        itemTouchHelper.attachToRecyclerView(binding.alarmsRecyclerView)
    }

    private fun setupClickListeners() {
        // Add alarm button in header
        binding.addAlarmButton.setOnClickListener {
            navigateToAlarmFragment()
        }

        // Floating action button (if you want to use it instead)
        binding.fabAddAlarm.setOnClickListener {
            navigateToAlarmFragment()
        }
    }

    private fun observeAlarms() {
        viewModel.upcomingAlarms.observe(viewLifecycleOwner) { alarms ->
            adapter.submitList(alarms)
            updateEmptyState(alarms.isEmpty())
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
        // Navigate to AlarmFragment to create new alarm
        val alarmFragment = AlarmFragment()
        parentFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment_activity_main, alarmFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun editAlarm(alarm: AlarmEntity) {
        // Navigate to AlarmFragment with existing alarm data for editing
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

    private fun showDeleteConfirmationDialog(alarm: AlarmEntity) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Alarm")
            .setMessage("Are you sure you want to delete the alarm \"${alarm.name}\"?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteAlarm(alarm)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                // Refresh the list to restore the swiped item
                adapter.notifyDataSetChanged()
            }
            .setOnCancelListener {
                // Refresh the list to restore the swiped item
                adapter.notifyDataSetChanged()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}