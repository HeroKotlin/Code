package com.github.herokotlin.code

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
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

    lateinit var onScanResult: (String) -> Unit

    var torchOn = false

        set(value) {

            if (field == value) {
                return
            }
            field = value

            barcodeView.setTorch(torchOn)
            if (torchOn) {
                torchButton.setImageResource(R.drawable.code_scanner_torch_off)
            }
            else {
                torchButton.setImageResource(R.drawable.code_scanner_torch_on)
            }

        }

    var isPreviewing = false

        set(value) {
            if (field == value) {
                return
            }
            field = value
            if (value) {
                torchButton.visibility = View.VISIBLE
                laserView.visibility = View.VISIBLE
                startLaser()
            }
            else {
                torchButton.visibility = View.GONE
                laserView.visibility = View.GONE
                stopLaser()
            }
        }

    private var lastText = ""

    private val callback = object: BarcodeCallback {
        override fun possibleResultPoints(resultPoints: List<ResultPoint>) {}
        override fun barcodeResult(result: BarcodeResult) {
            if (result.text == null || result.text == lastText) {
                return
            }
            lastText = result.text
            onScanResult(lastText)
        }
    }

    private var laserAnimator: Animator? = null

    private val laserGap: Int by lazy {
        resources.getDimensionPixelSize(R.dimen.code_scanner_laser_gap)
    }

    private val laserHeight: Int by lazy {
        resources.getDimensionPixelSize(R.dimen.code_scanner_laser_height)
    }

    private val guideMarginTop: Int by lazy {
        resources.getDimensionPixelSize(R.dimen.code_scanner_guide_margin_top)
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

        val formats = listOf(BarcodeFormat.QR_CODE, BarcodeFormat.CODE_128)
        barcodeView.decoderFactory = DefaultDecoderFactory(formats)
        barcodeView.decodeContinuous(callback)

        barcodeView.resume()

        torchButton.setOnClickListener {
            torchOn = !torchOn
        }

        barcodeView.addStateListener(object: CameraPreview.StateListener {
            override fun cameraClosed() {
                torchOn = false
                isPreviewing = false
            }

            override fun cameraError(error: Exception?) {
                torchOn = false
                isPreviewing = false
                Log.e("CodeScanner", error.toString())
            }

            override fun previewStopped() {
                torchOn = false
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

                    guideView.y = bottom + guideMarginTop

                    torchButton.y = top - torchMarginBottom - torchButtonHeight

                    laserView.layoutParams.width = (right - left - 2 * laserGap).toInt()
                    laserView.x = left + laserGap

                }
            }
        })

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
     * Start the camera preview and decoding. Typically this should be called from the Activity's
     * onResume() method.
     *
     * Call from UI thread only.
     */
    fun resume() {
        barcodeView.resume()
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
     * Pause scanning and preview; waiting for the Camera to be closed.
     *
     * This blocks the main thread.
     */
    fun pauseAndWait() {
        barcodeView.pauseAndWait()
    }

}