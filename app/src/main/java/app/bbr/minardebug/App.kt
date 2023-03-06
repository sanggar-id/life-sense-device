package app.bbr.minardebug

import android.app.Application
import com.lifesense.plugin.ble.LSBluetoothManager

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        LSBluetoothManager
            .getInstance()
            .initManager(applicationContext)

        LSBluetoothManager
            .getInstance()
            .registerBluetoothReceiver(applicationContext)
    }
}
