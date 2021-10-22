package com.bluetooth.bluetoothprocess

import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_control.*
import java.io.IOException
import java.util.*

class ControlActivity : AppCompatActivity() {
    companion object{
        var m_myUUID = UUID.fromString("00001101-0000-1000-00805F9B34FB")
        var m_bluetoothSocket : BluetoothSocket? = null
        lateinit var m_progress : ProgressDialog
        lateinit var m_bluetoothAdapter : BluetoothAdapter
        var m_isConnected : Boolean = false
        lateinit var m_adsress : String
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_control)
        //m_adsress = intent.getStringExtra(MainActivity.EXTRA_ADRESS)
        ConnectToDevice(  this).execute()
        button1.setOnClickListener { sendCommand("a") }
        button2.setOnClickListener { sendCommand("b") }
        button3.setOnClickListener { disconnect() }
   }

    private fun sendCommand(input: String) {
        if (m_bluetoothSocket!= null){
            try {
                m_bluetoothSocket!!.outputStream.write(input.toByteArray())
            }catch (e: IOException){
                e.printStackTrace()
            }
        }
    }
    private fun disconnect() {
        if (m_bluetoothSocket != null){
            try {
                m_bluetoothSocket!!.close()
                m_bluetoothSocket = null
                m_isConnected = false
            }catch(e: IOException){
                e.printStackTrace()
            }
        }
        finish()
    }
    private class ConnectToDevice(c: Context) : AsyncTask<Void, Void, String>() {
        private var connectSuccess: Boolean = true
        private val context: Context

        init {
            this.context = c
        }

        override fun onPreExecute() {
            super.onPreExecute()
            m_progress = ProgressDialog.show(context,"Connecting","place wait")
        }

        override fun doInBackground(vararg p0: Void?): String? {
            try {
                if (m_bluetoothSocket == null  || m_isConnected){
                    m_bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                    val device : BluetoothDevice = m_bluetoothAdapter.getRemoteDevice(m_adsress)
                    m_bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(m_myUUID)
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery()
                    m_bluetoothSocket!!.connect()
                }
            }catch(e: IOException){
                connectSuccess = false
                e.printStackTrace()
            }
            return null
        }

        override fun onPostExecute(result: String) {
            super.onPostExecute(result)
            if (connectSuccess){
                Log.i("data","couldn't connect")
            }else{
                m_isConnected = true
            }
            m_progress.dismiss()
        }
    }
}