package com.github.herokotlin.code

import android.content.Context

abstract class CodeScannerConfiguration(val context: Context) {

    /**
     * 保存录音文件的目录
     */
    var guideLabelTitle = "扫描"

    /**
     * 请求权限
     */
    abstract fun requestPermissions(permissions: List<String>, requestCode: Int): Boolean

}