package com.ngocdevv.purchase

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.os.Build
import android.view.Gravity
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import android.view.accessibility.AccessibilityNodeInfo
import android.view.animation.PathInterpolator
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import kotlin.math.abs
import kotlin.math.max

internal enum class Plan(val price: String, val fabricColor: FloatArray) {
    YEARLY("25", floatArrayOf(.9f, .6f, .7f)),
    MONTHLY("18", floatArrayOf(.1f, .2f, .4f))
}

internal class PurchaseSheet(context: Context, initialPlan: Plan,
                             private val onPlanSelected: (Plan) -> Unit,
                             private val onDismissRequested: () -> Unit) : FrameLayout(context) {
    private val scroll = ScrollView(context)
    private val body = LinearLayout(context)
    // A group alpha would allocate a bounded offscreen layer and crop the translating card.
    // Children occupy separate layout rows; applying alpha without that layer preserves overflow.
    private val upper = object : LinearLayout(context) {
        override fun hasOverlappingRendering() = false
    }
    private val fabric = FabricView(context)
    private val price = PriceView(context)
    private val title: TextView
    private val subtitle: TextView
    private val segment: SegmentControl
    private val card = FrameLayout(context)
    private val cardEntrance = FrameLayout(context)
    private val benefits = LinearLayout(context)
    private val trial = GlossyButton(context, "Start free trial")
    private val entrances = mutableListOf<ValueAnimator>()
    private var selected = initialPlan
    private var dragStartY = 0f
    private var dragStartX = 0f
    private var dragging = false
    private var velocity: VelocityTracker? = null
    private var dismissed = false

    init {
        background = context.rounded(Color.WHITE, 42f).apply {
            val radius = context.dp(42f)
            cornerRadii = floatArrayOf(radius, radius, radius, radius, 0f, 0f, 0f, 0f)
        }
        clipToOutline = true
        elevation = context.dp(8f)
        if (Build.VERSION.SDK_INT >= 28) accessibilityPaneTitle = "Purchase options"
        val handle = FrameLayout(context).apply {
            contentDescription = "Dismiss purchase options"
            isClickable = true; isFocusable = true
            setOnClickListener { onDismissRequested() }
        }
        val line = View(context).apply { background = context.rounded(Color.rgb(211, 211, 211), 2f) }
        handle.addView(line, LayoutParams(context.dp(46), context.dp(4), Gravity.CENTER).apply { topMargin = context.dp(4) })
        addView(handle, LayoutParams(LayoutParams.MATCH_PARENT, context.dp(24)))

        scroll.isFillViewport = true
        scroll.clipChildren = false
        scroll.clipToPadding = false
        scroll.isVerticalScrollBarEnabled = false
        scroll.overScrollMode = View.OVER_SCROLL_NEVER
        body.orientation = LinearLayout.VERTICAL
        body.gravity = Gravity.CENTER_HORIZONTAL
        body.clipChildren = false
        body.clipToPadding = false
        body.setPadding(0, context.dp(16), 0, context.dp(36))
        scroll.addView(body, FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
        addView(scroll, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT).apply { topMargin = context.dp(24) })
        upper.orientation = LinearLayout.VERTICAL
        upper.gravity = Gravity.CENTER_HORIZONTAL
        upper.clipChildren = false
        upper.clipToPadding = false
        body.addView(upper, rowParams(0))

        title = label("One price.One plan", 28f, bold = true).apply {
            gravity = Gravity.CENTER
            val content = SpannableString(text)
            content.setSpan(ForegroundColorSpan(Color.rgb(197, 26, 60)), 10, content.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            text = content
            if (Build.VERSION.SDK_INT >= 28) isAccessibilityHeading = true
        }
        upper.addView(title, rowParams(32))
        subtitle = label("Get access to well-researched school articles, expert opinions, and inspiring student stories that keep you learning beyond the classroom.", 12f).apply {
            gravity = Gravity.CENTER
            // FadeInDown replaces source opacity .6; the settled video is opaque black.
            setTextColor(Color.BLACK)
            setLineSpacing(0f, 1f)
        }
        upper.addView(subtitle, rowParams(40).apply { topMargin = context.dp(8) })
        segment = SegmentControl(context, initialPlan) { plan ->
            selected = plan
            fabric.setColor(plan.fabricColor)
            price.setPrice(plan.price)
            onPlanSelected(plan)
        }
        val segmentWidth = minOf(context.dp(220f * resources.configuration.fontScale.coerceAtLeast(1f)).toInt(), resources.displayMetrics.widthPixels - context.dp(64))
        val segmentHeight = context.dp(38f * resources.configuration.fontScale.coerceAtLeast(1f)).toInt()
        upper.addView(segment, LinearLayout.LayoutParams(segmentWidth, segmentHeight).apply {
            topMargin = context.dp(36); bottomMargin = context.dp(36)
        })

        card.background = context.rounded(Color.rgb(230, 153, 178), 28f)
        card.clipToOutline = true
        card.addView(fabric, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        val priceInfo = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
        priceInfo.addView(price)
        priceInfo.addView(label("All round secure payment", 13f).apply { setTextColor(Color.WHITE) })
        card.addView(priceInfo, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.BOTTOM or Gravity.START).apply {
            leftMargin = context.dp(24); bottomMargin = context.dp(24)
        })
        cardEntrance.clipChildren = false
        cardEntrance.addView(card, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        upper.addView(cardEntrance, rowParams(32, 240))
        body.addView(View(context), LinearLayout.LayoutParams(0, 0, 1f))

        benefits.orientation = LinearLayout.VERTICAL
        benefits.setPadding(context.dp(40), context.dp(28), context.dp(40), context.dp(28))
        listOf("Unlimited access to articles", "Watch full length videos", "See all school content", "Access to our community", "No ads").forEachIndexed { index, benefit ->
            val row = LinearLayout(context).apply { gravity = Gravity.CENTER_VERTICAL }
            row.addView(CheckMark(context), LinearLayout.LayoutParams(context.dp(16), context.dp(16)))
            row.addView(label(benefit, 14f, medium = true), LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f).apply {
                leftMargin = context.dp(12)
            })
            benefits.addView(row, LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                if (index != 0) topMargin = context.dp(18)
            })
        }
        body.addView(benefits, rowParams(0))
        trial.setOnClickListener { /* Presentation demo: original CTA also has no purchase handler. */ }
        body.addView(trial, rowParams(32).apply { height = context.buttonHeight() })
        fabric.setColor(initialPlan.fabricColor)
        price.setPrice(initialPlan.price, animate = false)
    }

    private fun label(value: String, size: Float, bold: Boolean = false, medium: Boolean = false) = TextView(context).apply {
        text = value; textSize = size; includeFontPadding = false
        setTextColor(Color.rgb(24, 24, 27))
        typeface = Typeface.create(if (medium) "sans-serif-medium" else "sans-serif", if (bold) Typeface.BOLD else Typeface.NORMAL)
    }

    /** Avoids the variable line height of a Unicode checkmark's fallback font. */
    private class CheckMark(context: Context) : View(context) {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(24, 24, 27)
            style = Paint.Style.STROKE
            strokeWidth = context.dp(1.4f)
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
        private val path = Path()
        init { importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO }
        override fun onDraw(canvas: Canvas) {
            path.reset()
            path.moveTo(width * .16f, height * .52f)
            path.lineTo(width * .4f, height * .75f)
            path.lineTo(width * .84f, height * .24f)
            canvas.drawPath(path, paint)
        }
    }

    private fun rowParams(margin: Int, height: Int = -2) = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
        if (height >= 0) context.dp(height) else height).apply {
        leftMargin = context.dp(margin); rightMargin = context.dp(margin)
    }

    fun setScreenSize(width: Int, height: Int) {
        fabric.screenWidth = width.toFloat(); fabric.screenHeight = height.toFloat()
        body.minimumHeight = max(0, layoutParams.height - context.dp(24))
    }

    fun present() {
        dismissed = false
        if (context.motionEnabled()) {
            translationY = height.toFloat()
            animate().translationY(0f).setDuration(450).setInterpolator(PathInterpolator(.2f, .8f, .2f, 1f)).start()
            listOf(title to 20f, subtitle to 120f, segment to 140f, benefits to 160f, trial to 180f).forEach { (view, distance) ->
                view.enter(distance)?.let { entrances += it }
            }
            upper.enter(20f)?.let { entrances += it }
            cardEntrance.enter(180f)?.let { entrances += it }
            card.enter(0f, zoom = true)?.let { entrances += it }
            fabric.enter(0f, mass = 3.0)?.let { entrances += it }
        }
        fabric.setRunning(true)
        postDelayed({ if (!dismissed && isAttachedToWindow) price.setPrice(selected.price) }, 600)
        title.requestFocus()
    }

    fun pauseFabric() = fabric.setRunning(false)
    fun resumeFabric() { if (!dismissed) fabric.setRunning(true) }
    fun dismiss(after: () -> Unit) {
        if (dismissed) return
        dismissed = true
        fabric.setRunning(false)
        entrances.forEach { it.cancel() }; entrances.clear()
        animate().cancel()
        if (context.motionEnabled()) animate().translationY(height.toFloat()).setDuration(260).withEndAction(after).start()
        else after()
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                dragStartY = event.rawY; dragStartX = event.rawX; dragging = false
                velocity?.recycle(); velocity = VelocityTracker.obtain().also { it.addMovement(event) }
            }
            MotionEvent.ACTION_MOVE -> {
                velocity?.addMovement(event)
                val dy = event.rawY - dragStartY
                if (dy > ViewConfiguration.get(context).scaledTouchSlop && dy > abs(event.rawX - dragStartX) && scroll.scrollY == 0) {
                    dragging = true; animate().cancel(); return true
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> { velocity?.recycle(); velocity = null }
        }
        return super.onInterceptTouchEvent(event)
    }

    @android.annotation.SuppressLint("ClickableViewAccessibility") // This handles a drag; actionable controls are real child Views.
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!dragging) return super.onTouchEvent(event)
        velocity?.addMovement(event)
        when (event.actionMasked) {
            MotionEvent.ACTION_MOVE -> translationY = max(0f, event.rawY - dragStartY)
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                velocity?.computeCurrentVelocity(1000)
                val shouldDismiss = event.actionMasked == MotionEvent.ACTION_UP &&
                    (translationY > height * .18f || (velocity?.yVelocity ?: 0f) > context.dp(700f))
                velocity?.recycle(); velocity = null; dragging = false
                if (shouldDismiss) onDismissRequested()
                else animate().translationY(0f).setDuration(350).setInterpolator(PathInterpolator(.2f, .8f, .2f, 1f)).start()
            }
        }
        return true
    }

    override fun onDetachedFromWindow() {
        entrances.forEach { it.cancel() }; entrances.clear()
        velocity?.recycle(); velocity = null
        fabric.setRunning(false)
        super.onDetachedFromWindow()
    }

    private class SegmentControl(context: Context, initial: Plan, private val onSelect: (Plan) -> Unit) : FrameLayout(context) {
        private val highlight = View(context)
        private val yearly: LinearLayout
        private val monthly: LinearLayout
        private var selected = initial
        private var highlightAnimator: ValueAnimator? = null
        private var badge: TextView? = null
        init {
            background = context.rounded(Color.WHITE, 10f)
            elevation = context.dp(1f)
            highlight.background = context.rounded(Color.rgb(245, 245, 245), 6f)
            highlight.elevation = context.dp(.7f)
            addView(highlight, LayoutParams(context.dp(128), context.dp(28)).apply {
                leftMargin = context.dp(5); topMargin = context.dp(5)
            })
            yearly = cell("Yearly", true)
            monthly = cell("Monthly", false)
            addView(yearly, LayoutParams(context.dp(128), LayoutParams.MATCH_PARENT).apply { leftMargin = context.dp(5) })
            addView(monthly, LayoutParams(context.dp(82), LayoutParams.MATCH_PARENT).apply { leftMargin = context.dp(133) })
            yearly.setOnClickListener { select(Plan.YEARLY) }
            monthly.setOnClickListener { select(Plan.MONTHLY) }
            update(animate = false)
        }
        private fun cell(name: String, badge: Boolean) = LinearLayout(context).apply {
            gravity = Gravity.CENTER
            elevation = context.dp(2f)
            isClickable = true; isFocusable = true
            contentDescription = if (badge) "Yearly, Save 25%" else "Monthly"
            val text = TextView(context).apply {
                this.text = name; textSize = 12f; includeFontPadding = false
                typeface = Typeface.create("sans-serif", Typeface.BOLD); setTextColor(Color.BLACK)
                importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
            }
            addView(text)
            if (badge) addView(TextView(context).apply {
                this.text = "Save 25%"; textSize = 11f; includeFontPadding = false
                maxLines = 1
                background = context.rounded(Color.WHITE, 30f)
                setPadding(context.dp(3), context.dp(3), context.dp(3), context.dp(3))
                setTextColor(Color.BLACK)
                importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
            }.also { this@SegmentControl.badge = it }, LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply { leftMargin = context.dp(8) })
            accessibilityDelegate = object : View.AccessibilityDelegate() {
                override fun onInitializeAccessibilityNodeInfo(host: View, info: AccessibilityNodeInfo) {
                    super.onInitializeAccessibilityNodeInfo(host, info)
                    info.className = "android.widget.RadioButton"; info.isCheckable = true; info.isChecked = host.isSelected
                }
            }
        }
        private fun select(plan: Plan) { selected = plan; update(animate = true); onSelect(plan) }
        private fun update(animate: Boolean) {
            highlightAnimator?.cancel()
            yearly.isSelected = selected == Plan.YEARLY
            monthly.isSelected = selected == Plan.MONTHLY
            badge?.animate()?.cancel()
            val badgeOpacity = if (selected == Plan.YEARLY) 1f else .7f
            if (animate && context.motionEnabled()) badge?.animate()?.alpha(badgeOpacity)?.setDuration(1000)?.start()
            else badge?.alpha = badgeOpacity
            val available = if (width > 0) width - context.dp(10) else context.dp(210)
            val yearlyWidth = (available * (128f / 210f)).toInt()
            val monthlyWidth = available - yearlyWidth
            val targetX = if (selected == Plan.YEARLY) 0f else yearlyWidth.toFloat()
            val targetWidth = if (selected == Plan.YEARLY) yearlyWidth else monthlyWidth
            highlight.animate().cancel()
            if (animate && context.motionEnabled()) {
                val startWidth = highlight.layoutParams.width
                highlightAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
                    duration = 1000; interpolator = PathInterpolator(.4f, 0f, .2f, 1f)
                    val startX = highlight.translationX
                    addUpdateListener {
                        val p = it.animatedValue as Float
                        highlight.translationX = startX + (targetX - startX) * p
                        highlight.layoutParams = highlight.layoutParams.apply { width = (startWidth + (targetWidth - startWidth) * p).toInt() }
                    }
                    start()
                }
            } else {
                highlight.translationX = targetX
                highlight.layoutParams = highlight.layoutParams.apply { width = targetWidth }
            }
        }
        override fun onDetachedFromWindow() {
            highlightAnimator?.cancel()
            badge?.animate()?.cancel()
            super.onDetachedFromWindow()
        }
        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            val w = View.MeasureSpec.getSize(widthMeasureSpec)
            val h = View.MeasureSpec.getSize(heightMeasureSpec)
            val available = w - context.dp(10)
            val yearlyWidth = (available * (128f / 210f)).toInt()
            (yearly.layoutParams as LayoutParams).width = yearlyWidth
            (monthly.layoutParams as LayoutParams).apply {
                width = available - yearlyWidth; leftMargin = context.dp(5) + yearlyWidth
            }
            (highlight.layoutParams as LayoutParams).height = h - context.dp(10)
            if (highlightAnimator?.isRunning != true) {
                highlight.layoutParams.width = if (selected == Plan.YEARLY) yearlyWidth else available - yearlyWidth
                highlight.translationX = if (selected == Plan.YEARLY) 0f else yearlyWidth.toFloat()
            }
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
        override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
            super.onSizeChanged(w, h, oldw, oldh)
            update(animate = false)
        }
    }
}

internal fun Context.rounded(color: Int, radiusDp: Float) = GradientDrawable().apply {
    setColor(color)
    cornerRadius = dp(radiusDp)
}
