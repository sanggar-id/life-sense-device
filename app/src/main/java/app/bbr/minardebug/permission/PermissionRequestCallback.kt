package app.bbr.minardebug.permission

interface PermissionRequestCallback {
    fun onPermissionPermanentlyDenied(permissions: List<String>)
    fun onDenied(permission: List<String>)
    fun onGranted(permissions: List<String>)
}
