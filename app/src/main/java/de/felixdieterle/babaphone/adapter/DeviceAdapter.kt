package de.felixdieterle.babaphone.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.felixdieterle.babaphone.R
import de.felixdieterle.babaphone.network.DeviceInfo

class DeviceAdapter(
    private val devices: MutableList<DeviceInfo>,
    private val onDeviceClick: (DeviceInfo) -> Unit
) : RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder>() {

    class DeviceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val deviceName: TextView = view.findViewById(R.id.deviceName)
        val deviceAddress: TextView = view.findViewById(R.id.deviceAddress)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_device, parent, false)
        return DeviceViewHolder(view)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        val device = devices[position]
        holder.deviceName.text = device.name
        holder.deviceAddress.text = "${device.address}:${device.port}"
        holder.itemView.setOnClickListener {
            onDeviceClick(device)
        }
    }

    override fun getItemCount() = devices.size

    fun addDevice(device: DeviceInfo) {
        if (!devices.any { it.name == device.name }) {
            devices.add(device)
            notifyItemInserted(devices.size - 1)
        }
    }

    fun removeDevice(device: DeviceInfo) {
        val index = devices.indexOfFirst { it.name == device.name }
        if (index != -1) {
            devices.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    fun clear() {
        devices.clear()
        notifyDataSetChanged()
    }
}
