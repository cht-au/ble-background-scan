package com.example.ble_app

import android.app.*
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat

class BleForegroundService : Service() {

    private lateinit var bluetoothLeScanner: BluetoothLeScanner
    private val targetDeviceAddress = "0C:F3:EE:B4:E0:5C" // <- Replace with your beacon's MAC

    override fun onCreate() {
        super.onCreate()

        val bluetoothAdapter = (getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
        bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(1, createNotification("Scanning for BLE devices..."))
        startScan()
        return START_STICKY
    }

    override fun onDestroy() {
        bluetoothLeScanner.stopScan(leScanCallback)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotification(content: String): Notification {
        val channelId = "ble_scan_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "BLE Scanning", NotificationManager.IMPORTANCE_LOW)
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("BLE Foreground Service")
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()
    }

    private fun startScan() {
        bluetoothLeScanner.startScan(leScanCallback)
    }

    private val leScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val address = result.device.address
            val rssi = result.rssi
            Log.d("BLE_SERVICE", "Detected $address at $rssi dBm")

            if (address == targetDeviceAddress && rssi > -70) {
                sendBeaconNotification("Beacon in range! RSSI: $rssi dBm")
            }
        }
    }

    private fun sendBeaconNotification(content: String) {
        val notification = createNotification(content)
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(1001, notification)
    }
}