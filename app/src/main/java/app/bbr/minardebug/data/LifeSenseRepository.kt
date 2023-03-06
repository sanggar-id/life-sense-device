package app.bbr.minardebug.data

import android.util.Log
import app.bbr.minardebug.util.rounded
import com.lifesense.plugin.ble.LSBluetoothManager
import com.lifesense.plugin.ble.OnPairingListener
import com.lifesense.plugin.ble.OnSearchingListener
import com.lifesense.plugin.ble.OnSyncingListener
import com.lifesense.plugin.ble.data.*
import com.lifesense.plugin.ble.data.scale.LSScaleWeight
import com.lifesense.plugin.ble.data.tracker.ATPairConfirmInfo
import com.lifesense.plugin.ble.data.tracker.ATPairConfirmState
import com.lifesense.plugin.ble.data.tracker.ATPairResultsCode
import com.lifesense.plugin.ble.data.tracker.ATUserInfo

interface LifeSenseRepository {
    fun get(): String
}

class LifeSenseRepositoryImpl constructor(
    private val sharedPref: SharedPreferencesRepository
) : LifeSenseRepository {

    // sdk manager
    private val sdkManager = LSBluetoothManager.getInstance()

    // filter only get weight scale device
    private val deviceType = listOf(LSDeviceType.WeightScale)

    // get device info
    private var mDevice: LSDeviceInfo? = null

    // device result (temp variable)
    private var result: LSScaleWeight? = null

    override fun get(): String {
        val status = sdkManager.managerStatus

        if (mDevice != null && sharedPref.isDevicePaired()) {
            if (result == null) {
                createConnection()
            }

            return result.normalized()
        }

        if (status == LSManagerStatus.Scanning) {
            sdkManager.stopSearch()
        }

        if (mDevice == null) {
            LSBluetoothManager
                .getInstance()
                .searchDevice(deviceType, searchDeviceListener)
        } else {
            if (sharedPref.device() != null && mDevice?.macAddress == sharedPref.device()?.macAddress && sharedPref.isDevicePaired()) {
                Log.d("LifeSense", "already paired")
            }

            val autoPair = 0x05
            sharedPref.device()?.pairMode = autoPair

            mockUserInfo()

            sharedPref.setDevicePair(
                LSBluetoothManager
                    .getInstance()
                    .pairDevice(sharedPref.device(), onPairingListener)
            )
        }

        return "0.0"
    }

    private fun mockUserInfo() {
        val info = ATUserInfo()
        info.age = 24
        info.isAthlete = false
        info.height = 1.73f
        info.weight = 74f
        info.userGender = LSUserGender.Male

        sharedPref.device()?.userInfo = info
    }

    private fun createConnection() {
        val device = sharedPref.device() ?: return

        if (sdkManager.managerStatus == LSManagerStatus.Syncing &&
            sdkManager.checkConnectState(device.macAddress) == LSConnectState.ConnectSuccess &&
            result != null
        ) {
            sdkManager.resetSyncingListener(onSyncDeviceListener)
            return
        }

        if (sdkManager.managerStatus == LSManagerStatus.Syncing) {
            return
        }

        sdkManager.devices = null
        sdkManager.stopDeviceSync()
        sdkManager.addDevice(device)
        sdkManager.startDeviceSync(onSyncDeviceListener)
    }

    private val onPairingListener = object : OnPairingListener() {
        override fun onStateChanged(device: LSDeviceInfo?, status: Int) {
            super.onStateChanged(device, status)

            if (status == ATPairResultsCode.PAIR_SUCCESSFULLY) {
                val config = LSScanIntervalConfig()
                config.pairDevice = device

                sdkManager.setManagerConfig(config)
            }
        }

        override fun onMessageUpdate(macAddress: String, setting: LSDevicePairSetting?) {
            when (setting?.pairCmd) {
                LSPairCommand.RandomCodeConfirm -> {
                    sdkManager.pushPairSetting(macAddress, setting)
                }
                LSPairCommand.DeviceIdRequest -> {
                    setting.obj = macAddress.replace(":", "")
                    sdkManager.pushPairSetting(macAddress, setting)
                }
                LSPairCommand.PairConfirm -> {
                    val confirmInfo = ATPairConfirmInfo(ATPairConfirmState.Success)
                    confirmInfo.userNumber = 0
                    setting.obj = confirmInfo
                    sdkManager.pushPairSetting(macAddress, setting)
                }
                else -> {}
            }
        }
    }

    private val onSyncDeviceListener = object : OnSyncingListener() {
        override fun onNotificationDataUpdate(p0: String?, p1: IDeviceData?) {}

        override fun onStateChanged(broadcastId: String, state: LSConnectState) {}

        override fun onScaleWeightDataUpdate(broadcastId: String, weight: LSScaleWeight?) {
            result = weight
        }

        override fun onDeviceInformationUpdate(broadcastId: String, device: LSDeviceInfo?) {
            if (sharedPref.device() == null) return

            sharedPref.device()?.apply {
                firmwareVersion = device?.firmwareVersion
                hardwareVersion = device?.hardwareVersion
                modelNumber = device?.modelNumber

                if (device?.password != null) {
                    password = device.password
                }
            }
        }
    }

    private val searchDeviceListener = object : OnSearchingListener() {
        override fun onSearchResults(info: LSDeviceInfo?) {
            // cancel previous device
            if (sharedPref.device() != null) {
                if (sharedPref.device()?.macAddress != info?.macAddress) {

                    // cancel pairing previous device
                    LSBluetoothManager
                        .getInstance()
                        .cancelDevicePairing(sharedPref.device())
                }
            }

            if (info != null) {
                sharedPref.save(info)
                mDevice = info
            }
        }
    }

    private fun LSScaleWeight?.normalized(): String {
        if (this == null) return "0.0"

        return (this.weight * 0.1)
            .rounded()
            .toString()
    }
}
