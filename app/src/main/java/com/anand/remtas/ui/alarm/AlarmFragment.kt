package com.anand.remtas.ui.alarm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.anand.remtas.R
import com.anand.remtas.databinding.FragmentAlarmBinding
import java.text.SimpleDateFormat
import java.util.*

class AlarmFragment : Fragment() {
    private var _binding: FragmentAlarmBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: AlarmViewModel
    private lateinit var adapter: AlarmAdapter

    // Time picker state
    private var selectedHour = 7
    private var selectedMinute = 30
    private var isAM = true

    // Day toggles state
    private val selectedDays = mutableSetOf<Int>() // 0=Sunday, 1=Monday, etc.
    private val dayToggles = mutableListOf<TextView>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this).get(AlarmViewModel::class.java)
        _binding = FragmentAlarmBinding.inflate(inflater, container, false)

        setupUI()
        setupClickListeners()
        setupObservers()

        return binding.root
    }

    // ---------------- UI Setup ----------------
    private fun setupUI() {
        adapter = AlarmAdapter()

        // Initialize day toggles
        dayToggles.addAll(listOf(
            binding.sundayToggle,
            binding.mondayToggle,
            binding.tuesdayToggle,
            binding.wednesdayToggle,
            binding.thursdayToggle,
            binding.fridayToggle,
            binding.saturdayToggle
        ))

        // Default selected days (Mon-Fri)
        selectedDays.addAll(listOf(1, 2, 3, 4, 5))
        updateDayToggles()

        updateTimeDisplay()
        updateDateDisplay()
        updateAmPmButtons()
    }

    private fun setupClickListeners() {
        // Time
        binding.hoursDisplay.setOnClickListener { showTimePickerDialog(true) }
        binding.minutesDisplay.setOnClickListener { showTimePickerDialog(false) }

        // AM/PM
        binding.amButton.setOnClickListener {
            isAM = true
            updateAmPmButtons()
        }
        binding.pmButton.setOnClickListener {
            isAM = false
            updateAmPmButtons()
        }

        // Day toggles
        dayToggles.forEachIndexed { index, toggle ->
            toggle.setOnClickListener { toggleDay(index) }
        }

        // Alarm name
        binding.alarmNameDisplay.setOnClickListener { showAlarmNameDialog() }

        // Switches
        binding.soundSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setSoundEnabled(isChecked)
        }
        binding.vibrationSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setVibrationEnabled(isChecked)
        }
        binding.snoozeSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setSnoozeEnabled(isChecked)
        }

        // Action buttons
        binding.cancelButton.setOnClickListener { requireActivity().onBackPressed() }
        binding.saveButton.setOnClickListener { saveAlarm() }

        // Containers
        binding.soundToggleContainer.setOnClickListener { showSoundSelectionDialog() }
        binding.vibrationToggleContainer.setOnClickListener { showVibrationSelectionDialog() }
        binding.snoozeToggleContainer.setOnClickListener { showSnoozeSettingsDialog() }
    }

    private fun setupObservers() {
        viewModel.alarms.observe(viewLifecycleOwner) { alarms ->
            adapter.submitList(alarms.toList())
        }
        viewModel.alarmName.observe(viewLifecycleOwner) { binding.alarmNameDisplay.text = it }
        viewModel.soundName.observe(viewLifecycleOwner) { binding.soundDescription.text = it }
        viewModel.vibrationPattern.observe(viewLifecycleOwner) { binding.vibrationDescription.text = it }
        viewModel.snoozeSettings.observe(viewLifecycleOwner) { binding.snoozeDescription.text = it }
    }

    // ---------------- UI Updates ----------------
    private fun updateTimeDisplay() {
        binding.hoursDisplay.text = String.format("%02d", selectedHour)
        binding.minutesDisplay.text = String.format("%02d", selectedMinute)
    }

    private fun updateAmPmButtons() {
        if (isAM) {
            binding.amButton.background = ContextCompat.getDrawable(requireContext(), R.drawable.ampm_button_selected)
            binding.amButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            binding.pmButton.background = ContextCompat.getDrawable(requireContext(), R.drawable.ampm_button_unselected)
            binding.pmButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.secondary_text))
        } else {
            binding.pmButton.background = ContextCompat.getDrawable(requireContext(), R.drawable.ampm_button_selected)
            binding.pmButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            binding.amButton.background = ContextCompat.getDrawable(requireContext(), R.drawable.ampm_button_unselected)
            binding.amButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.secondary_text))
        }
    }

    private fun updateDayToggles() {
        dayToggles.forEachIndexed { index, toggle ->
            if (selectedDays.contains(index)) {
                toggle.background = ContextCompat.getDrawable(requireContext(), R.drawable.day_toggle_selected)
                toggle.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            } else {
                toggle.background = ContextCompat.getDrawable(requireContext(), R.drawable.day_toggle_unselected)
                toggle.setTextColor(ContextCompat.getColor(requireContext(), R.color.secondary_text))
            }
        }
    }

    private fun updateDateDisplay() {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, 1) // Tomorrow
        val dateFormat = SimpleDateFormat("EEE, dd MMM", Locale.getDefault())
        binding.dateText.text = "Tomorrow - ${dateFormat.format(calendar.time)}"
    }

    private fun toggleDay(dayIndex: Int) {
        if (selectedDays.contains(dayIndex)) selectedDays.remove(dayIndex)
        else selectedDays.add(dayIndex)
        updateDayToggles()
    }

    // ---------------- Dialogs ----------------
    private fun showTimePickerDialog(isHour: Boolean) {}
    private fun showAlarmNameDialog() {}
    private fun showSoundSelectionDialog() {}
    private fun showVibrationSelectionDialog() {}
    private fun showSnoozeSettingsDialog() {}

    // ---------------- Save Alarm ----------------
    private fun saveAlarm() {
        val hour24 = if (isAM) {
            if (selectedHour == 12) 0 else selectedHour
        } else {
            if (selectedHour == 12) 12 else selectedHour + 12
        }
        val timeString = String.format("%02d:%02d", hour24, selectedMinute)

        val alarm = AlarmItem(
            time = timeString,
            name = binding.alarmNameDisplay.text.toString(),
            selectedDays = selectedDays.toSet(),
            soundEnabled = binding.soundSwitch.isChecked,
            vibrationEnabled = binding.vibrationSwitch.isChecked,
            snoozeEnabled = binding.snoozeSwitch.isChecked
        )

        viewModel.addAlarm(alarm)
        requireActivity().onBackPressed()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
