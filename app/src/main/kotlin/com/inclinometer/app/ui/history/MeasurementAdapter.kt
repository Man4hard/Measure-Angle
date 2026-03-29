package com.inclinometer.app.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.inclinometer.app.data.model.Measurement
import com.inclinometer.app.databinding.ItemMeasurementBinding
import java.text.SimpleDateFormat
import java.util.*

class MeasurementAdapter(
    private val onDeleteClick: (Measurement) -> Unit
) : ListAdapter<Measurement, MeasurementAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMeasurementBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemMeasurementBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(measurement: Measurement) {
            binding.tvLabel.text = measurement.label
            binding.tvMode.text = measurement.mode
            binding.tvPitch.text = "P: %.1f°".format(measurement.pitch)
            binding.tvRoll.text = "R: %.1f°".format(measurement.roll)
            binding.tvYaw.text = "Y: %.1f°".format(measurement.yaw)
            val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            binding.tvTimestamp.text = sdf.format(Date(measurement.timestamp))
            binding.btnDelete.setOnClickListener { onDeleteClick(measurement) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Measurement>() {
        override fun areItemsTheSame(oldItem: Measurement, newItem: Measurement) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Measurement, newItem: Measurement) = oldItem == newItem
    }
}
