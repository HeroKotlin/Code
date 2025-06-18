package com.github.herokotlin.code

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.RectF
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.RelativeLayout
import com.github.herokotlin.code.databinding.CodeScannerBinding
import com.google.zxing.BarcodeFormat
import com.google.zxing.ResultPoint
import com.google.zxing.client.android.Intents
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.CameraPreview
import com.journeyapps.barcodescanner.DefaultDecoderFactory
import java.lang.Exception

open class CodeScanner: RelativeLayout {

    var guideTitle = ""

        set(value) {
            if (field == value) {
                return
            }
            field = value
            binding.guideLabel.text = value
        }

    lateinit var callback: CodeScannerCallback

    private lateinit var binding: CodeScannerBinding

    private var supportedCodeType = listOf(BarcodeFormat.QR_CODE, BarcodeFormat.CODE_39, BarcodeFormat.CODE_93,
        BarcodeFormat.CODE_128, BarcodeFormat.EAN_8, BarcodeFormat.EAN_13, BarcodeFormat.UPC_E)

    private var isTorchOn = false

        set(value) {

            if (field == value) {
                return
            }
            field = value

            binding.barcodeView.setTorch(isTorchOn)

            if (isTorchOn) {
                binding.torchButton.setImageResource(R.drawable.code_scanner_torch_off)
            }
            else {
                binding.torchButton.setImageResource(R.drawable.code_scanner_torch_on)
            }

        }

    private var isPreviewing = false

        set(value) {
            if (field == value) {
                return
            }
            field = value
            if (value) {
                binding.guideLabel.visibility = View.VISIBLE
                binding.torchButton.visibility = View.VISIBLE
                binding.laserView.visibility = View.VISIBLE
                startLaser()
            }
            else {
                binding.guideLabel.visibility = View.GONE
                binding.torchButton.visibility = View.GONE
                binding.laserView.visibility = View.GONE
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

        binding = CodeScannerBinding.inflate(LayoutInflater.from(context), this, true)

        binding.barcodeView.decoderFactory = DefaultDecoderFactory(supportedCodeType, null, null, Intents.Scan.MIXED_SCAN)

        binding.torchButton.setOnClickListener {
            isTorchOn = !isTorchOn
        }

        binding.barcodeView.addStateListener(object: CameraPreview.StateListener {
            override fun cameraClosed() {
                isTorchOn = false
                isPreviewing = false
            }

            override fun cameraError(error: Exception?) {
                isTorchOn = false
                isPreviewing = false
            }

            override fun previewStopped() {
                isTorchOn = false
                isPreviewing = false
            }

            override fun previewStarted() {
                isPreviewing = true
            }

            override fun previewSized() {
                val rect = binding.barcodeView.framingRect
                if (rect != null) {

                    // 兼容某些手机不走正常流程
                    isPreviewing = true

                    val left = rect.left.toFloat()
                    val top = rect.top.toFloat()
                    val right = rect.right.toFloat()
                    val bottom = rect.bottom.toFloat()

                    binding.viewFinder.box = RectF(left, top, right, bottom)
                    binding.viewFinder.invalidate()

                    binding.guideLabel.y = bottom + guideLabelMarginTop

                    binding.torchButton.y = top - torchMarginBottom - torchButtonHeight

                    binding.laserView.layoutParams.width = (right - left - 2 * laserGap).toInt()
                    binding.laserView.x = left + laserGap

                }
            }
        })

    }

    open fun destroy() {
        stop()
        isPreviewing = false
    }

    private fun startLaser() {

        val animator = ValueAnimator.ofFloat(binding.viewFinder.box.top, binding.viewFinder.box.bottom - laserHeight)
        animator.duration = 3000
        animator.interpolator = LinearInterpolator()
        animator.addUpdateListener {
            binding.laserView.y = it.animatedValue as Float
        }
        animator.addListener(object: AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                if (binding.laserView.visibility == View.VISIBLE) {
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

    fun start() {
        binding.barcodeView.decodeContinuous(barcodeCallback)
        binding.barcodeView.resume()
    }

    fun stop() {
        binding.barcodeView.pause()
        binding. barcodeView.stopDecoding()
    }

    /**
     * Start the camera preview and decoding. Typically this should be called from the Activity's
     * onResume() method.
     *
     * Call from UI thread only.
     */
    fun resume() {
        binding.barcodeView.resume()
    }

    /**
     * Stops the live preview and decoding.
     *
     * Call from the Activity's onPause() method.
     */
    fun pause() {
        binding.barcodeView.pause()
    }

}