package com.bokehforu.openflip.feature.clock.controller

import android.view.View
import android.view.animation.DecelerateInterpolator
import com.bokehforu.openflip.feature.clock.databinding.ActivityMainBinding
import com.bokehforu.openflip.feature.clock.ui.controller.GearAnimationController
import com.bokehforu.openflip.feature.clock.ui.WindowConfigurator
import com.bokehforu.openflip.feature.clock.view.StateToggleGlowView
import com.bokehforu.openflip.core.util.rotate360
import com.bokehforu.openflip.core.util.setDividerVisibilityAnimated
import com.bokehforu.openflip.core.util.setLabelVisibilityAnimated
import com.bokehforu.openflip.core.util.setVisibilityAnimated
import com.bokehforu.openflip.core.util.setVisibilityInstant
import com.bokehforu.openflip.feature.clock.viewmodel.FullscreenClockViewModel

/**
 * Manages the visibility and state of UI elements surrounding the clock.
 * Handles "Zen Mode", Seconds display logic, and interaction states.
 */
class UIStateController(
    private val binding: ActivityMainBinding,
    private val viewModel: FullscreenClockViewModel,
    private val gearAnimationController: GearAnimationController,
    private val windowConfigurator: WindowConfigurator,
    private val stateToggleGlowView: StateToggleGlowView
) {
    companion object {
        private const val DIVIDER_DURATION_ENTER = 350L
        private const val DIVIDER_DURATION_EXIT = 100L
    }

    fun onThemeChanged(isDark: Boolean) {
        // Pill background removed
    }

    private fun getLabelOffsets(): Pair<Float, Float> {
        val density = binding.root.resources.displayMetrics.density
        val isLandscape = binding.root.resources.configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
        // 24dp offset
        val offsetPx = 24 * density
        return if (isLandscape) {
            Pair(offsetPx, 0f) // Slide from right
        } else {
            Pair(0f, offsetPx) // Slide from bottom
        }
    }

     fun updateSecondsVisibility() {
         val isLandscape = binding.root.resources.configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
         if (viewModel.uiState.value.showSeconds) {
            binding.apply {
                val (ox, oy) = getLabelOffsets()
                // Instrument Panel Elements (Hide in seconds mode)
                setAllDividersVisibility(visible = false, isVertical = isLandscape, duration = DIVIDER_DURATION_EXIT)
                binding.lightLabel?.setLabelVisibilityAnimated(visible = false, offsetX = ox, offsetY = oy, gone = false)
                binding.lightDot?.setLabelVisibilityAnimated(visible = false, offsetX = ox, offsetY = oy, gone = false)
                binding.tuningLabel?.setLabelVisibilityAnimated(visible = false, offsetX = ox, offsetY = oy, gone = false)
                binding.tuningDot?.setLabelVisibilityAnimated(visible = false, offsetX = ox, offsetY = oy, gone = false)
                binding.invertLabel?.setLabelVisibilityAnimated(visible = false, offsetX = ox, offsetY = oy, gone = false)
                binding.invertDot?.setLabelVisibilityAnimated(visible = false, offsetX = ox, offsetY = oy, gone = false)
                binding.settingLabel?.setLabelVisibilityAnimated(visible = false, offsetX = ox, offsetY = oy, gone = false)
                binding.settingDot?.setLabelVisibilityAnimated(visible = false, offsetX = ox, offsetY = oy, gone = false)

                settingsButtonInclude.root.apply {
                    setVisibilityAnimated(visible = true, withScale = true)
                    // REMOVED: isClickable = true (Handled by Compose button)
                }

                themeToggleButtonInclude.root.apply {
                    setVisibilityAnimated(visible = false, gone = false)
                    isClickable = false
                }
                infiniteKnobButtonInclude.root.apply {
                    setVisibilityAnimated(visible = false, gone = false)
                    isClickable = false
                }
                stateToggleButtonInclude.root.apply {
                    setVisibilityAnimated(visible = false, gone = false)
                    isClickable = false
                }
            }
            gearAnimationController.stop()
        } else {
            val isInteracting = viewModel.isInteracting

            binding.apply {
                val (ox, oy) = getLabelOffsets()
                // Instrument Panel Transition
                setAllDividersVisibility(visible = isInteracting, isVertical = isLandscape, duration = if (isInteracting) DIVIDER_DURATION_ENTER else DIVIDER_DURATION_EXIT)
                binding.lightLabel?.setLabelVisibilityAnimated(visible = isInteracting, offsetX = ox, offsetY = oy, gone = false)
                binding.lightDot?.setLabelVisibilityAnimated(visible = isInteracting, offsetX = ox, offsetY = oy, gone = false)
                binding.tuningLabel?.setLabelVisibilityAnimated(visible = isInteracting, offsetX = ox, offsetY = oy, gone = false)
                binding.tuningDot?.setLabelVisibilityAnimated(visible = isInteracting, offsetX = ox, offsetY = oy, gone = false)
                binding.invertLabel?.setLabelVisibilityAnimated(visible = isInteracting, offsetX = ox, offsetY = oy, gone = false)
                binding.invertDot?.setLabelVisibilityAnimated(visible = isInteracting, offsetX = ox, offsetY = oy, gone = false)
                binding.settingLabel?.setLabelVisibilityAnimated(visible = isInteracting, offsetX = ox, offsetY = oy, gone = false)
                binding.settingDot?.setLabelVisibilityAnimated(visible = isInteracting, offsetX = ox, offsetY = oy, gone = false)
                
                // No longer handling symmetrySpacer here as it's part of setAllDividersVisibility

                settingsButtonInclude.root.apply {
                    rotationX = 0f
                    rotationY = 0f

                    setVisibilityAnimated(visible = isInteracting, withScale = true)
                    // REMOVED: isClickable = isInteracting
                }

                themeToggleButtonInclude.root.apply {
                    setVisibilityAnimated(visible = isInteracting, gone = false)
                    isClickable = isInteracting
                }
                infiniteKnobButtonInclude.root.apply {
                    setVisibilityAnimated(visible = isInteracting, gone = false)
                    isClickable = isInteracting
                }

                val lightButtonVisible = stateToggleGlowView.isGlowEnabled || isInteracting

                stateToggleButtonInclude.root.apply {
                    setVisibilityAnimated(visible = lightButtonVisible, gone = false)
                    isClickable = lightButtonVisible
                }
            }
        }
    }

     fun initializeVisibility() {
         if (viewModel.uiState.value.showSeconds) {
            binding.apply {
                setAllDividersVisibilityInstant(visible = false)
                binding.lightLabel?.apply { visibility = View.INVISIBLE; translationX = 0f; translationY = 0f }
                binding.lightDot?.apply { visibility = View.INVISIBLE; translationX = 0f; translationY = 0f }
                binding.tuningLabel?.apply { visibility = View.INVISIBLE; translationX = 0f; translationY = 0f }
                binding.tuningDot?.apply { visibility = View.INVISIBLE; translationX = 0f; translationY = 0f }
                binding.invertLabel?.apply { visibility = View.INVISIBLE; translationX = 0f; translationY = 0f }
                binding.invertDot?.apply { visibility = View.INVISIBLE; translationX = 0f; translationY = 0f }
                binding.settingLabel?.apply { visibility = View.INVISIBLE; translationX = 0f; translationY = 0f }
                binding.settingDot?.apply { visibility = View.INVISIBLE; translationX = 0f; translationY = 0f }
                // Managed by setAllDividersVisibilityInstant

                settingsButtonInclude.root.apply {
                    visibility = View.VISIBLE
                }

                themeToggleButtonInclude.root.apply {
                    visibility = View.INVISIBLE
                    isClickable = false
                }
                infiniteKnobButtonInclude.root.apply {
                    visibility = View.INVISIBLE
                    isClickable = false
                }
                stateToggleButtonInclude.root.apply {
                    visibility = View.INVISIBLE
                    isClickable = false
                }
            }
        } else {
            val isInteracting = viewModel.isInteracting
            val uiVisibility = if (isInteracting) View.VISIBLE else View.INVISIBLE

            binding.apply {
                setAllDividersVisibilityInstant(visible = isInteracting)
                binding.lightLabel?.apply { visibility = uiVisibility; translationX = 0f; translationY = 0f }
                binding.lightDot?.apply { visibility = uiVisibility; translationX = 0f; translationY = 0f }
                binding.tuningLabel?.apply { visibility = uiVisibility; translationX = 0f; translationY = 0f }
                binding.tuningDot?.apply { visibility = uiVisibility; translationX = 0f; translationY = 0f }
                binding.invertLabel?.apply { visibility = uiVisibility; translationX = 0f; translationY = 0f }
                binding.invertDot?.apply { visibility = uiVisibility; translationX = 0f; translationY = 0f }
                binding.settingLabel?.apply { visibility = uiVisibility; translationX = 0f; translationY = 0f }
                binding.settingDot?.apply { visibility = uiVisibility; translationX = 0f; translationY = 0f }

                settingsButtonInclude.root.apply {
                    visibility = uiVisibility
                }

                themeToggleButtonInclude.root.apply {
                    visibility = uiVisibility
                    isClickable = isInteracting
                }
                infiniteKnobButtonInclude.root.apply {
                    visibility = uiVisibility
                    isClickable = isInteracting
                }

                val lightButtonVisible = stateToggleGlowView.isGlowEnabled || isInteracting
                stateToggleButtonInclude.root.apply {
                    visibility = if (lightButtonVisible) View.VISIBLE else View.INVISIBLE
                    isClickable = lightButtonVisible
                }
            }
        }
    }

     fun onInteractionStateChanged() {
         val isInteracting = viewModel.isInteracting
         updateSecondsVisibility()
         
         // Only stop gear animation when interaction ends (no-op currently)
         if (!viewModel.uiState.value.showSeconds && !isInteracting) {
            gearAnimationController.stop()
        }
    }

     fun updateVisibilityInstant() {
         if (viewModel.uiState.value.showSeconds) {
            binding.apply {
                setAllDividersVisibilityInstant(visible = false)
                binding.lightLabel?.apply { setVisibilityInstant(visible = false, gone = false); translationX = 0f; translationY = 0f }
                binding.lightDot?.apply { setVisibilityInstant(visible = false, gone = false); translationX = 0f; translationY = 0f }
                binding.tuningLabel?.apply { setVisibilityInstant(visible = false, gone = false); translationX = 0f; translationY = 0f }
                binding.tuningDot?.apply { setVisibilityInstant(visible = false, gone = false); translationX = 0f; translationY = 0f }
                binding.invertLabel?.apply { setVisibilityInstant(visible = false, gone = false); translationX = 0f; translationY = 0f }
                binding.invertDot?.apply { setVisibilityInstant(visible = false, gone = false); translationX = 0f; translationY = 0f }
                binding.settingLabel?.apply { setVisibilityInstant(visible = false, gone = false); translationX = 0f; translationY = 0f }
                binding.settingDot?.apply { setVisibilityInstant(visible = false, gone = false); translationX = 0f; translationY = 0f }

                settingsButtonInclude.root.apply {
                    setVisibilityInstant(visible = true)
                }

                themeToggleButtonInclude.root.apply {
                    setVisibilityInstant(visible = false, gone = false)
                    isClickable = false
                }
                infiniteKnobButtonInclude.root.apply {
                    setVisibilityInstant(visible = false, gone = false)
                    isClickable = false
                }
                stateToggleButtonInclude.root.apply {
                    setVisibilityInstant(visible = false, gone = false)
                    isClickable = false
                }
            }
            gearAnimationController.stop()
        } else {
            val isInteracting = viewModel.isInteracting

            binding.apply {
                setAllDividersVisibilityInstant(visible = isInteracting)
                binding.lightLabel?.apply { setVisibilityInstant(visible = isInteracting, gone = false); translationX = 0f; translationY = 0f }
                binding.lightDot?.apply { setVisibilityInstant(visible = isInteracting, gone = false); translationX = 0f; translationY = 0f }
                binding.tuningLabel?.apply { setVisibilityInstant(visible = isInteracting, gone = false); translationX = 0f; translationY = 0f }
                binding.tuningDot?.apply { setVisibilityInstant(visible = isInteracting, gone = false); translationX = 0f; translationY = 0f }
                binding.invertLabel?.apply { setVisibilityInstant(visible = isInteracting, gone = false); translationX = 0f; translationY = 0f }
                binding.invertDot?.apply { setVisibilityInstant(visible = isInteracting, gone = false); translationX = 0f; translationY = 0f }
                binding.settingLabel?.apply { setVisibilityInstant(visible = isInteracting, gone = false); translationX = 0f; translationY = 0f }
                binding.settingDot?.apply { setVisibilityInstant(visible = isInteracting, gone = false); translationX = 0f; translationY = 0f }


                settingsButtonInclude.root.apply {
                    rotationX = 0f
                    rotationY = 0f
                    setVisibilityInstant(visible = isInteracting)
                }

                themeToggleButtonInclude.root.apply {
                    setVisibilityInstant(visible = isInteracting, gone = false)
                    isClickable = isInteracting
                }
                infiniteKnobButtonInclude.root.apply {
                    setVisibilityInstant(visible = isInteracting, gone = false)
                    isClickable = isInteracting
                }

                val lightButtonVisible = stateToggleGlowView.isGlowEnabled || isInteracting
                stateToggleButtonInclude.root.apply {
                    setVisibilityInstant(visible = lightButtonVisible, gone = false)
                    isClickable = lightButtonVisible
                }
            }
        }
    }

    fun hideSystemUI() {
        windowConfigurator.hideSystemUI()
    }

    fun animateThemeButton() {
        binding.themeToggleButtonInclude.themeToggleIcon.rotate360()
    }

    fun animateButtonsOnOrientationChange() {
        // No-op: icon animations should be click-only.
    }

    private fun playSpinAnimation(view: View, clockwise: Boolean = true) {
        view.rotate360(clockwise = clockwise)
    }

    private fun setAllDividersVisibility(visible: Boolean, isVertical: Boolean, duration: Long) {
        binding.apply {
            // Note: Using simple Alpha animation (withScale = false) for all divider segments
            // to make them appear as one continuous "spine" without the fragmented grow effect.

            settingsDivider?.setVisibilityAnimated(visible, withScale = false, duration = duration, gone = false)
            settingsDivider1?.setVisibilityAnimated(visible, withScale = false, duration = duration, gone = false)
            settingsDivider2?.setVisibilityAnimated(visible, withScale = false, duration = duration, gone = false)
            settingsDivider3?.setVisibilityAnimated(visible, withScale = false, duration = duration, gone = false)
            settingsDivider3b?.setVisibilityAnimated(visible, withScale = false, duration = duration, gone = false)
            settingsDivider4?.setVisibilityAnimated(visible, withScale = false, duration = duration, gone = false)

            // Show short vertical spine lines in both orientations.
            // They visually "touch" each button edge as part of the instrument spine.
            val showSpines = visible
            stateToggleButtonInclude.spineTopState.setVisibilityAnimated(showSpines, withScale = false, duration = duration, gone = false)
            stateToggleButtonInclude.spineBottomState.setVisibilityAnimated(showSpines, withScale = false, duration = duration, gone = false)
            infiniteKnobButtonInclude.spineTopKnob.setVisibilityAnimated(showSpines, withScale = false, duration = duration, gone = false)
            infiniteKnobButtonInclude.spineBottomKnob.setVisibilityAnimated(showSpines, withScale = false, duration = duration, gone = false)
            themeToggleButtonInclude.spineTopTheme.setVisibilityAnimated(showSpines, withScale = false, duration = duration, gone = false)
            themeToggleButtonInclude.spineBottomTheme.setVisibilityAnimated(showSpines, withScale = false, duration = duration, gone = false)
            settingsButtonInclude.spineTopSettings.setVisibilityAnimated(showSpines, withScale = false, duration = duration, gone = false)
            settingsButtonInclude.spineBottomSettings.setVisibilityAnimated(showSpines, withScale = false, duration = duration, gone = false)
        }
    }

    private fun setAllDividersVisibilityInstant(visible: Boolean) {
        val isVertical = binding.root.context.resources.configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
        binding.apply {
            settingsDivider?.setVisibilityInstant(visible, gone = false)
            settingsDivider1?.setVisibilityInstant(visible, gone = false)
            settingsDivider2?.setVisibilityInstant(visible, gone = false)
            settingsDivider3?.setVisibilityInstant(visible, gone = false)
            settingsDivider3b?.setVisibilityInstant(visible, gone = false)
            settingsDivider4?.setVisibilityInstant(visible, gone = false)

            // Show short vertical spine lines in both orientations.
            // They visually "touch" each button edge as part of the instrument spine.
            val showSpines = visible
            stateToggleButtonInclude.spineTopState.setVisibilityInstant(showSpines, gone = false)
            stateToggleButtonInclude.spineBottomState.setVisibilityInstant(showSpines, gone = false)
            infiniteKnobButtonInclude.spineTopKnob.setVisibilityInstant(showSpines, gone = false)
            infiniteKnobButtonInclude.spineBottomKnob.setVisibilityInstant(showSpines, gone = false)
            themeToggleButtonInclude.spineTopTheme.setVisibilityInstant(showSpines, gone = false)
            themeToggleButtonInclude.spineBottomTheme.setVisibilityInstant(showSpines, gone = false)
            settingsButtonInclude.spineTopSettings.setVisibilityInstant(showSpines, gone = false)
            settingsButtonInclude.spineBottomSettings.setVisibilityInstant(showSpines, gone = false)
        }
    }
}
