package app.bbr.minardebug.permission

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

interface PermissionDelegate {
    fun isPermissionGranted(permission: String): Boolean
    fun shouldShowRequestPermissionRationale(permission: String): Boolean
    fun requestPermission(permissions: Array<out String>, requestCode: Int)
}

class ActivityPermissionDelegate(private val activity: Activity) : PermissionDelegate {
    override fun isPermissionGranted(permission: String): Boolean =
        isGranted(activity, permission)

    override fun shouldShowRequestPermissionRationale(permission: String): Boolean =
        ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)

    override fun requestPermission(permissions: Array<out String>, requestCode: Int) {
        ActivityCompat.requestPermissions(activity, permissions, requestCode)
    }
}

internal fun isGranted(context: Context, permission: String): Boolean {
    return ContextCompat.checkSelfPermission(context, permission) ==
            PackageManager.PERMISSION_GRANTED
}
