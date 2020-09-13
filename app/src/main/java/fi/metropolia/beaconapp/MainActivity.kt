package fi.metropolia.beaconapp

import android.Manifest
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Toast
import android.content.Context
import android.content.pm.PackageManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.*
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*
import java.net.NetworkInterface
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap



class MainActivity : AppCompatActivity() {

    companion object {
        const val TAG = "MainActivity"
        const val SCAN_PERIOD: Long = 10000
    }

    private lateinit var btManager: BluetoothManager
    private lateinit var btAdapter: BluetoothAdapter
    private lateinit var bleScanner: BluetoothLeScanner
    private lateinit var bleCallback: ScanCallback
    private var scanResults: HashMap<String?, ScanResult?> = HashMap()
    private var isScanning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        scanResultListRv.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "No fine location access")
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }

        btManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        btAdapter = btManager.adapter
        deviceMacTv.text = "${getString(R.string.phone_bt_address)} ${getWifiMacAddress()}"

        if (hasPermissions()) {
            scanButton.setOnClickListener { startScan() }
        } else {
            Toast.makeText(this, "No permission granted!", Toast.LENGTH_LONG).show()
            scanButton.isClickable = false
            scanButton.alpha = 0.25f
        }

    }

    private fun startScan() {
        Log.d(TAG, "BLE Scanning started...")
        bleScanner = btAdapter.bluetoothLeScanner
        bleCallback = BLEScanCallback()
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
            .build()

        val filter: List<ScanFilter>? = null

        // Stop scanning after period
        Handler().postDelayed({ stopScan() }, SCAN_PERIOD)

        isScanning = true
        setScanUI()
        bleScanner.startScan(filter, settings, bleCallback)
    }

    private fun stopScan() {
        Log.d(TAG, "BLE Scanning stopped.")
        isScanning = false
        setScanUI()
        bleScanner.stopScan(bleCallback)
        updateDeviceList()
    }

    private fun setScanStatusText() {
        if (scanResults.isNotEmpty()) {
            val devices = scanResults.size
            scanStatusTv.text = "$devices DEVICES"
        } else {
            scanStatusTv.text = "NO DEVICES FOUND"
        }
    }

    private fun setScanUI() {
        if (isScanning) {
            spinner.visibility = View.VISIBLE
            scanButton.alpha = 0.25f
            scanButton.isClickable = false
            scanStatusTv.text = "SCANNING FOR DEVICES"
        } else {
            spinner.visibility = View.GONE
            scanButton.alpha = 1f
            scanButton.isClickable = true
        }

    }

    private fun updateDeviceList() {
        val deviceList = ArrayList(scanResults.values)
        val adapter = CustomAdapter(deviceList)
        scanResultListRv.adapter = adapter
    }

    private fun hasPermissions(): Boolean {
        if (!btAdapter.isEnabled) {
            Log.d(TAG, "No Bluetooth LE capability")
            return false
        } else if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED) {
            Log.d("DBG", "No fine location access")
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            return true // assuming that the user grants permission
        }
        return true
    }

    private fun getWifiMacAddress(): String {
        try {
            val interfaceName = "wlan0"
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (intf in interfaces) {
                if (!intf.name.equals(interfaceName, ignoreCase = true)) {
                    continue
                }

                val mac = intf.hardwareAddress ?: return ""

                val buf = StringBuilder()
                for (aMac in mac) {
                    buf.append(String.format("%02X:", aMac))
                }
                if (buf.isNotEmpty()) {
                    buf.deleteCharAt(buf.length - 1)
                }
                return buf.toString()
            }
        } catch (ex: Exception) {
        }
        // for now eat exceptions
        return "scuffed :/"
    }

    private inner class BLEScanCallback: ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            addScanResult(result)
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.d(TAG, "BLE scan failed with error code: $errorCode")
        }

        private fun addScanResult(result: ScanResult?) {
            val deviceAddress: String? = result?.device?.address
            if (!scanResults.containsKey(deviceAddress)) {
                scanResults[deviceAddress] = result
                setScanStatusText()
                updateDeviceList()
                Log.d(TAG, "Device address: $deviceAddress - ${result?.device?.name} (${result?.isConnectable})")
            }
        }
    }

}
