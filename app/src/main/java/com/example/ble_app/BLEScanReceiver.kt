package com.example.ble_app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.bluetooth.le.ScanResult
import android.bluetooth.le.BluetoothLeScanner
import android.os.Build
import androidx.annotation.RequiresApi
import java.sql.Timestamp


class BLEScanReceiver : BroadcastReceiver() {
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("BLEScanReceiver", "Received BLE scan result broadcast.")
        val results = intent.getParcelableArrayListExtra(
            BluetoothLeScanner.EXTRA_LIST_SCAN_RESULT,
            ScanResult::class.java)

        if (results != null) {
            for (result in results) {
                val device = result.device
                val address = device.address
                val rssi = result.rssi

                val currentTimeMillis = System.currentTimeMillis()
                val timeStamp = Timestamp(currentTimeMillis)

                Log.d("BLEScanReceiver", "Device found: $address, RSSI: $rssi, time: $timeStamp")
                val notificationHelper = NotificationHandler(context)
                notificationHelper.buildNotification(
                    title = "BLE Device Detected",
                    message = "Address: $address, RSSI: $rssi, Time: $timeStamp"
                )
            }
        }
    }
}
