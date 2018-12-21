package com.github.herokotlin.code

import android.content.Context

abstract class CodeScannerConfiguration(val context: Context) {

    /**
     * 请求权限
     */
    abstract fun requestPermissions(permissions: List<String>, requestCode: Int): Boolean

}