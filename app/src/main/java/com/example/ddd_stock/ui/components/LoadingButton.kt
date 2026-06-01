package com.example.ddd_stock.ui.components
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import com.example.ddd_stock.R

class LoadingButton @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = androidx.appcompat.R.attr.buttonStyle
) : AppCompatButton(context, attrs, defStyleAttr) {
    private var isLoading = false
    private var progressAngle = 0f
    private val spinnerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = ContextCompat.getColor(context, R.color.white); style = Paint.Style.STROKE; strokeWidth = 4f }
    private var animator: ValueAnimator? = null
    private var originalText = ""

    fun showLoading() {
        if (isLoading) return; isLoading = true; originalText = text.toString(); isEnabled = false; text = ""
        animator?.cancel(); animator = ValueAnimator.ofFloat(0f, 360f).apply {
            duration = 1000; repeatCount = ValueAnimator.INFINITE
            addUpdateListener { progressAngle = it.animatedValue as Float; invalidate() }; start()
        }
    }

    fun hideLoading() {
        if (!isLoading) return; isLoading = false; animator?.cancel(); animator = null; progressAngle = 0f; text = originalText; isEnabled = true; invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (isLoading) {
            val r = minOf(width, height) / 3f; val cx = width / 2f; val cy = height / 2f
            canvas.drawArc(RectF(cx - r, cy - r, cx + r, cy + r), progressAngle, 270f, false, spinnerPaint)
        }
    }

    override fun onDetachedFromWindow() { super.onDetachedFromWindow(); animator?.cancel() }
}
