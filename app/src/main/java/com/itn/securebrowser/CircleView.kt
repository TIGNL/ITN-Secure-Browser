package com.itn.securebrowser

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class CircleView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFFFFFF.toInt()
        style = Paint.Style.STROKE
        strokeWidth = 6f
    }

    override fun onDraw(canvas: Canvas) {
        val r = (minOf(width, height) / 2f) - paint.strokeWidth
        canvas.drawCircle(width / 2f, height / 2f, r, paint)
    }
}

