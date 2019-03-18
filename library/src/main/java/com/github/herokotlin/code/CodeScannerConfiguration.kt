package com.github.herokotlin.code

abstract class CodeScannerConfiguration {

    /**
     * 请求权限
     */
    abstract fun requestPermissions(permissions: List<String>, requestCode: Int): Boolean

}