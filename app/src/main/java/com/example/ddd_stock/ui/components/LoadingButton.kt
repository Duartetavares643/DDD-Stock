package com.example.ddd_stock.ui.components

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import com.example.ddd_stock.R

class LoadingButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = androidx.appcompat.R.attr.buttonStyle
) : AppCompatButton(context, attrs, defStyleAttr) {

    private var isLoading = false
    private var progressAngle = 0f
    private val spinnerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.white)
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }
    private var animator: ValueAnimator? = null
    private var originalText: CharSequence = ""
    private var originalEnabled = true

    fun showLoading() {
        if (isLoading) return
        isLoading = true
        originalText = text
        originalEnabled = isEnabled
        isEnabled = false
        text = ""

        animator?.cancel()
        animator = ValueAnimator.ofFloat(0f, 360f).apply {
            duration = 1000
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener { anim ->
                progressAngle = anim.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    fun hideLoading() {
        if (!isLoading) return
        isLoading = false
        animator?.cancel()
        animator = null
        progressAngle = 0f
        text = originalText
        isEnabled = originalEnabled
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (isLoading) {
            val centerX = (width / 2).toFloat()
            val centerY = (height / 2).toFloat()
            val radius = minOf(width, height) / 3f
            val rectF = RectF(
                centerX - radius,
                centerY - radius,
                centerX + radius,
                centerY + radius
            )
            canvas.drawArc(rectF, progressAngle, 270f, false, spinnerPaint)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animator?.cancel()
    }
}
