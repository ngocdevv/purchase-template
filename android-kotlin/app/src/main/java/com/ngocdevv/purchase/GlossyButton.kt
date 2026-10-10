package com.ngocdevv.purchase

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Outline
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.Gravity
import android.view.MotionEvent
import android.widget.TextView
import kotlin.math.max

/** Native text button with the source's inset glossy surface and press scale. */
internal class GlossyButton(context: Context, label: String) : TextView(context) {
    init {
        text = label
        textSize = 16f
        setTextColor(Color.rgb(59, 59, 66))
        typeface = Typeface.create("sans-serif", Typeface.BOLD)
        gravity = Gravity.CENTER
        includeFontPadding = false
        setPadding(context.dp(52), context.dp(18), context.dp(52), context.dp(18))
        minHeight = context.buttonHeight()
        background = GlossySurface(context)
        elevation = context.dp(6f)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            outlineAmbientShadowColor = Color.rgb(124, 124, 127)
            outlineSpotShadowColor = Color.rgb(124, 124, 127)
        }
        isClickable = true
        isFocusable = true
        accessibilityDelegate = object : android.view.View.AccessibilityDelegate() {
            override fun onInitializeAccessibilityNodeInfo(host: android.view.View, info: android.view.accessibility.AccessibilityNodeInfo) {
                super.onInitializeAccessibilityNodeInfo(host, info)
                info.className = "android.widget.Button"
            }
        }
    }

    override fun setPressed(pressed: Boolean) {
        super.setPressed(pressed)
        (background as? GlossySurface)?.let { surface ->
            surface.pressed = pressed
            surface.invalidateSelf()
            // Android derives shadow blur/offset from elevation; use a smaller
            // elevation for the source's 12→6 blur and 8→3 offset on press.
            elevation = context.dp(if (pressed) 3f else 6f)
            invalidateOutline()
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (context.motionEnabled()) {
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> animate().scaleX(.97f).scaleY(.97f).setDuration(110).start()
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> animate().scaleX(1f).scaleY(1f).setDuration(220).start()
            }
        }
        return super.onTouchEvent(event)
    }
    override fun performClick(): Boolean = super.performClick()

    private class GlossySurface(private val context: Context) : Drawable() {
        var pressed = false
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val rect = RectF()
        override fun draw(canvas: Canvas) {
            rect.set(bounds)
            paint.color = if (pressed) Color.rgb(225, 225, 231) else Color.rgb(231, 231, 235)
            canvas.drawRoundRect(rect, rect.height()/2, rect.height()/2, paint)
            rect.inset(context.dp(4f), context.dp(4f))
            paint.color = Color.rgb(249, 249, 251)
            canvas.drawRoundRect(rect, rect.height()/2, rect.height()/2, paint)
            paint.color = Color.argb(217, 255, 255, 255)
            rect.set(bounds.left + context.dp(14f), bounds.top + context.dp(8f),
                bounds.right - context.dp(14f), bounds.top + context.dp(24f))
            canvas.drawRoundRect(rect, context.dp(16f), context.dp(16f), paint)
        }
        override fun setAlpha(alpha: Int) {}
        override fun getOutline(outline: Outline) {
            outline.setRoundRect(bounds, bounds.height() / 2f)
            // Keep the native resting shadow and halve its strength on press
            // (the RN shadowOpacity changes from .25 to .12).
            outline.alpha = if (pressed) .12f / .25f else 1f
        }
        override fun setColorFilter(colorFilter: android.graphics.ColorFilter?) {}
        @Deprecated("Deprecated in Android") override fun getOpacity() = android.graphics.PixelFormat.TRANSLUCENT
    }
}

internal fun Context.buttonHeight() = dp(54f + max(0f, resources.configuration.fontScale - 1f) * 22f).toInt()
