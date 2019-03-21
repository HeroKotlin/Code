package com.github.herokotlin.code

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.graphics.RectF
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.RelativeLayout
import com.github.herokotlin.permission.Permission
import com.google.zxing.BarcodeFormat
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.CameraPreview
import com.journeyapps.barcodescanner.DefaultDecoderFactory
import kotlinx.android.synthetic.main.code_scanner.view.*
import java.lang.Exception

open class CodeScanner: RelativeLayout {

    // 用于请求权限
    var activity: Activity? = null

    var guideTitle = ""

        set(value) {
            if (field == value) {
                return
            }
            field = value
            guideLabel.text = value
        }

    lateinit var callback: CodeScannerCallback

    private val permission = Permission(89190904, listOf(Manifest.permission.CAMERA))

    private var supportedCodeType = listOf(BarcodeFormat.QR_CODE, BarcodeFormat.CODE_128)

    private var isTorchOn = false

        set(value) {

            if (field == value) {
                return
            }
            field = value

            barcodeView.setTorch(isTorchOn)

            if (isTorchOn) {
                torchButton.setImageResource(R.drawable.code_scanner_torch_off)
            }
            else {
                torchButton.setImageResource(R.drawable.code_scanner_torch_on)
            }

        }

    private var isPreviewing = false

        set(value) {
            if (field == value) {
                return
            }
            field = value
            if (value) {
                guideLabel.visibility = View.VISIBLE
                torchButton.visibility = View.VISIBLE
                laserView.visibility = View.VISIBLE
                startLaser()
            }
            else {
                guideLabel.visibility = View.GONE
                torchButton.visibility = View.GONE
                laserView.visibility = View.GONE
                stopLaser()
            }
        }

    private var laserAnimator: Animator? = null

    private var barcodeCallback = object: BarcodeCallback {
        override fun possibleResultPoints(resultPoints: List<ResultPoint>) {}
        override fun barcodeResult(result: BarcodeResult) {
            if (result.text == null) {
                return
            }
            callback.onScanSuccess(result.text)
        }
    }

    private val laserGap: Int by lazy {
        resources.getDimensionPixelSize(R.dimen.code_scanner_laser_gap)
    }

    private val laserHeight: Int by lazy {
        resources.getDimensionPixelSize(R.dimen.code_scanner_laser_height)
    }

    private val guideLabelMarginTop: Int by lazy {
        resources.getDimensionPixelSize(R.dimen.code_scanner_guide_label_margin_top)
    }

    private val torchMarginBottom: Int by lazy {
        resources.getDimensionPixelSize(R.dimen.code_scanner_torch_button_margin_bottom)
    }

    private val torchButtonHeight: Int by lazy {
        resources.getDimensionPixelSize(R.dimen.code_scanner_torch_button_height)
    }

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

        LayoutInflater.from(context).inflate(R.layout.code_scanner, this)

        barcodeView.decoderFactory = DefaultDecoderFactory(supportedCodeType)

        torchButton.setOnClickListener {
            isTorchOn = !isTorchOn
        }

        barcodeView.addStateListener(object: CameraPreview.StateListener {
            override fun cameraClosed() {
                isTorchOn = false
                isPreviewing = false
            }

            override fun cameraError(error: Exception?) {
                isTorchOn = false
                isPreviewing = false
                callback.onPermissionsNotGranted()
            }

            override fun previewStopped() {
                isTorchOn = false
                isPreviewing = false
            }

            override fun previewStarted() {
                isPreviewing = true
            }

            override fun previewSized() {
                val rect = barcodeView.framingRect
                if (rect != null) {

                    // 兼容某些手机不走正常流程
                    isPreviewing = true

                    val left = rect.left.toFloat()
                    val top = rect.top.toFloat()
                    val right = rect.right.toFloat()
                    val bottom = rect.bottom.toFloat()

                    viewFinder.box = RectF(left, top, right, bottom)
                    viewFinder.invalidate()

                    guideLabel.y = bottom + guideLabelMarginTop

                    torchButton.y = top - torchMarginBottom - torchButtonHeight

                    laserView.layoutParams.width = (right - left - 2 * laserGap).toInt()
                    laserView.x = left + laserGap

                }
            }
        })

        permission.onPermissionsNotGranted = {
            callback.onPermissionsNotGranted()
        }
        permission.onPermissionsGranted = {
            callback.onPermissionsGranted()
        }
        permission.onPermissionsDenied = {
            callback.onPermissionsDenied()
        }

    }

    private fun startLaser() {

        val animator = ValueAnimator.ofFloat(viewFinder.box.top, viewFinder.box.bottom - laserHeight)
        animator.duration = 3000
        animator.interpolator = LinearInterpolator()
        animator.addUpdateListener {
            laserView.y = it.animatedValue as Float
        }
        animator.addListener(object: AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                if (laserView.visibility == View.VISIBLE) {
                    startLaser()
                }
            }
        })
        animator.start()

        laserAnimator = animator

    }

    private fun stopLaser() {
        laserAnimator?.cancel()
        laserAnimator = null
    }

    private fun requestPermissions() {
        val context = activity ?: (context as Activity)
        permission.requestPermissions(context) {
            barcodeView.resume()
        }
    }

    fun start() {
        barcodeView.decodeContinuous(barcodeCallback)
        requestPermissions()
    }

    fun stop() {
        barcodeView.pause()
        barcodeView.stopDecoding()
    }

    /**
     * Start the camera preview and decoding. Typically this should be called from the Activity's
     * onResume() method.
     *
     * Call from UI thread only.
     */
    fun resume() {
        requestPermissions()
    }

    /**
     * Stops the live preview and decoding.
     *
     * Call from the Activity's onPause() method.
     */
    fun pause() {
        barcodeView.pause()
    }

    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        permission.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

}