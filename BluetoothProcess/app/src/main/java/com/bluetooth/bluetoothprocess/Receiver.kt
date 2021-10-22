package com.bluetooth.bluetoothprocess

import android.app.AlertDialog
import android.app.Dialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class Receiver: BroadcastReceiver() {

    var deviceList: (ArrayList<BluetoothDevice>, String, UUID) -> Unit = { _, _, _ -> }
    var deviceArrayList: ArrayList<BluetoothDevice> = arrayListOf()

    var devices: (ArrayList<BluetoothDevice?>) -> Unit = {}
    var devicesListesi: ArrayList<BluetoothDevice?> = arrayListOf()

    override fun onReceive(context: Context?, intent: Intent?) {
        val action: String? = intent?.action
        when (action) {
            BluetoothDevice.ACTION_FOUND -> {
                Toast.makeText(context, "Cihaz bulundu", Toast.LENGTH_LONG).show()
                val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                devicesListesi.add(device)
                devices(devicesListesi)
                device?.let {
                    Log.i("DeviceInfo", it.name + " - " + it.address)
                    // device.name?.let { Log.i("cihaz reciever", it) }
                    deviceArrayList.add(device)
                    val deviceName = device.name
                    val deviceHardwareAddress = device.address // MAC address
                }
                if (deviceArrayList.isNotEmpty()) {
                    device?.name?.let { deviceList(deviceArrayList, it, UUID.randomUUID()) }
                }
            }

            BluetoothDevice.ACTION_ACL_CONNECTED -> {
                Toast.makeText(context, "ACTION_ACL_CONNECTED", Toast.LENGTH_LONG).show()
            }
            BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                Toast.makeText(context, "ACTION_ACL_DISCONNECTED", Toast.LENGTH_LONG).show()
            }
            BluetoothDevice.EXTRA_DEVICE -> {
                Toast.makeText(context, "EXTRA_DEVICE", Toast.LENGTH_LONG).show()
            }
            BluetoothAdapter.EXTRA_SCAN_MODE -> {
                Toast.makeText(context, "EXTRA_SCAN_MODE", Toast.LENGTH_LONG).show()
            }
            BluetoothAdapter.ACTION_STATE_CHANGED -> {
                Toast.makeText(context, "Bluetooth kapandı/açıldı", Toast.LENGTH_LONG).show()
            }
            BluetoothDevice.ACTION_PAIRING_REQUEST -> {

            }
        }
    }
}

val uuid: UUID = UUID.fromString("8989063a-c9af-463a-b3f1-f21d9b2b827b")
var message = "al"

class BluetoothClient(device: BluetoothDevice): Thread() {
    private val socket = device.createRfcommSocketToServiceRecord(uuid)

    override fun run() {
        Log.i("client", "Connecting")
        this.socket.connect()

        Log.i("client", "Sending")
        val outputStream = this.socket.outputStream
        val inputStream = this.socket.inputStream
        try {
            outputStream.write(message.toByteArray())
            outputStream.flush()
            Log.i("client", "Sent")
        } catch(e: Exception) {
            Log.e("client", "Cannot send", e)
        } finally {
            outputStream.close()
            inputStream.close()
            this.socket.close()
        }
    }
}