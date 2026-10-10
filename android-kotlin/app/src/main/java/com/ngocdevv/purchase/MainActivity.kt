package com.ngocdevv.purchase

import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.window.OnBackInvokedDispatcher

class MainActivity : Activity() {
    private lateinit var root: FrameLayout
    private lateinit var purchase: GlossyButton
    private var sheet: PurchaseSheet? = null
    private var backdrop: View? = null
    private var selected = Plan.YEARLY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        selected = savedInstanceState?.getString("plan")?.let { runCatching { Plan.valueOf(it) }.getOrNull() } ?: Plan.YEARLY
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        if (Build.VERSION.SDK_INT >= 30) window.setDecorFitsSystemWindows(false)
        root = FrameLayout(this).apply { setBackgroundColor(Color.WHITE) }
        purchase = GlossyButton(this, "Purchase").apply { setOnClickListener { present() } }
        root.addView(purchase, FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, buttonHeight(), Gravity.CENTER))
        setContentView(root)
        root.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ -> updateSheetSize() }
        if (Build.VERSION.SDK_INT >= 33) onBackInvokedDispatcher.registerOnBackInvokedCallback(OnBackInvokedDispatcher.PRIORITY_DEFAULT) {
            if (sheet != null) dismiss() else finish()
        }
        if (savedInstanceState?.getBoolean("sheet") == true) root.post { present() }
    }

    private fun present() {
        if (sheet != null) return
        purchase.visibility = View.INVISIBLE
        purchase.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
        backdrop = View(this).apply {
            setBackgroundColor(Color.BLACK)
            alpha = .5f
            contentDescription = "Dismiss purchase options"
            setOnClickListener { dismiss() }
        }.also { root.addView(it, FrameLayout.LayoutParams(-1, -1)) }
        val panel = PurchaseSheet(this, selected, { selected = it }, { dismiss() })
        sheet = panel
        root.addView(panel, FrameLayout.LayoutParams(minOf(root.width, dp(560)), (root.height * .895f).toInt(), Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL))
        updateSheetSize()
        panel.post { if (sheet === panel) panel.present() }
    }

    private fun updateSheetSize() {
        val panel = sheet ?: return
        val width = minOf(root.width, dp(560))
        val height = (root.height * .895f).toInt()
        if (panel.layoutParams.width != width || panel.layoutParams.height != height)
            panel.layoutParams = (panel.layoutParams as FrameLayout.LayoutParams).apply {
                this.width = width; this.height = height
            }
        panel.setScreenSize(root.width, root.height)
    }

    private fun dismiss() {
        val panel = sheet ?: return
        panel.dismiss {
            if (sheet === panel) {
                root.removeView(panel)
                backdrop?.let { root.removeView(it) }
                backdrop = null; sheet = null
                purchase.visibility = View.VISIBLE
                purchase.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_AUTO
                purchase.requestFocus()
            }
        }
    }

    @Deprecated("Deprecated in Android")
    @android.annotation.SuppressLint("GestureBackNavigation") // API33+ uses the registered OnBackInvokedCallback above.
    override fun onBackPressed() { if (sheet != null) dismiss() else super.onBackPressed() }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("plan", selected.name)
        outState.putBoolean("sheet", sheet != null)
        super.onSaveInstanceState(outState)
    }
    override fun onPause() { sheet?.pauseFabric(); super.onPause() }
    override fun onResume() { super.onResume(); sheet?.resumeFabric() }
}
