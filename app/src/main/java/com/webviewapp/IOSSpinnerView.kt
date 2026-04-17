package com.webviewapp

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.View

class IOSSpinnerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val lineCount = 12
    private val dp = resources.displayMetrics.density

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color       = Color.BLACK
        strokeWidth = 2f * dp
        strokeCap   = Paint.Cap.ROUND
        style       = Paint.Style.STROKE
    }

    private var currentIndex = 0
    private val handler = Handler(Looper.getMainLooper())

    private val runnable = object : Runnable {
        override fun run() {
            currentIndex = (currentIndex + 1) % lineCount
            invalidate()
            handler.postDelayed(this, 1000L / lineCount)
        }
    }

    override fun onDraw(canvas: Canvas) {
        canvas.save()
        canvas.translate(width / 2f, height / 2f)
        for (i in 0 until lineCount) {
            val steps = (currentIndex - i + lineCount) % lineCount
            paint.alpha = ((lineCount - steps).toFloat() / lineCount * 255).toInt().coerceIn(30, 255)
            canvas.drawLine(0f, -9f * dp, 0f, -16f * dp, paint)
            canvas.rotate(360f / lineCount)
        }
        canvas.restore()
    }

    fun start() { handler.removeCallbacks(runnable); handler.post(runnable) }
    fun stop()  { handler.removeCallbacks(runnable) }
}
