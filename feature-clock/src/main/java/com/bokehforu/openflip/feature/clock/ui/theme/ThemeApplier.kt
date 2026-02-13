package com.bokehforu.openflip.feature.clock.ui.theme

import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.GradientDrawable
import androidx.core.content.ContextCompat
import com.bokehforu.openflip.feature.clock.R
import com.bokehforu.openflip.feature.clock.databinding.ActivityMainBinding
import com.bokehforu.openflip.feature.clock.view.FullscreenFlipClockView
import com.bokehforu.openflip.core.util.resolveThemeColor
import com.bokehforu.openflip.core.R as CoreR

class ThemeApplier(
    private val binding: ActivityMainBinding,
    private val flipClockView: FullscreenFlipClockView
) {

    fun applyTheme(isDark: Boolean) {
        flipClockView.setDarkTheme(isDark)

        val themeRes = if (isDark) CoreR.style.Theme_OpenFlip_Dark else CoreR.style.Theme_OpenFlip_Light
        val themedContext = android.view.ContextThemeWrapper(binding.root.context, themeRes)

        // Set rootLayout background to match theme.
        // This fills the gap when OLED protection shifts the clockView via translationX/Y.
        val bgColor = themedContext.resolveThemeColor(CoreR.attr.appBackgroundColor)
        binding.rootLayout.setBackgroundColor(bgColor)

        val iconColor = themedContext.resolveThemeColor(CoreR.attr.settingsIconColor)
        binding.themeToggleButtonInclude.themeToggleIcon.setColorFilter(iconColor, PorterDuff.Mode.SRC_IN)

        // Update inner FrameLayout backgrounds (48dp circles)
        val bgDrawable = ContextCompat.getDrawable(binding.root.context, R.drawable.shape_settings_rounded_rect)?.mutate()
        if (bgDrawable is GradientDrawable) {
            val bgSolidColor = themedContext.resolveThemeColor(CoreR.attr.settingsControlBackground)
            bgDrawable.setColor(bgSolidColor)
        }

        // Revert Theme Toggle to Circle (Decoupled from Waterfall Seconds)
        val themeToggleBgDrawable = ContextCompat.getDrawable(binding.root.context, R.drawable.shape_settings_circle)?.mutate()
        if (themeToggleBgDrawable is GradientDrawable) {
            val bgSolidColor = themedContext.resolveThemeColor(CoreR.attr.settingsControlBackground)
            themeToggleBgDrawable.setColor(bgSolidColor)
            binding.themeToggleButtonInclude.themeToggleButtonInner.background = themeToggleBgDrawable
        }

        // Apply colors to the custom view
        binding.infiniteKnobButtonInclude.infiniteKnobView.setColors(isDark)

        // Instrument Panel (Divider + Labels)
        val explanationColor = themedContext.resolveThemeColor(CoreR.attr.settingsSecondaryTextColor)

        // All dividers and spines use 80% opacity for subtle visual hierarchy
        val subtleExplanationColor = (explanationColor and 0x00FFFFFF) or (0xCC shl 24)
        binding.settingsDivider?.setBackgroundColor(subtleExplanationColor)
        binding.settingsDivider1?.setBackgroundColor(subtleExplanationColor)
        binding.settingsDivider2?.setBackgroundColor(subtleExplanationColor)
        binding.settingsDivider3?.setBackgroundColor(subtleExplanationColor)
        binding.settingsDivider3b?.setBackgroundColor(subtleExplanationColor)
        binding.settingsDivider4?.setBackgroundColor(subtleExplanationColor)

        // Button internal spines (portrait mode)
        binding.stateToggleButtonInclude.spineTopState.setBackgroundColor(subtleExplanationColor)
        binding.stateToggleButtonInclude.spineBottomState.setBackgroundColor(subtleExplanationColor)
        binding.infiniteKnobButtonInclude.spineTopKnob.setBackgroundColor(subtleExplanationColor)
        binding.infiniteKnobButtonInclude.spineBottomKnob.setBackgroundColor(subtleExplanationColor)
        binding.themeToggleButtonInclude.spineTopTheme.setBackgroundColor(subtleExplanationColor)
        binding.themeToggleButtonInclude.spineBottomTheme.setBackgroundColor(subtleExplanationColor)
        binding.settingsButtonInclude.spineTopSettings.setBackgroundColor(subtleExplanationColor)
        binding.settingsButtonInclude.spineBottomSettings.setBackgroundColor(subtleExplanationColor)

        binding.lightLabel?.setTextColor(explanationColor)
        binding.tuningLabel?.setTextColor(explanationColor)
        binding.invertLabel?.setTextColor(explanationColor)
        binding.settingLabel?.setTextColor(explanationColor)

        // Swipe hint icon follows theme color via vector tint
        binding.swipeHintIcon?.setColorFilter(explanationColor, PorterDuff.Mode.SRC_IN)

        // Label backgrounds are set via XML to ?attr/themeBackgroundColor
        // They mask the spine line to create the "cut" effect in landscape
    }

    fun updateLabelBackground(color: Int) {
        // Update rootLayout background during theme transition animation
        // This keeps it in sync with the clockView's background color transition
        binding.rootLayout.setBackgroundColor(color)
    }
}
