package com.bluetooth.bluetoothprocess

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.persistableBundleOf
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_devices.view.*

class DeviceAdapter : RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder>() {
    val items: ArrayList<String> = arrayListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            DeviceViewHolder(
                    LayoutInflater.from(parent.context)
                            .inflate(R.layout.item_devices, parent, false)
            )

    override fun getItemCount() = items.count()
    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        if (items[position].isEmpty().not()){
            holder.bind(items[position])
        }
    }

    inner class DeviceViewHolder(private val itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: String) {
            item.let {
                itemView.tv_device_name.text = it
                itemView.tv_device_id.text = it
            }
        }
    }

    fun setItems(response: ArrayList<String>) {
        items.addAll(response)
        notifyDataSetChanged()
    }
}