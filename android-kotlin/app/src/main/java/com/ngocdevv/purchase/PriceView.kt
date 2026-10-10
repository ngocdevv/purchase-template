package com.ngocdevv.purchase

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.view.animation.PathInterpolator
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView

internal class PriceView(context: Context) : LinearLayout(context) {
    private val digits = mutableListOf<Pair<TextView, TextView>>()
    private val animations = mutableListOf<ValueAnimator>()
    private var flip = true

    init {
        orientation = HORIZONTAL
        gravity = Gravity.TOP
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_YES
        val currency = glyph("$", 24f).apply { typeface = Typeface.create("sans-serif", Typeface.BOLD) }
        addView(currency, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
            topMargin = context.dp(10); rightMargin = context.dp(4)
        })
        repeat(2) {
            val cell = FrameLayout(context)
            val top = glyph("0", 56f)
            val bottom = glyph("0", 56f)
            cell.addView(top)
            cell.addView(bottom)
            addView(cell, LayoutParams(LayoutParams.WRAP_CONTENT, context.dp(61f * resources.configuration.fontScale).toInt()))
            digits += top to bottom
        }
        setPrice("25", animate = false)
    }

    private fun glyph(value: String, size: Float) = TextView(context).apply {
        text = value; textSize = size
        setTextColor(Color.WHITE)
        typeface = Typeface.create("sans-serif", Typeface.BOLD)
        includeFontPadding = false
        importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        cameraDistance = context.dp(1000f)
    }

    fun setPrice(value: String, animate: Boolean = true) {
        animations.forEach { it.cancel() }; animations.clear()
        contentDescription = "$$value"
        digits.forEachIndexed { index, (top, bottom) ->
            top.text = value[index].toString()
            bottom.text = value[index].toString()
            if (!animate || !context.motionEnabled()) {
                setProgress(top, bottom, 1f)
            } else {
                // Both faces carry the new digit, exactly as PriceAnimation.tsx.
                val from = if (flip) 0f else 1f
                val to = 1f - from
                setProgress(top, bottom, from)
                animations += ValueAnimator.ofFloat(from, to).apply {
                    startDelay = index * 40L
                    duration = 350
                    interpolator = PathInterpolator(.2f, 0f, .1f, 1f)
                    addUpdateListener { setProgress(top, bottom, it.animatedValue as Float) }
                    start()
                }
            }
        }
        if (animate) flip = !flip
    }

    private fun setProgress(top: TextView, bottom: TextView, progress: Float) {
        val halfHeight = context.dp(61f * resources.configuration.fontScale) / 2
        top.alpha = 1f - progress
        top.rotationX = progress * 90
        top.translationY = -progress * halfHeight
        bottom.alpha = progress
        bottom.rotationX = -90f * (1f - progress)
        bottom.translationY = halfHeight * (1f - progress)
    }

    override fun onDetachedFromWindow() {
        animations.forEach { it.cancel() }
        super.onDetachedFromWindow()
    }
}
