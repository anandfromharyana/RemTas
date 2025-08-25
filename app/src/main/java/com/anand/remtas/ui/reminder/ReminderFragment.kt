package com.anand.remtas.ui.reminder

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.anand.remtas.databinding.FragmentReminderBinding
import java.text.SimpleDateFormat
import java.util.*

class ReminderFragment : Fragment() {

    private var _binding: FragmentReminderBinding? = null
    private val binding get() = _binding!!

    private lateinit var reminderViewModel: ReminderViewModel
    private var selectedDateTime: Date? = null
    private val dateFormatter = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        reminderViewModel = ViewModelProvider(this)[ReminderViewModel::class.java]
        _binding = FragmentReminderBinding.inflate(inflater, container, false)

        setupUI()
        observeViewModel()

        return binding.root
    }

    private fun setupUI() {
        binding.btnSelectDateTime.setOnClickListener {
            showDateTimePicker()
        }

        binding.btnSaveReminder.setOnClickListener {
            saveReminder()
        }
    }

    private fun observeViewModel() {
        reminderViewModel.reminderSaved.observe(viewLifecycleOwner) { saved ->
            if (saved) {
                Toast.makeText(context, "Reminder saved successfully!", Toast.LENGTH_SHORT).show()
                clearForm()
                reminderViewModel.resetSaveStatus()
            }
        }

        reminderViewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (message.isNotEmpty()) {
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                reminderViewModel.clearErrorMessage()
            }
        }
    }

    private fun showDateTimePicker() {
        val calendar = Calendar.getInstance()

        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                TimePickerDialog(
                    requireContext(),
                    { _, hourOfDay, minute ->
                        calendar.set(year, month, dayOfMonth, hourOfDay, minute, 0)
                        selectedDateTime = calendar.time
                        binding.tvSelectedDateTime.text = dateFormatter.format(selectedDateTime!!)
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    false
                ).show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.minDate = System.currentTimeMillis()
        }.show()
    }

    private fun saveReminder() {
        val title = binding.etReminderTitle.text.toString()
        val description = binding.etReminderDescription.text.toString()
        val category = binding.etCategory.text.toString()

        val priority = when (binding.rgPriority.checkedRadioButtonId) {
            binding.rbHigh.id -> "High"
            binding.rbLow.id -> "Low"
            else -> "Medium"
        }

        if (selectedDateTime == null) {
            Toast.makeText(context, "Please select a date and time", Toast.LENGTH_SHORT).show()
            return
        }

        reminderViewModel.saveReminder(title, description, selectedDateTime!!, priority, category)
    }

    private fun clearForm() {
        binding.etReminderTitle.text?.clear()
        binding.etReminderDescription.text?.clear()
        binding.etCategory.setText("General")
        binding.rbMedium.isChecked = true
        selectedDateTime = null
        binding.tvSelectedDateTime.text = "No date selected"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}