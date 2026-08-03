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
        textPaint.textSize      = 8f * density   // ثابت دائماً
    }

    override fun onDraw(canvas: Canvas) {
        val pad = strokePaint.strokeWidth + 2f
        rect.set(pad, pad, width - pad, height - pad)
        canvas.drawRect(rect, strokePaint)

        val label = if (tabCount > 9) "+9" else tabCount.toString()

        textPaint.getTextBounds(label, 0, label.length, bounds)
        val cx = width / 2f
        val cy = height / 2f + bounds.height() / 2f - bounds.bottom

        canvas.drawText(label, cx, cy, textPaint)
    }
}
