package com.bokehforu.openflip.feature.clock.ui.controller

import com.bokehforu.openflip.feature.clock.controller.UIStateController
import com.bokehforu.openflip.domain.usecase.ToggleThemeUseCase
import com.bokehforu.openflip.feature.clock.ui.theme.ThemeApplier
import com.bokehforu.openflip.feature.clock.ui.transition.ColorTransitionController
import com.bokehforu.openflip.feature.clock.ui.WindowConfigurator

/**
 * Controller responsible for theme switching logic and animations.
 * Handles the theme toggle button click and coordinates background color transitions.
 *
 * Extracted from FullscreenClockActivity to reduce Activity complexity.
 */
class ThemeToggleController(
    private val toggleThemeUseCase: ToggleThemeUseCase,
    private val uiStateController: UIStateController,
    private val windowConfigurator: WindowConfigurator,
    private val colorTransitionController: ColorTransitionController,
    private val clockView: com.bokehforu.openflip.feature.clock.view.FullscreenFlipClockView,
    private val themeApplier: ThemeApplier,
    private val isDarkThemeProvider: () -> Boolean
) {

    /**
     * Request a theme change with color transition animation.
     *
     * @param isDark Target theme state
     * @param force If true, bypasses the "no-change" check and forces a transition animation
     */
     fun requestThemeChange(isDark: Boolean, force: Boolean = false) {
         val currentIsDark = isDarkThemeProvider()

         // Guard: If requesting the same theme and not forced, do nothing
         if (!force && currentIsDark == isDark) return

         // Execute Color Transition - background fades from old color to new color
         colorTransitionController.startTransition(
             fromIsDark = currentIsDark,
             targetIsDark = isDark,
             durationMs = 300,
             onUpdate = { color ->
                 // SYNCHRONIZE: Update the clock view and label backgrounds
                 // in real-time to match the root layout fade. This prevents color blocks.
                 clockView.backgroundColorOverride = color
                 themeApplier.updateLabelBackground(color)
             },
             onComplete = {
                 // Clear override after theme change is complete so it uses theme colors again
                 clockView.backgroundColorOverride = null
                 // Sync the window background to the final theme color now that the animation is done.
                 windowConfigurator.applyBackgroundColor(isDark, force = true)
             }
         ) {
             // Persist theme via use case. Window background is updated in onComplete
             // to avoid a flash before the animation starts.
             toggleThemeUseCase.set(isDark)
         }
     }

     /**
      * Handle theme toggle button click event.
      */
     fun handleThemeToggleClick(onFeedback: () -> Unit) {
        onFeedback()
        uiStateController.animateThemeButton()
        requestThemeChange(!isDarkThemeProvider())
    }

    /**
     * Clean up resources.
     */
    fun destroy() {
        colorTransitionController.destroy()
    }
}
