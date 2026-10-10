package com.ngocdevv.purchase

import android.animation.ValueAnimator
import android.content.Context
import android.provider.Settings
import android.view.View
import android.view.animation.Interpolator
import kotlin.math.exp
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

internal fun Context.dp(value: Float) = value * resources.displayMetrics.density
internal fun Context.dp(value: Int) = dp(value.toFloat()).toInt()

/** Analytical solution to the same damped spring used by the Reanimated source. */
internal class SoftSpring(private val seconds: Double = 7.0, mass: Double = 1.0) : Interpolator {
    private val damping = 10.0 / mass
    private val stiffness = 10.0 / mass
    private val discriminant = damping * damping - 4.0 * stiffness
    override fun getInterpolation(input: Float): Float {
        if (input >= 1f) return 1f
        val time = input * seconds
        val remaining = when {
            discriminant > 1e-8 -> {
                val root = sqrt(discriminant)
                val r1 = (-damping + root) / 2.0
                val r2 = (-damping - root) / 2.0
                (r2 * exp(r1 * time) - r1 * exp(r2 * time)) / (r2 - r1)
            }
            discriminant < -1e-8 -> {
                val decay = damping / 2.0
                val frequency = sqrt(stiffness - decay * decay)
                exp(-decay * time) * (cos(frequency * time) + decay / frequency * sin(frequency * time))
            }
            else -> exp(-damping * time / 2.0) * (1.0 + damping * time / 2.0)
        }
        return (1.0 - remaining).toFloat()
    }
}

internal fun Context.motionEnabled(): Boolean =
    Settings.Global.getFloat(contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) > 0f

internal fun View.enter(distanceDp: Float, delay: Long = 0L, zoom: Boolean = false, mass: Double = if (zoom) .7 else 1.0): ValueAnimator? {
    if (!context.motionEnabled()) return null
    alpha = 0f
    translationY = context.dp(distanceDp)
    if (zoom) { scaleX = 0f; scaleY = 0f }
    return ValueAnimator.ofFloat(0f, 1f).apply {
        duration = 7000
        startDelay = delay
        interpolator = SoftSpring(mass = mass)
        addUpdateListener {
            val progress = it.animatedValue as Float
            alpha = progress.coerceIn(0f, 1f)
            translationY = context.dp(distanceDp) * (1f - progress)
            if (zoom) { scaleX = progress; scaleY = progress }
        }
        start()
    }
}
