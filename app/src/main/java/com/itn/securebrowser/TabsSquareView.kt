package com.itn.securebrowser

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View

class TabsSquareView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    var tabCount: Int = 1
        set(value) { field = value; invalidate() }

    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color      = 0xFFFFFFFF.toInt()
        style      = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color     = 0xFFFFFFFF.toInt()
        typeface  = Typeface.DEFAULT
        textAlign = Paint.Align.CENTER
    }

    private val rect   = RectF()
    private val bounds = Rect()

    private val density get() = context.resources.displayMetrics.density

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        strokePaint.strokeWidth = density * 1.5f
    }

    override fun onDraw(canvas: Canvas) {
        val pad = strokePaint.strokeWidth + 2f
        rect.set(pad, pad, width - pad, height - pad)
        canvas.drawRect(rect, strokePaint)

        val label: String
        val spSize: Float

        if (tabCount > 99) {
            label  = "+99"
            spSize = 4f
        } else {
            label  = tabCount.toString()
            spSize = 8f
        }

        textPaint.textSize = spSize * density
        textPaint.getTextBounds(label, 0, label.length, bounds)

        val cx = width / 2f
        val cy = height / 2f + bounds.height() / 2f - bounds.bottom

        canvas.drawText(label, cx, cy, textPaint)
    }
}
