package com.anand.remtas.ui.alarm

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.anand.remtas.R
import com.anand.remtas.databinding.FragmentAlarmBinding
import com.anand.remtas.ui.localDB.AlarmEntity
 import java.text.SimpleDateFormat
import java.util.*

class AlarmFragment : Fragment() {
    private var _binding: FragmentAlarmBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: AlarmViewModel
    private lateinit var adapter: AlarmAdapter

    // Editing mode
    private var isEditMode = false
    private var editingAlarmId = 0

    // Time picker state
    private var selectedHour = 7
    private var selectedMinute = 30
    private var isAM = true

    // Date picker state
    private var selectedDate = Calendar.getInstance()

    // Day toggles state
    private val selectedDays = mutableSetOf<Int>() // 0=Sunday, 1=Monday, etc.
    private val dayToggles = mutableListOf<TextView>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(requireActivity())[AlarmViewModel::class.java]
        _binding = FragmentAlarmBinding.inflate(inflater, container, false)

        setupUI()
        setupClickListeners()
        setupObservers()

        // Check if we're in edit mode
        arguments?.let { args ->
            isEditMode = args.getInt("alarm_id", 0) != 0
            if (isEditMode) {
                loadExistingAlarmData(args)
            }
        }

        return binding.root
    }

    // ---------------- UI Setup ----------------
    private fun setupUI() {
        adapter = AlarmAdapter()

        // Initialize day toggles
        dayToggles.addAll(
            listOf(
                binding.sundayToggle,
                binding.mondayToggle,
                binding.tuesdayToggle,
                binding.wednesdayToggle,
                binding.thursdayToggle,
                binding.fridayToggle,
                binding.saturdayToggle
            )
        )

        // Default selected days (Mon-Fri)
        selectedDays.addAll(listOf(1, 2, 3, 4, 5))
        updateDayToggles()

        // Set default date to tomorrow
        selectedDate.add(Calendar.DAY_OF_MONTH, 1)

        updateTimeDisplay()
        updateDateDisplay()
        updateAmPmButtons()
        setupTimeEditTextListeners()
    }

    private fun setupClickListeners() {
        // Time EditTexts - Handle focus and text changes
        binding.hoursDisplay.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                validateAndUpdateHour()
            }
        }

        binding.minutesDisplay.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                validateAndUpdateMinute()
            }
        }

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

        // Date picker - Calendar icon and date text clickable
        binding.calendarIcon.setOnClickListener { showDatePickerDialog() }
        binding.dateText.setOnClickListener { showDatePickerDialog() }

        // Alarm name
        binding.alarmNameDisplay.setOnClickListener { showAlarmNameDialog() }

        // Switches
        binding.soundSwitch.setOnCheckedChangeListener { _, isChecked ->
            // update in ViewModel if needed
        }
        binding.vibrationSwitch.setOnCheckedChangeListener { _, isChecked ->
            // update in ViewModel if needed
        }
        binding.snoozeSwitch.setOnCheckedChangeListener { _, isChecked ->
            // update in ViewModel if needed
        }

        // Action buttons
        binding.cancelButton.setOnClickListener { requireActivity().onBackPressed() }
        binding.saveButton.setOnClickListener { saveAlarm() }

        // Containers (future dialogs for sound, vibration, snooze)
        binding.soundToggleContainer.setOnClickListener { showSoundSelectionDialog() }
        binding.vibrationToggleContainer.setOnClickListener { showVibrationSelectionDialog() }
        binding.snoozeToggleContainer.setOnClickListener { showSnoozeSettingsDialog() }
    }

    private fun setupTimeEditTextListeners() {
        // Hour EditText TextWatcher
        binding.hoursDisplay.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                s?.toString()?.let { text ->
                    if (text.isNotEmpty() && text.length <= 2) {
                        try {
                            val hour = text.toInt()
                            if (hour in 1..12) {
                                selectedHour = hour
                            }
                        } catch (e: NumberFormatException) {
                            // Handle invalid input
                        }
                    }
                }
            }
        })

        // Minute EditText TextWatcher
        binding.minutesDisplay.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                s?.toString()?.let { text ->
                    if (text.isNotEmpty() && text.length <= 2) {
                        try {
                            val minute = text.toInt()
                            if (minute in 0..59) {
                                selectedMinute = minute
                            }
                        } catch (e: NumberFormatException) {
                            // Handle invalid input
                        }
                    }
                }
            }
        })
    }

    private fun setupObservers() {
        viewModel.alarms.observe(viewLifecycleOwner) { alarms ->
            adapter.submitList(alarms.toList())
        }
    }

    // ---------------- UI Updates ----------------
    private fun updateTimeDisplay() {
        binding.hoursDisplay.setText(String.format("%02d", selectedHour))
        binding.minutesDisplay.setText(String.format("%02d", selectedMinute))
    }

    private fun validateAndUpdateHour() {
        val text = binding.hoursDisplay.text.toString()
        if (text.isEmpty()) {
            selectedHour = 7
            binding.hoursDisplay.setText("07")
            return
        }

        try {
            val hour = text.toInt()
            when {
                hour < 1 -> {
                    selectedHour = 1
                    binding.hoursDisplay.setText("01")
                }
                hour > 12 -> {
                    selectedHour = 12
                    binding.hoursDisplay.setText("12")
                }
                else -> {
                    selectedHour = hour
                    binding.hoursDisplay.setText(String.format("%02d", hour))
                }
            }
        } catch (e: NumberFormatException) {
            selectedHour = 7
            binding.hoursDisplay.setText("07")
        }
    }

    private fun validateAndUpdateMinute() {
        val text = binding.minutesDisplay.text.toString()
        if (text.isEmpty()) {
            selectedMinute = 30
            binding.minutesDisplay.setText("30")
            return
        }

        try {
            val minute = text.toInt()
            when {
                minute < 0 -> {
                    selectedMinute = 0
                    binding.minutesDisplay.setText("00")
                }
                minute > 59 -> {
                    selectedMinute = 59
                    binding.minutesDisplay.setText("59")
                }
                else -> {
                    selectedMinute = minute
                    binding.minutesDisplay.setText(String.format("%02d", minute))
                }
            }
        } catch (e: NumberFormatException) {
            selectedMinute = 30
            binding.minutesDisplay.setText("30")
        }
    }

    private fun updateAmPmButtons() {
        if (isAM) {
            binding.amButton.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.ampm_button_selected)
            binding.amButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            binding.pmButton.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.ampm_button_unselected)
            binding.pmButton.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.secondary_text
                )
            )
        } else {
            binding.pmButton.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.ampm_button_selected)
            binding.pmButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            binding.amButton.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.ampm_button_unselected)
            binding.amButton.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.secondary_text
                )
            )
        }
    }

    private fun updateDayToggles() {
        dayToggles.forEachIndexed { index, toggle ->
            if (selectedDays.contains(index)) {
                toggle.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.day_toggle_selected)
                toggle.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            } else {
                toggle.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.day_toggle_unselected)
                toggle.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.secondary_text
                    )
                )
            }
        }
    }

    private fun updateDateDisplay() {
        val dateFormat = SimpleDateFormat("EEE, dd MMM", Locale.getDefault())
        val today = Calendar.getInstance()

        when {
            isSameDay(selectedDate, today) -> {
                binding.dateText.text = "Today - ${dateFormat.format(selectedDate.time)}"
            }
            isTomorrow(selectedDate, today) -> {
                binding.dateText.text = "Tomorrow - ${dateFormat.format(selectedDate.time)}"
            }
            else -> {
                binding.dateText.text = dateFormat.format(selectedDate.time)
            }
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

    private fun toggleDay(dayIndex: Int) {
        if (selectedDays.contains(dayIndex)) selectedDays.remove(dayIndex)
        else selectedDays.add(dayIndex)
        updateDayToggles()
    }

    // ---------------- Dialogs (placeholders) ----------------
    private fun showDatePickerDialog() {
        val datePickerDialog = android.app.DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                selectedDate.set(Calendar.YEAR, year)
                selectedDate.set(Calendar.MONTH, month)
                selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                updateDateDisplay()
            },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH)
        )

        // Set minimum date to today (can't set alarms for past dates)
        datePickerDialog.datePicker.minDate = System.currentTimeMillis()

        datePickerDialog.show()
    }

    private fun showTimePickerDialog(isHour: Boolean) {}
    private fun showAlarmNameDialog() {}
    private fun showSoundSelectionDialog() {}
    private fun showVibrationSelectionDialog() {}
    private fun showSnoozeSettingsDialog() {}

    // ---------------- Save Alarm ----------------
    private fun loadExistingAlarmData(args: Bundle) {
        editingAlarmId = args.getInt("alarm_id", 0)

        // Load time
        val timeString = args.getString("alarm_time", "07:30")
        val timeParts = timeString.split(":")
        val hour24 = timeParts[0].toIntOrNull() ?: 7
        val minute = timeParts[1].toIntOrNull() ?: 30

        // Convert 24h to 12h format
        isAM = hour24 < 12
        selectedHour = when {
            hour24 == 0 -> 12
            hour24 > 12 -> hour24 - 12
            else -> hour24
        }
        selectedMinute = minute

        // Load name
        binding.alarmNameDisplay.setText(args.getString("alarm_name", "Alarm"))

        // Load selected days
        selectedDays.clear()
        val daysString = args.getString("alarm_days", "")
        if (daysString.isNotEmpty()) {
            selectedDays.addAll(
                daysString.split(",").mapNotNull { it.toIntOrNull() }
            )
        }

        // Load toggles
        binding.soundSwitch.isChecked = args.getBoolean("alarm_sound", true)
        binding.vibrationSwitch.isChecked = args.getBoolean("alarm_vibration", true)
        binding.snoozeSwitch.isChecked = args.getBoolean("alarm_snooze", true)

        // Load date if exists
        args.getString("alarm_date")?.let { dateString ->
            try {
                val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                selectedDate.time = dateFormat.parse(dateString) ?: Date()
            } catch (e: Exception) {
                // Use default date if parsing fails
            }
        }

        // Update UI
        updateTimeDisplay()
        updateAmPmButtons()
        updateDayToggles()
        updateDateDisplay()

        // Update button text
        binding.saveButton.text = "Update"
    }

    private fun saveAlarm() {
        // Validate time inputs before saving
        validateAndUpdateHour()
        validateAndUpdateMinute()

        val hour24 = if (isAM) {
            if (selectedHour == 12) 0 else selectedHour
        } else {
            if (selectedHour == 12) 12 else selectedHour + 12
        }
        val timeString = String.format("%02d:%02d", hour24, selectedMinute)

        val alarm = AlarmEntity(
            id = if (isEditMode) editingAlarmId else 0,
            time = timeString,
            name = binding.alarmNameDisplay.text.toString(),
            selectedDays = selectedDays.joinToString(","), // stored as comma string
            soundEnabled = binding.soundSwitch.isChecked,
            vibrationEnabled = binding.vibrationSwitch.isChecked,
            snoozeEnabled = binding.snoozeSwitch.isChecked,
            date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedDate.time),
            isEnabled = true
        )

        if (isEditMode) {
            viewModel.updateAlarm(alarm)
        } else {
            viewModel.addAlarm(alarm)
        }
        requireActivity().onBackPressed()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}