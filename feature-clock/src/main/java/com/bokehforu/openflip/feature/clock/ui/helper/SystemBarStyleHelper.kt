package com.bokehforu.openflip.feature.clock.ui.helper

import android.graphics.Color
import android.os.Build
import android.view.Window
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat

object SystemBarStyleHelper {
    fun applyTransparentBars(
        window: Window,
        useLightIcons: Boolean,
        behavior: Int = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            WindowCompat.setDecorFitsSystemWindows(window, false)
        }

        val controller = WindowCompat.getInsetsController(window, window.decorView) ?: return
        controller.systemBarsBehavior = behavior
        controller.isAppearanceLightStatusBars = useLightIcons
        controller.isAppearanceLightNavigationBars = useLightIcons

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            @Suppress("DEPRECATION")
            window.statusBarColor = Color.TRANSPARENT
            @Suppress("DEPRECATION")
            window.navigationBarColor = Color.TRANSPARENT
        }
    }
}
