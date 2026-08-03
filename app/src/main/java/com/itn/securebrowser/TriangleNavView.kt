package com.itn.securebrowser

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View

/**
 * مثلث فارغ (stroke فقط) يستخدم كزر الخلف/الأمام في شريط التنقل السفلي.
 * الاتجاه: يشير إلى اليسار افتراضياً (خلف).
 * يمكن عكسه بـ scaleX = -1 من الكود لزر الأمام.
 */
class TriangleNavView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color       = 0xFFFFFFFF.toInt()
        style       = Paint.Style.STROKE
        strokeWidth = 6f
        strokeJoin  = Paint.Join.ROUND
        strokeCap   = Paint.Cap.ROUND
    }

    private val path = Path()

    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        val pad = paint.strokeWidth + 2f

        path.reset()
        // مثلث يشير لليسار: قمته على اليسار، قاعدته على اليمين
        path.moveTo(pad,         h / 2f)       // الرأس (يسار - وسط)
        path.lineTo(w - pad,     pad)           // أعلى اليمين
        path.lineTo(w - pad,     h - pad)       // أسفل اليمين
        path.close()

        canvas.drawPath(path, paint)
    }
}
