package top.someones.cardmatch.ui

import android.app.Activity
import android.content.pm.PackageManager

object PermissionsManagement {
    fun checkPermissions(activity: Activity, permissions: Array<String?>): Boolean {
        for (permission in permissions) {
            if (activity.checkSelfPermission(permission!!) == PackageManager.PERMISSION_DENIED) return false
        }
        return true
    }

    fun verifyPermissions(activity: Activity, permissions: Array<String?>?, requestCode: Int) {
        activity.requestPermissions(permissions!!, requestCode)
    }
}