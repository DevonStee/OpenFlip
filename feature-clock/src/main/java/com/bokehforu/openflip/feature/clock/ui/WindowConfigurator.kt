package com.bokehforu.openflip.feature.clock.ui

import android.app.Activity
import android.graphics.drawable.ColorDrawable
import android.hardware.display.DisplayManager
import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.bokehforu.openflip.feature.clock.R
import com.bokehforu.openflip.core.util.resolveThemeColor
import com.bokehforu.openflip.core.R as CoreR

class WindowConfigurator(
    private val activity: Activity,
    private val isDarkProvider: () -> Boolean
) {

    private var displayListener: DisplayManager.DisplayListener? = null

    fun configureInitialWindow(isDark: Boolean) {
        val window = activity.window
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }

        val layoutParams = window.attributes
        layoutParams.rotationAnimation = WindowManager.LayoutParams.ROTATION_ANIMATION_SEAMLESS
        window.attributes = layoutParams
        applyBackgroundColor(isDark)
        hideSystemUI()
    }

    fun hideSystemUI() {
        val window = activity.window
        WindowCompat.getInsetsController(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private var lastUsedColor: Int? = null

    fun applyBackgroundColor(isDark: Boolean = isDarkProvider(), force: Boolean = false) {
        // Applies settings background colors (lighter/darker) for better contrast with buttons
        val themeRes = if (isDark) CoreR.style.Theme_OpenFlip_Dark else CoreR.style.Theme_OpenFlip_Light
        val targetColor = activity.resolveThemeColor(CoreR.attr.appBackgroundColor, themeRes)
        
        // Optimization: Only update if color actually changed to avoid flicker during rotation
        // Skip cache check if force=true (e.g., during theme transitions)
        if (!force && lastUsedColor == targetColor) return
        lastUsedColor = targetColor

        val window: Window = activity.window
        window.setBackgroundDrawable(ColorDrawable(targetColor))
        activity.findViewById<View>(android.R.id.content)?.setBackgroundColor(targetColor)
        window.decorView.postInvalidate()
    }

    fun registerDisplayListener(displayManager: DisplayManager) {
        if (displayListener != null) return
        displayListener = object : DisplayManager.DisplayListener {
            override fun onDisplayAdded(displayId: Int) = Unit
            override fun onDisplayRemoved(displayId: Int) = Unit
            override fun onDisplayChanged(displayId: Int) {
                applyBackgroundColor()
            }
        }
        displayListener?.let {
            displayManager.registerDisplayListener(it, android.os.Handler(android.os.Looper.getMainLooper()))
        }
    }

    fun unregisterDisplayListener(displayManager: DisplayManager) {
        displayListener?.let { displayManager.unregisterDisplayListener(it) }
        displayListener = null
    }

    fun onConfigurationChanged() {
        applyBackgroundColor()
    }
}
