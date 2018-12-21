package com.github.herokotlin.code

interface CodeScannerCallback {

    // 识别成功
    fun onScanSuccess(text: String) {

    }

    // 扫描时，发现没权限
    fun onScanWithoutPermissions() {

    }

    // 用户点击同意授权
    fun onPermissionsGranted() {

    }

    // 用户点击拒绝授权
    fun onPermissionsDenied() {

    }

    fun onSizeChange() {

    }

    fun onPreviewingChange(isPreviewing: Boolean) {

    }

}