package com.github.herokotlin.code

import android.content.Context
import android.util.AttributeSet
import com.google.zxing.BarcodeFormat

class QRCodeView: CodeView {

    override val codeType = BarcodeFormat.QR_CODE

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

}