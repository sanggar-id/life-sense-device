@file:SuppressLint("MissingPermission")

package app.bbr.minardebug

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import app.bbr.minardebug.permission.PermissionRequestCallback
import app.bbr.minardebug.permission.PermissionsManager
import com.lifesense.plugin.ble.LSBluetoothManager

class MainActivity : AppCompatActivity(), PermissionRequestCallback {

    private val txtResult by lazy { findViewById<TextView>(R.id.txt_result) }
    private val btnGetResult by lazy { findViewById<Button>(R.id.btn_get_result) }

    private val permissionManager by lazy {
        PermissionsManager.init(this, this)
    }

    private val bluetoothLeService by lazy {
        val manager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        manager.adapter.bluetoothLeScanner
    }

    private var isPermissionGranted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initView()
    }

    override fun onStart() {
        super.onStart()
        requestPermission()
    }

    override fun onStop() {
        super.onStop()

        bluetoothLeService.stopScan(onBluetoothCallback())
    }

    private fun requestPermission() {
        permissionManager.requestPermissions(
            listOf(
                "android.permission.ACCESS_FINE_LOCATION",
                "android.permission.ACCESS_COARSE_LOCATION",
                "android.permission.BLUETOOTH",
                "android.permission.BLUETOOTH_SCAN",
                "android.permission.BLUETOOTH_CONNECT"
            ),
            CODE_PERMISSION_REQUEST
        )
    }

    private fun initView() {
        btnGetResult.setOnClickListener {
            if (isPermissionGranted) {
                if (LSBluetoothManager.getInstance().isBluetoothAvailable) {
                    bluetoothLeService.startScan(onBluetoothCallback())
                } else {
                    Toast.makeText(applicationContext, "Please turn-on your bluetooth.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(applicationContext, "Please grant the permission first.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun onBluetoothCallback() = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            if (result == null) return

            txtResult.text = Dependencies
                .lifeSenseRepository()
                .get()
        }
    }

    // because this app is debug only, assume we always granted the permission
    override fun onPermissionPermanentlyDenied(permissions: List<String>) {}

    // because this app is debug only, assume we always granted the permission
    override fun onDenied(permission: List<String>) {}

    override fun onGranted(permissions: List<String>) {
        val isBluetoothGranted = permissions.contains("android.permission.BLUETOOTH")
        val isLocationGranted = permissions.contains("android.permission.ACCESS_FINE_LOCATION")

        isPermissionGranted = isBluetoothGranted && isLocationGranted
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == CODE_PERMISSION_REQUEST) {
            permissionManager.onRequestPermissionsResult(permissions, grantResults)
        }
    }

    companion object {
        private const val CODE_PERMISSION_REQUEST = 123
    }
}
