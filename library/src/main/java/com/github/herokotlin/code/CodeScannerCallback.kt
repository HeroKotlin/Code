package com.github.herokotlin.code

import android.app.Activity
import androidx.core.app.ActivityCompat

interface CodeScannerCallback {

    // 识别成功
    fun onScanSuccess(text: String) {

    }

    fun onRequestPermissions(activity: Activity, permissions: Array<out String>, requestCode: Int) {
        ActivityCompat.requestPermissions(activity, permissions, requestCode)
    }

    fun onPermissionsGranted() {

    }

    fun onPermissionsDenied() {

    }

    fun onPermissionsNotGranted() {

    }

}