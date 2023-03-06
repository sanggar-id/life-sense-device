package app.bbr.minardebug.data

import com.lifesense.plugin.ble.data.LSDeviceInfo

interface SharedPreferencesRepository {
    fun save(device: LSDeviceInfo?)

    fun setDevicePair(status: Boolean)
    fun device(): LSDeviceInfo?
    fun isDevicePaired(): Boolean
}

class SharedPreferencesRepositoryImpl : SharedPreferencesRepository {

    private var mDevice: LSDeviceInfo? = null
    private var isDevicePaired: Boolean = false

    override fun device(): LSDeviceInfo? {
        return mDevice
    }

    override fun save(device: LSDeviceInfo?) {
        mDevice = device
    }

    override fun setDevicePair(status: Boolean) {
        isDevicePaired = status
    }

    override fun isDevicePaired(): Boolean {
        return isDevicePaired
    }
}
