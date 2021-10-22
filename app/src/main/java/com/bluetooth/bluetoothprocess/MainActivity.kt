package com.bluetooth.bluetoothprocess
import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.MacAddress
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException
import java.util.*


class MainActivity : AppCompatActivity() {

    var deviceAdapter = DeviceAdapter()
    var deviceList: ArrayList<String> = arrayListOf()
    var bluetoothAdapter: BluetoothAdapter? = null
    lateinit var receiver: Receiver

    val MODULE_MAC = "00:13:EF:00:6A:6B"
    val REQUEST_ENABLE_BT = 1
    private val MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")

    var bta: BluetoothAdapter? = null
    var mmSocket: BluetoothSocket? = null
    var mmDevice: BluetoothDevice? = null
    var btt: ConnectedThread? = null
    var response: TextView? = null
    var lightflag = false
    var relayFlag = true
    var mHandler: Handler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        receiver = Receiver()

        receiver.deviceList = { deviceList, deviceName, deviceUuid->
            val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, deviceList)
            rv_device_list.adapter = adapter
           //deviceAdapter.setItems(deviceList)
            rv_device_list.setOnItemClickListener(object : AdapterView.OnItemClickListener {
                override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    initiateBluetoothProcess(deviceList[position].address)
                    //Toast.makeText(applicationContext, deviceList[position].name.toString(), Toast.LENGTH_SHORT).show()
                    adapter.notifyDataSetChanged()
                }

            })
        }


       // rv_device_list.layoutManager = LinearLayoutManager(applicationContext, LinearLayoutManager.VERTICAL, false)
      //  rv_device_list.adapter = deviceAdapter

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            Toast.makeText(applicationContext, "Not support", Toast.LENGTH_LONG).show()
        }
        bluetoothEnableCheck(bluetoothAdapter)

        bluetoothPermission()

        btn_search_device.setOnClickListener {
            val filter = IntentFilter(BluetoothDevice.ACTION_FOUND).apply {
                addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
                addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
                addAction(BluetoothDevice.EXTRA_DEVICE)
                addAction(BluetoothAdapter.EXTRA_SCAN_MODE)
            }
            registerReceiver(receiver, filter)
            bluetoothAdapter?.startDiscovery()
        }
        btn_old_device.setOnClickListener {
            findBluetoothDeviceList(bluetoothAdapter)
        }
        is_visible_show.setOnClickListener {
            isVisibleDeviceBluetooth()
        }
    }

    private fun bluetoothEnableCheck(bluetoothAdapter: BluetoothAdapter?){
        if (bluetoothAdapter?.isEnabled == false) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, 1)
        }
    }

    //Önceden bağlı cihazları getirir.
    private fun findBluetoothDeviceList(bluetoothAdapter: BluetoothAdapter?){
        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
        pairedDevices?.forEach { device ->
            deviceList.add(device.name)
            Log.i("cihaz last", device.name)
            val deviceName = device.name
            val deviceHardwareAddress = device.address // MAC address
        }
        deviceAdapter.setItems(deviceList)
    }

    //Uygulamayı kapattığında register bağlantısını sıfırlıyor.
    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

    private fun isVisibleDeviceBluetooth(){
        val requestCode = 1;
        val discoverableIntent: Intent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
            putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
        }
        startActivityForResult(discoverableIntent, requestCode)
    }

    private fun bluetoothPermission(){
        val permission1 = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH)
        val permission2 = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN)
        val permission3 = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        val permission4 = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        if (permission1 != PackageManager.PERMISSION_GRANTED
                || permission2 != PackageManager.PERMISSION_GRANTED
                || permission3 != PackageManager.PERMISSION_GRANTED
                || permission4 != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(
                            Manifest.permission.BLUETOOTH,
                            Manifest.permission.BLUETOOTH_ADMIN,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION),
                    642)
        } else {
            Log.d("DISCOVERING-PERMISSIONS", "Permissions Granted")
        }
    }

//"B4:F6:1C:CA:93:BF" iphone mac
    fun initiateBluetoothProcess(macAddress: String){
        bluetoothAdapter?.let { bta->
            if(bta.isEnabled()){
                //attempt to connect to bluetooth module
                var tmp: BluetoothSocket? = null;
                mmDevice = bta.getRemoteDevice(macAddress);
                //create socket
                try {
                    mmDevice?.let {
                        tmp = it.createRfcommSocketToServiceRecord(MY_UUID);
                        mmSocket = tmp;
                        mmSocket?.connect();
                        Log.i("[BLUETOOTH]","Connected to: "+it.getName());
                    }
                }catch(e: IOException){
                    try{mmSocket?.close();}catch(c: IOException){return;}
                }

                Log.i("[BLUETOOTH]", "Creating handler");

                 val mHandler = object : Handler(Looper.getMainLooper()) {
                    override fun handleMessage(msg: Message) {
                        if(msg.what == ConnectedThread.RESPONSE_MESSAGE){
                            response?.let { response->
                                if(response.getText().toString().length >= 30){
                                    response.setText("");
                                    response.append(msg.obj.toString());
                                }else{
                                    response.append("\n" + msg.obj.toString());
                                }
                            }
                        }
                    }
                }


                Log.i("[BLUETOOTH]", "Creating and running Thread");
                btt = mmSocket?.let { ConnectedThread(it, mHandler) }
                btt?.start();
            }
        }
    }
}
