package com.github.herokotlin.code

interface CodeScannerCallback {

    // 扫描时，发现没权限
    fun onScanWithoutPermissions() {

    }

    // 识别成功
    fun onScanSuccess(code: String) {

    }

    // 用户点击同意授权
    fun onPermissionsGranted() {

    }

    // 用户点击拒绝授权
    fun onPermissionsDenied() {

    }

}