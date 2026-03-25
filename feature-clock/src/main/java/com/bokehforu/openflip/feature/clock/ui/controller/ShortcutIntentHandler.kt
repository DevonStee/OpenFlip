/*
 * Copyright (C) 2026 DevonStee
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
