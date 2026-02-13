package com.bokehforu.openflip.feature.clock.ui.controller

import android.content.Intent
import com.bokehforu.openflip.domain.usecase.ToggleThemeUseCase
import com.bokehforu.openflip.domain.usecase.UpdateShowSecondsUseCase

/**
 * Handler for App Shortcuts (long-press launcher icon shortcuts).
 * Supports theme switching, seconds toggle, and settings opening.
 *
 * Extracted from FullscreenClockActivity to reduce Activity complexity.
 */
class ShortcutIntentHandler(
    private val toggleThemeUseCase: ToggleThemeUseCase,
    private val updateShowSecondsUseCase: UpdateShowSecondsUseCase,
    private val onOpenSettings: () -> Unit,
    private val isDarkThemeProvider: () -> Boolean,
    private val isShowSecondsProvider: () -> Boolean
) {

    companion object {
        // Intent extra keys
        private const val EXTRA_THEME = "theme"
        private const val EXTRA_SHOW_SECONDS = "show_seconds"
        private const val EXTRA_OPEN_SETTINGS = "open_settings"

        // Theme values
        private const val THEME_DARK = "dark"
    }

    /**
     * Handle incoming intent for app shortcuts.
     * Should be called from onCreate and onNewIntent.
     */
    fun handleIntent(intent: Intent?) {
        intent ?: return

        handleThemeShortcut(intent)
        handleSecondsShortcut(intent)
        handleSettingsShortcut(intent)
    }

    /**
     * Handle theme switching shortcut.
     */
    private fun handleThemeShortcut(intent: Intent) {
        intent.getStringExtra(EXTRA_THEME)?.let { theme ->
            val isDark = theme == THEME_DARK
            if (isDarkThemeProvider() != isDark) {
                toggleThemeUseCase.set(isDark)
            }
        }
    }

    /**
     * Handle seconds display toggle shortcut.
     */
    private fun handleSecondsShortcut(intent: Intent) {
        if (intent.hasExtra(EXTRA_SHOW_SECONDS)) {
            val show = intent.getStringExtra(EXTRA_SHOW_SECONDS) == "true"
            if (isShowSecondsProvider() != show) {
                updateShowSecondsUseCase.execute(show)
            }
        }
    }

    /**
     * Handle settings opening shortcut.
     */
    private fun handleSettingsShortcut(intent: Intent) {
        if (intent.getBooleanExtra(EXTRA_OPEN_SETTINGS, false)) {
            onOpenSettings()
        }
    }
}
