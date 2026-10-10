package com.ngocdevv.purchase

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RuntimeShader
import android.os.Build
import android.view.Choreographer
import android.view.View
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sin

/** The Skia fabric effect ported to AGSL. The denominator deliberately uses screen size. */
internal class FabricView(context: Context) : View(context), Choreographer.FrameCallback {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val gpu = if (Build.VERSION.SDK_INT >= 33) GpuFabric() else null
    private val fallback = if (gpu == null) Bitmap.createBitmap(84, 60, Bitmap.Config.ARGB_8888) else null
    private val pixels = IntArray(84 * 60)
    private val destination = Rect()
    private var color = floatArrayOf(.9f, .6f, .7f)
    private var active = false
    private var scheduled = false
    private var lastFrame = 0L
    private var time = 0f
    private var fallbackDrawnAt = -1f
    var screenWidth = 1f
    var screenHeight = 1f

    init { importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO }

    fun setColor(value: FloatArray) { color = value; fallbackDrawnAt = -1f; invalidate() }

    fun setRunning(value: Boolean) {
        active = value && context.motionEnabled()
        lastFrame = 0L
        if (active && isAttachedToWindow && !scheduled) {
            scheduled = true
            Choreographer.getInstance().postFrameCallback(this)
        } else if (!active) {
            Choreographer.getInstance().removeFrameCallback(this)
            scheduled = false
        }
    }

    override fun onAttachedToWindow() { super.onAttachedToWindow(); setRunning(active) }
    override fun onDetachedFromWindow() {
        Choreographer.getInstance().removeFrameCallback(this)
        scheduled = false; lastFrame = 0
        super.onDetachedFromWindow()
    }

    override fun doFrame(frameTimeNanos: Long) {
        scheduled = false
        if (!active || !isAttachedToWindow || !isShown || windowVisibility != VISIBLE) { lastFrame = 0; return }
        if (lastFrame != 0L) time += ((frameTimeNanos - lastFrame) / 1_000_000_000.0).toFloat()
        lastFrame = frameTimeNanos
        if (gpu != null || time - fallbackDrawnAt >= 1f / 30f) invalidate()
        scheduled = true
        Choreographer.getInstance().postFrameCallback(this)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (gpu != null) {
            gpu.draw(canvas, paint, width.toFloat(), height.toFloat(), screenWidth, screenHeight, color, time)
        } else {
            val bitmap = fallback ?: return
            if (fallbackDrawnAt < 0f || time - fallbackDrawnAt >= 1f / 30f) {
                for (y in 0 until 60) for (x in 0 until 84) {
                    val u = (x + .5f) * width / 84f / max(screenWidth, 1f)
                    val v = (y + .5f) * height / 60f / max(screenHeight, 1f)
                    pixels[y * 84 + x] = sample(u.toDouble(), v.toDouble())
                }
                bitmap.setPixels(pixels, 0, 84, 0, 0, 84, 60)
                fallbackDrawnAt = time
            }
            destination.set(0, 0, width, height)
            canvas.drawBitmap(bitmap, null, destination, paint)
        }
    }

    // API 26–32 keeps the same wave/lighting equations, sampled at 84×60 and filtered.
    private fun sample(u: Double, v: Double): Int {
        val t = time.toDouble()
        val wave1 = sin(v * 10 + t * 2) * cos(u * 8 - t * 1.5)
        val wave2 = sin(v * 15 + t * 1.5) * sin(u * 12 - t * 2)
        val wave3 = cos(v * 8 + t) * sin(u * 6 - t * 1.8)
        val displacement = (wave1 * .4 + wave2 * .35 + wave3 * .25) * .5
        val wrinkle = sin(v * 30 + displacement * 10) * sin(u * 25 - t * 2 + displacement * 8) * .15
        val diffuseInput = ((u * .1 + 1 + displacement * .6 - .3) / .7).coerceIn(0.0, 1.0)
        val diffuse = diffuseInput * diffuseInput * (3 - 2 * diffuseInput)
        val specular = max(displacement + wrinkle, 0.0).pow(3) * .3
        val ao = (1 - abs(displacement) * .4).coerceIn(.6, 1.0)
        val light = ((diffuse * .8 + specular + wrinkle) * ao).coerceIn(.4, 1.15)
        fun channel(i: Int): Int {
            val shadow = .9 * .2 + color[i] * .8
            val col = (shadow * (1 - light) + color[i] * light) * (.8 + light * .3) + .06 * sin(displacement * 5)
            return (col.coerceIn(0.0, 1.0) * 255).toInt()
        }
        return Color.rgb(channel(0), channel(1), channel(2))
    }

    @android.annotation.TargetApi(33)
    private class GpuFabric {
        private val shader = RuntimeShader(SOURCE)
        fun draw(canvas: Canvas, paint: Paint, width: Float, height: Float,
                 screenWidth: Float, screenHeight: Float, color: FloatArray, time: Float) {
            shader.setFloatUniform("uResolution", screenWidth, screenHeight, screenWidth / screenHeight)
            shader.setFloatUniform("uTime", time)
            shader.setFloatUniform("uColor", color)
            shader.setFloatUniform("uAmplitude", .5f)
            shader.setFloatUniform("uSpeed", 1f)
            paint.shader = shader
            canvas.drawRect(0f, 0f, width, height, paint)
        }
    }

    companion object {
        // Only vec types/main signature change from SkSL; every numerical expression is preserved.
        private const val SOURCE = """
            uniform float3 uResolution;
            uniform float uTime;
            uniform float3 uColor;
            uniform float uAmplitude;
            uniform float uSpeed;
            half4 main(float2 fragCoord) {
              float2 uv = fragCoord / uResolution.xy;
              float time = uTime * uSpeed;
              float wave1 = sin(uv.y * 10.0 + time * 2.0) * cos(uv.x * 8.0 - time * 1.5);
              float wave2 = sin(uv.y * 15.0 + time * 1.5) * sin(uv.x * 12.0 - time * 2.0);
              float wave3 = cos(uv.y * 8.0 + time * 1.0) * sin(uv.x * 6.0 - time * 1.8);
              float displacement = (wave1 * 0.4 + wave2 * 0.35 + wave3 * 0.25) * uAmplitude;
              float gradient = uv.x * 0.1 + 1;
              float wrinkle = sin(uv.y * 30.0 + displacement * 10.0) *
                              sin(uv.x * 25.0 - time * 2.0 + displacement * 8.0) * 0.15;
              float diffuse = gradient + displacement * 0.6;
              diffuse = smoothstep(0.3, 1.0, diffuse);
              float specular = pow(max(displacement + wrinkle, 0.0), 3.0) * 0.3;
              float ao = 1.0 - (abs(displacement) * 0.4);
              ao = clamp(ao, 0.6, 1.0);
              float lighting = (diffuse * 0.8 + specular + wrinkle) * ao;
              lighting = clamp(lighting, 0.4, 1.15);
              float3 shadowTint = mix(float3(0.9), uColor, 0.8);
              float3 col = mix(shadowTint, uColor, lighting);
              col = col * (0.8 + lighting * 0.3);
              col += float3(0.06) * sin(displacement * 5.0);
              return half4(col, 1.0);
            }
        """
    }
}
