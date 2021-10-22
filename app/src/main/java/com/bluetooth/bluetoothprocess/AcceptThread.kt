package com.bluetooth.bluetoothprocess

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.icu.lang.UProperty.NAME
import android.util.Log
import android.widget.Toast
import java.io.IOException
import java.util.*

class AcceptThread(name: String, uuid: UUID, var bluetoothAdapter: BluetoothAdapter?) : Thread() {

    private var mmServerSocket: BluetoothServerSocket? = null

    fun AcceptThread() {
        // Use a temporary object that is later assigned to mmServerSocket,
        // because mmServerSocket is final
        var tmp: BluetoothServerSocket? = null
        try {
            // MY_UUID is the app's UUID string, also used by the client code
            tmp = bluetoothAdapter?.listenUsingRfcommWithServiceRecord("BT", UUID.randomUUID())
        } catch (e: IOException) {
            Log.i("error", e.message.toString())
        }
        mmServerSocket = tmp
    }

    override fun run() {
        var socket: BluetoothSocket? = null
        // Keep listening until exception occurs or a socket is returned
        while (true) {
            socket = try {
                mmServerSocket!!.accept()
            } catch (e: IOException) {
                break
            }
            // If a connection was accepted
            if (socket != null) {
                // Do work to manage the connection (in a separate thread)
                Log.i("accept","Kabul etti")
                //manageConnectedSocket(socket)
                mmServerSocket!!.close()
                break
            }
        }
    }

    /** Will cancel the listening socket, and cause the thread to finish  */
    fun cancel() {
        try {
            mmServerSocket!!.close()
        } catch (e: IOException) {
        }
    }
}