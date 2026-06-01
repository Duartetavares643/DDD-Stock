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

class PasswordStrengthIndicator @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var strength = ValidationUtils.PasswordStrength.WEAK
    private var progress = 0f
    private val bg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = ContextCompat.getColor(context, R.color.dark_surface); style = Paint.Style.FILL }
    private val fg = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val r = 8f

    fun setStrength(s: ValidationUtils.PasswordStrength) {
        strength = s
        val t = when (s) { ValidationUtils.PasswordStrength.WEAK -> 0.25f; ValidationUtils.PasswordStrength.MEDIUM -> 0.5f; ValidationUtils.PasswordStrength.STRONG -> 0.75f; ValidationUtils.PasswordStrength.VERY_STRONG -> 1.0f }
        ValueAnimator.ofFloat(progress, t).apply { duration = 300; addUpdateListener { progress = it.animatedValue as Float; invalidate() }; start() }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas); val w = width.toFloat(); val h = height.toFloat()
        canvas.drawRoundRect(RectF(0f, 0f, w, h), r, r, bg)
        val fw = w * progress
        if (fw > 0f) { fg.color = color(); canvas.drawRoundRect(RectF(0f, 0f, fw, h), r, r, fg) }
    }

    private fun color() = ContextCompat.getColor(context, when (strength) {
        ValidationUtils.PasswordStrength.WEAK -> R.color.strength_weak; ValidationUtils.PasswordStrength.MEDIUM -> R.color.strength_medium
        ValidationUtils.PasswordStrength.STRONG -> R.color.strength_strong; ValidationUtils.PasswordStrength.VERY_STRONG -> R.color.strength_very_strong
    })
}
