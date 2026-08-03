package com.itn.securebrowser

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View

class TriangleNavView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color      = 0xFFFFFFFF.toInt()
        style      = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap  = Paint.Cap.ROUND
    }

    private val path = Path()

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        paint.strokeWidth = context.resources.displayMetrics.density * 1.5f
    }

    override fun onDraw(canvas: Canvas) {
        val w   = width.toFloat()
        val h   = height.toFloat()
        val pad = paint.strokeWidth + 2f

        path.reset()
        path.moveTo(pad,     h / 2f)   // رأس يسار
        path.lineTo(w - pad, pad)       // أعلى يمين
        path.lineTo(w - pad, h - pad)  // أسفل يمين
        path.close()

        canvas.drawPath(path, paint)
    }
}
