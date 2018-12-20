package com.github.herokotlin.code

import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View

internal class ViewFinder : View {

    companion object {

        var DEFAULT_MASK_COLOR = Color.parseColor("#22000000")

        var DEFAULT_CORNER_COLOR = Color.parseColor("#1485E1")

        var DEFAULT_CORNER_WIDTH = 2

        var DEFAULT_CORNER_SIZE = 20

        var DEFAULT_BORDER_WIDTH = 1

        var DEFAULT_BORDER_COLOR = Color.parseColor("#CCFFFFFF")

    }

    var maskColor = DEFAULT_MASK_COLOR

    var cornerColor = DEFAULT_CORNER_COLOR

    var cornerWidth = DEFAULT_CORNER_WIDTH

    var cornerSize = DEFAULT_CORNER_SIZE

    var borderWidth = DEFAULT_BORDER_WIDTH

    var borderColor = DEFAULT_BORDER_COLOR

    var box = RectF()

    /**
     * 视图的尺寸
     */
    private var viewRect = RectF(0f, 0f, 0f, 0f)

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(attrs, defStyle)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {

        val typedArray = context.obtainStyledAttributes(
            attrs, R.styleable.ViewFinder, defStyle, 0)

        maskColor = typedArray.getInt(R.styleable.ViewFinder_view_finder_mask_color, DEFAULT_MASK_COLOR)

        cornerColor = typedArray.getInt(R.styleable.ViewFinder_view_finder_corner_color, DEFAULT_CORNER_COLOR)

        cornerWidth = typedArray.getDimensionPixelSize(
            R.styleable.ViewFinder_view_finder_corner_width,
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_CORNER_WIDTH.toFloat(), resources.displayMetrics).toInt()
        )

        cornerSize = typedArray.getDimensionPixelSize(
            R.styleable.ViewFinder_view_finder_corner_size,
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_CORNER_SIZE.toFloat(), resources.displayMetrics).toInt()
        )

        borderWidth = typedArray.getDimensionPixelSize(
            R.styleable.ViewFinder_view_finder_border_width,
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_BORDER_WIDTH.toFloat(), resources.displayMetrics).toInt()
        )

        borderColor = typedArray.getInt(R.styleable.ViewFinder_view_finder_border_color, DEFAULT_BORDER_COLOR)

        // 获取完 TypedArray 的值后，
        // 一般要调用 recycle 方法来避免重新创建的时候出错
        typedArray.recycle()

    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        viewRect.right = w.toFloat()
        viewRect.bottom = h.toFloat()
    }

    override fun onDraw(canvas: Canvas) {

        super.onDraw(canvas)

        if (box.width() == 0f || box.height() == 0f) {
            return
        }

        // save 和 restore 相当于一组操作
        canvas.save()

        // 遮罩
        paint.style = Paint.Style.FILL
        paint.color = maskColor

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            canvas.clipOutRect(box)
        }
        else {
            // 两次调用 clipRect 实现一个镂空的矩形
            canvas.clipRect(viewRect)
            canvas.clipRect(box, Region.Op.DIFFERENCE)
        }

        // 在镂空的矩形上画图
        canvas.drawRect(viewRect, paint)

        canvas.restore()

        val strokeWidth = borderWidth.toFloat()
        if (strokeWidth > 0) {

            // 取景框边框
            paint.style = Paint.Style.STROKE
            paint.color = borderColor
            paint.strokeWidth = strokeWidth

            val halfBorderWidth = strokeWidth / 2
            canvas.drawRect(
                box.left + halfBorderWidth,
                box.top + halfBorderWidth,
                box.right - halfBorderWidth,
                box.bottom - halfBorderWidth,
                paint
            )

        }

        // 四个角
        paint.style = Paint.Style.FILL
        paint.color = cornerColor

        var left = box.left
        var top = box.top

        // 左上
        canvas.drawRect(left, top, left + cornerSize, top + cornerWidth, paint)

        canvas.drawRect(left, top + cornerWidth, left + cornerWidth, top + cornerSize, paint)

        left = box.right - cornerSize

        // 右上
        canvas.drawRect(left, top, box.right, top + cornerWidth, paint)

        left = box.right - cornerWidth
        canvas.drawRect(left, top + cornerWidth, box.right, top + cornerSize, paint)


        top = box.bottom - cornerSize
        // 右下
        canvas.drawRect(left, top, box.right, box.bottom, paint)

        left = box.right - cornerSize
        top = box.bottom - cornerWidth
        canvas.drawRect(left, top, box.right - cornerWidth, box.bottom, paint)


        // 左下
        left = box.left
        top = box.bottom - cornerSize

        canvas.drawRect(left, top, left + cornerWidth, box.bottom, paint)

        canvas.drawRect(left + cornerWidth, box.bottom - cornerWidth, box.left + cornerSize, box.bottom, paint)

    }

}
