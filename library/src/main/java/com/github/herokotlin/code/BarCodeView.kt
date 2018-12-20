package com.github.herokotlin.code

import android.content.Context
import android.util.AttributeSet
import com.google.zxing.BarcodeFormat

class BarCodeView: CodeView {

    override val codeType = BarcodeFormat.CODE_128

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

}