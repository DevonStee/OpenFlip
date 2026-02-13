package com.bokehforu.openflip.feature.clock.ui.controller

import android.view.View
import android.widget.ImageView
import androidx.core.view.isVisible
import com.bokehforu.openflip.feature.clock.view.StateToggleGlowView
import com.bokehforu.openflip.feature.clock.view.FullscreenFlipClockView
import com.bokehforu.openflip.feature.clock.R
import com.bokehforu.openflip.core.util.resolveThemeColor
import com.bokehforu.openflip.feature.clock.viewmodel.BulbState
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import com.bokehforu.openflip.core.R as CoreR

/**
 * Controller responsible for managing the light effect toggle button (StateToggleGlowView).
 * Handles click events, countdown timer, and light state changes.
 *
 * Extracted from FullscreenClockActivity to reduce Activity complexity.
 */
class LightToggleController @AssistedInject constructor(
    @Assisted private val stateToggleButton: StateToggleGlowView,
    @Assisted private val stateToggleIcon: ImageView,
    @Assisted private val clockView: FullscreenFlipClockView,
    @Assisted("isDarkThemeProvider") private val isDarkThemeProvider: () -> Boolean,
    @Assisted("onLightStateChanged") private val onLightStateChanged: () -> Unit,
    @Assisted("onToggleRequested") private val onToggleRequested: () -> Unit
) {

    private var countdownSeconds: Int = 0

    fun bind() {
        updateIconTint()

        stateToggleButton.setOnClickListener {
            onToggleRequested()
        }

        // Initial visibility will be applied via applyState(...)
    }


    /**
     * Force turn off the light effect.
     * Called by: timer finish, seconds mode activation, or external triggers.
     */
    fun forceTurnOffLight() {
        applyState(BulbState.OFF, 0)
    }

    /**
     * Set theme for the glow effect.
     */
    fun setTheme(isDark: Boolean) {
        stateToggleButton.setTheme(isDark)
        updateIconTint(isDark)
    }

    /**
     * Set custom typeface for countdown display.
     */
    fun setTypeface(typeface: android.graphics.Typeface) {
        stateToggleButton.setTypeface(typeface)
    }

    fun applyState(state: BulbState, countdownSeconds: Int) {
        val isOn = state is BulbState.ON
        stateToggleButton.isSelected = isOn
        stateToggleIcon.isSelected = isOn
        stateToggleButton.setGlowEnabled(isOn)
        clockView.setLightIntensity(if (isOn) 1f else 0f)

        this.countdownSeconds = countdownSeconds
        stateToggleButton.setCountdown(if (isOn) countdownSeconds else 0)

        // Icon animation/visibility
        if (isOn) {
            hideIconInstant()
        } else {
            showIconInstant()
        }

        onLightStateChanged()
        stateToggleButton.post { onLightStateChanged() }
    }

    private fun playIconPopIn() {
        stateToggleIcon.apply {
            visibility = View.VISIBLE
            alpha = 0f
            scaleX = 1f
            scaleY = 1f
            animate()
                .alpha(1f)
                .scaleX(1.12f)
                .scaleY(1.12f)
                .setDuration(110L)
                .withEndAction {
                    // ease back to 1.0
                    animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(110L)
                        .start()
                }
                .start()
        }
    }

    private fun playIconPopOut() {
        stateToggleIcon.apply {
            if (!isVisible) return
            animate()
                .alpha(0f)
                .scaleX(1.08f)
                .scaleY(1.08f)
                .setDuration(90L)
                .withEndAction {
                    visibility = View.GONE
                    alpha = 1f
                    scaleX = 1f
                    scaleY = 1f
                }
                .start()
        }
    }

    private fun showIconInstant() {
        stateToggleIcon.apply {
            visibility = View.VISIBLE
            alpha = 1f
            scaleX = 1f
            scaleY = 1f
        }
    }

    private fun hideIconInstant() {
        stateToggleIcon.apply {
            visibility = View.GONE
            alpha = 1f
            scaleX = 1f
            scaleY = 1f
        }
    }

    fun destroy() {
        // No-op: countdown is driven by ViewModel, so we don't keep a timer here anymore.
    }

    private fun updateIconTint(isDark: Boolean = isDarkThemeProvider()) {
        val themeRes = if (isDark) CoreR.style.Theme_OpenFlip_Dark else CoreR.style.Theme_OpenFlip_Light
        val tintColor = stateToggleIcon.context.resolveThemeColor(CoreR.attr.lightBulbRaysColor, themeRes)
        stateToggleIcon.setColorFilter(tintColor, android.graphics.PorterDuff.Mode.SRC_IN)
    }

    @AssistedFactory
    interface Factory {
        fun create(
            stateToggleButton: StateToggleGlowView,
            stateToggleIcon: ImageView,
            clockView: FullscreenFlipClockView,
            @Assisted("isDarkThemeProvider") isDarkThemeProvider: () -> Boolean,
            @Assisted("onLightStateChanged") onLightStateChanged: () -> Unit,
            @Assisted("onToggleRequested") onToggleRequested: () -> Unit
        ): LightToggleController
    }

}
