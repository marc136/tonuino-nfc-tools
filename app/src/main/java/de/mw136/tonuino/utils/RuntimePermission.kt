package de.mw136.tonuino.utils

import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object RuntimePermission {
    fun askFor(activity: Activity?, permission: String, requestCode: Int?): Int {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            // API level < 23 automatically grants permissions via Manifest
            PackageManager.PERMISSION_GRANTED
        } else {
            // for API level 23+
            if (ContextCompat.checkSelfPermission(activity!!, permission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, arrayOf(permission), requestCode!!)
                PackageManager.PERMISSION_DENIED
            } else {
                PackageManager.PERMISSION_GRANTED
            }
        }
    }
}