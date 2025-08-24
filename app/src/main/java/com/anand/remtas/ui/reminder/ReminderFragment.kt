package com.anand.remtas.ui.reminder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.anand.remtas.databinding.FragmentAlarmBinding
import com.anand.remtas.databinding.FragmentReminderBinding

class ReminderFragment : Fragment() {

    private var _binding: FragmentReminderBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val reminderViewModel =
            ViewModelProvider(this).get(ReminderViewModel::class.java)

        _binding = FragmentReminderBinding.inflate(inflater, container, false)
        val root: View = binding.root

//        val textView: TextView = binding
//        reminderViewModel.text.observe(viewLifecycleOwner) {
//            textView.text = it
//        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}