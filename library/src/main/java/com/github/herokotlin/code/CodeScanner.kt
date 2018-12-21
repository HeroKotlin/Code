package com.github.herokotlin.code

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.RectF
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.RelativeLayout
import com.google.zxing.BarcodeFormat
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.CameraPreview
import com.journeyapps.barcodescanner.DefaultDecoderFactory
import kotlinx.android.synthetic.main.code_scanner.view.*
import java.lang.Exception

class CodeScanner: RelativeLayout {

    companion object {

        const val PERMISSION_REQUEST_CODE = 19765

    }

    var guideTitle = ""

        set(value) {
            if (field == value) {
                return
            }
            field = value
            guideLabel.text = value
        }

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

    private lateinit var configuration: CodeScannerConfiguration
    private lateinit var callback: CodeScannerCallback

    private var laserAnimator: Animator? = null

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

    fun init(configuration: CodeScannerConfiguration, callback: CodeScannerCallback) {

        this.configuration = configuration
        this.callback = callback

        barcodeView.decoderFactory = DefaultDecoderFactory(supportedCodeType)

        barcodeView.decodeContinuous(object: BarcodeCallback {
            override fun possibleResultPoints(resultPoints: List<ResultPoint>) {}
            override fun barcodeResult(result: BarcodeResult) {
                if (result.text == null) {
                    return
                }
                callback.onScanSuccess(result.text)
            }
        })

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
                callback.onScanWithoutPermissions()
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

    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        requestPermissions()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        callback.onSizeChange()
    }

    private fun init() {
        LayoutInflater.from(context).inflate(R.layout.code_scanner, this)
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

    /**
     * 判断是否有权限，如没有，发起授权请求
     */
    private fun requestPermissions() {
        val hasPermissions = configuration.requestPermissions(
            listOf(
                android.Manifest.permission.CAMERA
            ),
            PERMISSION_REQUEST_CODE
        )
        if (hasPermissions) {
            barcodeView.resume()
        }
        else {
            callback.onScanWithoutPermissions()
        }
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

    /**
     * 如果触发了用户授权，则必须在 Activity 级别实现 onRequestPermissionsResult 接口，并调此方法完成授权
     */
    fun requestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        if (requestCode != PERMISSION_REQUEST_CODE) {
            return
        }

        for (i in 0 until permissions.size) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                callback.onPermissionsDenied()
                return
            }
        }

        callback.onPermissionsGranted()
        barcodeView.resume()

    }

}