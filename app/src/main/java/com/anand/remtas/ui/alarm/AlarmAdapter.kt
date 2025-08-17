package com.anand.remtas.ui.alarm

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anand.remtas.databinding.ItemAlarmBinding

class AlarmAdapter : RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder>() {
    private var items: List<AlarmItem> = emptyList()

    fun submitList(newList: List<AlarmItem>) {
        items = newList
        notifyDataSetChanged()
    }

    class AlarmViewHolder(val binding: ItemAlarmBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {
        val binding = ItemAlarmBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AlarmViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
        val item = items[position]
        holder.binding.textAlarmTime.text = item.time
        // Extend binding here if needed
    }

    override fun getItemCount() = items.size
}
