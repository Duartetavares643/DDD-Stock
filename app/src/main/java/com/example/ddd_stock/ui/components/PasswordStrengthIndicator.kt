package com.example.ddd_stock.ui.components

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.example.ddd_stock.R
import com.example.ddd_stock.util.ValidationUtils
import kotlin.math.roundToInt

class PasswordStrengthIndicator @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var currentStrength = ValidationUtils.PasswordStrength.WEAK
    private var animatedProgress = 0f

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.dark_surface)
        style = Paint.Style.FILL
    }
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val cornerRadius = 8f

    fun setStrength(strength: ValidationUtils.PasswordStrength) {
        currentStrength = strength
        val targetProgress = when (strength) {
            ValidationUtils.PasswordStrength.WEAK -> 0.25f
            ValidationUtils.PasswordStrength.MEDIUM -> 0.5f
            ValidationUtils.PasswordStrength.STRONG -> 0.75f
            ValidationUtils.PasswordStrength.VERY_STRONG -> 1.0f
        }
        ValueAnimator.ofFloat(animatedProgress, targetProgress).apply {
            duration = 300
            addUpdateListener { anim ->
                animatedProgress = anim.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val width = width.toFloat()
        val height = height.toFloat()
        val rectF = RectF(0f, 0f, width, height)

        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, backgroundPaint)

        val filledWidth = width * animatedProgress
        if (filledWidth > 0f) {
            fillPaint.color = getStrengthColor()
            canvas.drawRoundRect(
                RectF(0f, 0f, filledWidth, height),
                cornerRadius, cornerRadius, fillPaint
            )
        }
    }

    private fun getStrengthColor(): Int {
        return when (currentStrength) {
            ValidationUtils.PasswordStrength.WEAK -> ContextCompat.getColor(context, R.color.strength_weak)
            ValidationUtils.PasswordStrength.MEDIUM -> ContextCompat.getColor(context, R.color.strength_medium)
            ValidationUtils.PasswordStrength.STRONG -> ContextCompat.getColor(context, R.color.strength_strong)
            ValidationUtils.PasswordStrength.VERY_STRONG -> ContextCompat.getColor(context, R.color.strength_very_strong)
        }
    }
}
