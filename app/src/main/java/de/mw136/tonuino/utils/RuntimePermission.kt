package de.mw136.tonuino.utils

import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object RuntimePermission {
    fun askFor(activity: Activity?, permission: String, requestCode: Int?): Int {
        // for API level < 23
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            // automatically grants permissions via Manifest
            return PackageManager.PERMISSION_GRANTED
        } else {
            // for API level 23+
            if (ContextCompat.checkSelfPermission(activity!!, permission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, arrayOf(permission), requestCode!!)
                return PackageManager.PERMISSION_DENIED
            } else {
                return PackageManager.PERMISSION_GRANTED
            }
        }
    }
}