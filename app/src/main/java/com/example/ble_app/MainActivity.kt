package com.example.ble_app

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.ble_app.ui.theme.BleappTheme
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.ParcelUuid
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.unit.sp


class MainActivity : ComponentActivity() {

    private var bluetoothManager: BluetoothManager? = null
    private var bluetoothAdapter: BluetoothAdapter? = null
    private val handler = Handler(Looper.getMainLooper())
    private val SCAN_PERIOD: Long = 10000
    private var scanning = false
    private var bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner

    private val foundDevices = mutableStateListOf<String>()
    private val foundAddresses = mutableStateListOf<String>()
    private val devicesMap = mutableStateMapOf<String, Int>()
    private val targetDeviceAddress = "0C:F3:EE:B5:12:10"




    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bluetoothManager = getSystemService(BluetoothManager::class.java)
        bluetoothAdapter = bluetoothManager?.adapter
        bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner
        val scanner = getSystemService(BluetoothManager::class.java).adapter?.bluetoothLeScanner

        // Checking Permissions
        requestBluetooth()
//        checkAndRequestPermissions()


//        if (scanner != null) {
//            startScan(this, scanner)
//        }

        enableEdgeToEdge()


        setContent {
            Content()
        }
    }



    @Composable
    fun Content() {
        BleappTheme {
            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = { scanBackground() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Background Scan")
                        }
                        Button(
                            onClick = { requestPushNotification() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Request Notification")
                        }
                    }
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding),
                        onScanClick = {
                            Log.d("BluetoothScan", "Scan button clicked")
                            scanLeDevice()
                        },
                        devices = foundDevices,
                        devicesMap = devicesMap
                    )
                }


            }
        }
    }

//    private val REQUEST_PERMISSIONS_CODE = 1
//    private fun checkAndRequestPermissions() {
//        val permissions = mutableListOf<String>()
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
//            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
//        }
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
//        }
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
//        }
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            permissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
//        }
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
//            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
//        }
//        if (permissions.isNotEmpty()) {
//            ActivityCompat.requestPermissions(this, permissions.toTypedArray(), REQUEST_PERMISSIONS_CODE)
//        }
//    }

    private fun scanLeDevice() {
        if (!scanning) { // Stops scanning after a pre-defined scan period.
            handler.postDelayed({
                scanning = false

                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.BLUETOOTH_SCAN
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling ActivityCompat#requestPermissions
                }

                bluetoothLeScanner?.stopScan(leScanCallback)
            }, SCAN_PERIOD)
            scanning = true
            bluetoothLeScanner?.startScan(leScanCallback)
        } else {
            scanning = false
            bluetoothLeScanner?.stopScan(leScanCallback)
        }
    }
    private val leScanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)

            val device = result.device
            val address = device.address
            val rssi = result.rssi  //signal strength in dBm

            devicesMap[address] = rssi
        }
        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.e("BluetoothScan", "Scan failed with error code: $errorCode")
        }
    }
    @SuppressLint("InlinedApi")
    @RequiresApi(Build.VERSION_CODES.O)
    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    private fun startScan(
        context: Context,
        scanner: BluetoothLeScanner,
    ): PendingIntent? {
        val scanSettings: ScanSettings = ScanSettings.Builder()
            // There are other modes that might work better depending on the use case
            .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
            // If not set the results will be batch while the screen is off till the screen is turned one again
            .setReportDelay(3000)
            // Use balanced, when in background it will be switched to low power
            .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
            .build()

        // Create the pending intent that will be invoked when the scan happens and the filters matches
        val resultIntent = PendingIntent.getBroadcast(
            context,
            1,
            Intent(context, BLEScanReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE,
        )

        val scanFilters = listOf(
            ScanFilter.Builder()
                .setDeviceAddress("0C:F3:EE:B5:12:10")
                .build(),
        )
        // Empty filter for debugging
//        val scanFilters = emptyList<ScanFilter>()

        Log.d("BLEScanReceiver", "Scanning")
        scanner.startScan(scanFilters, scanSettings, resultIntent)
        return resultIntent
    }
    private fun requestBluetooth() {
        // check android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

            val permissions = mutableListOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION,
            )
            requestMultiplePermissions.launch(
                permissions.toTypedArray()
            )
            // This is big location
//            requestMultiplePermissions.launch(
//                arrayOf(
//                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
//                )
//            )
//            requestMultiplePermissions.launch(
//                arrayOf(
//                    Manifest.permission.POST_NOTIFICATIONS
//                )
//            )
        } else {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            requestEnableBluetooth.launch(enableBtIntent)
        }
    }
    private val requestEnableBluetooth =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                // granted
            } else {
                // denied
            }
        }
    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                Log.d("BLES", "${it.key} = ${it.value}")
            }
        }

    @RequiresApi(Build.VERSION_CODES.O) //required for scanning
    private fun scanBackground() {
        val scanner = getSystemService(BluetoothManager::class.java).adapter?.bluetoothLeScanner
        if (scanner != null) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestBackgroundLocation()
            }
            startScan(this, scanner)
        }


    }
    private fun requestBackgroundLocation() {
        Log.d("BLES", "Requesting background location")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            requestBackgroundLocationPermission.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
    }
    private val requestBackgroundLocationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            Log.d("Permissions", "Background location granted: $isGranted")
        }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestPushNotification() {
        Log.d("BLES", "Requesting Post Notificatino")
        requestPushNotification.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
    private val requestPushNotification =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            Log.d("Permissions", "Push Notifcation Permission Granted: $isGranted")
        }
}


@Composable
fun Greeting(name: String, modifier: Modifier = Modifier, onScanClick: () -> Unit, devices: List<String>,devicesMap: Map<String,Int> ) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Hello $name!")
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onScanClick) {
            Text("Start BLE Scan")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Discovered Devices:")
        LazyColumn {
            items(devicesMap.entries.toList().sortedBy { -it.value }) { (address, rssi) ->
                DeviceItem(address = address, rssi = rssi)
            }
        }
    }
}

@Composable
fun DeviceItem(address: String, rssi: Int) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(10.dp)) {
        Text(text = "Address: $address RSSI: $rssi", fontSize = 15.sp, lineHeight = 10.sp)
    }
}