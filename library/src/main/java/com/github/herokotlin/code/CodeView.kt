package com.github.herokotlin.code

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.journeyapps.barcodescanner.BarcodeEncoder

open class CodeView: ImageView {

    private val barcodeEncoder = BarcodeEncoder()

    private var hints = HashMap<EncodeHintType, Any>()

    var text = ""

        set(value) {
            if (field == value) {
                return
            }
            field = value
            updateCode()
        }

    open val codeType = BarcodeFormat.CODE_128

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init()
    }

    private fun init() {
        hints[EncodeHintType.MARGIN] = 0
        hints[EncodeHintType.CHARACTER_SET] = "utf-8"
        hints[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.H
    }

    private fun updateCode() {
        val bitmap = barcodeEncoder.encodeBitmap(text, codeType, width, height, hints)
        setImageBitmap(bitmap)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateCode()
    }

}