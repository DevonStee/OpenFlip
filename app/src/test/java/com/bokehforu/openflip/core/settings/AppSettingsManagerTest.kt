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

package com.bokehforu.openflip.core.settings

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.bokehforu.openflip.data.settings.AppSettingsManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
class AppSettingsManagerTest {
    
    private lateinit var context: Context
    private lateinit var manager: AppSettingsManager
    
    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        manager = AppSettingsManager(context)
        manager.resetToDefaults()
    }
    
    @After
    fun tearDown() {
        manager.resetToDefaults()
    }
    
    @Test
    fun `initial state matches defaults`() = runTest {
        val settings = manager.settingsFlow.first()
        
        assertEquals(AppSettingsManager.DEFAULT_TIME_FORMAT_MODE, settings.timeFormatMode)
        assertEquals(AppSettingsManager.DEFAULT_DARK_THEME, settings.isDarkTheme)
        assertEquals(AppSettingsManager.DEFAULT_HAPTIC_ENABLED, settings.isHapticEnabled)
        assertEquals(AppSettingsManager.DEFAULT_SOUND_ENABLED, settings.isSoundEnabled)
        assertEquals(AppSettingsManager.DEFAULT_SHOW_SECONDS, settings.showSeconds)
        assertEquals(AppSettingsManager.DEFAULT_SHOW_FLAPS, settings.showFlaps)
    }
    
    @Test
    fun `settingsFlow emits when isDarkTheme changes`() = runTest {
        val initialSettings = manager.settingsFlow.value
        manager.isDarkTheme = !initialSettings.isDarkTheme
        
        val updatedSettings = manager.settingsFlow.value
        assertEquals(!initialSettings.isDarkTheme, updatedSettings.isDarkTheme)
    }
    
    @Test
    fun `settingsFlow emits when showSeconds changes`() = runTest {
        val initialSettings = manager.settingsFlow.value
        assertFalse(initialSettings.showSeconds)
        
        manager.showSeconds = true
        
        val updatedSettings = manager.settingsFlow.value
        assertTrue(updatedSettings.showSeconds)
    }
    
    @Test
    fun `settingsFlow emits when showFlaps changes`() = runTest {
        val initialSettings = manager.settingsFlow.value
        assertTrue(initialSettings.showFlaps)
        
        manager.showFlaps = false
        
        val updatedSettings = manager.settingsFlow.value
        assertFalse(updatedSettings.showFlaps)
    }
    
    @Test
    fun `settingsFlow emits when isHapticEnabled changes`() = runTest {
        val initialSettings = manager.settingsFlow.value
        assertTrue(initialSettings.isHapticEnabled)
        
        manager.isHapticEnabled = false
        
        val updatedSettings = manager.settingsFlow.value
        assertFalse(updatedSettings.isHapticEnabled)
    }
    
    @Test
    fun `settingsFlow emits when isSoundEnabled changes`() = runTest {
        val initialSettings = manager.settingsFlow.value
        assertTrue(initialSettings.isSoundEnabled)
        
        manager.isSoundEnabled = false
        
        val updatedSettings = manager.settingsFlow.value
        assertFalse(updatedSettings.isSoundEnabled)
    }
    
    @Test
    fun `settingsFlow emits when orientationMode changes`() = runTest {
        val initialSettings = manager.settingsFlow.value
        assertEquals(0, initialSettings.orientationMode)
        
        manager.orientationMode = 1
        
        val updatedSettings = manager.settingsFlow.value
        assertEquals(1, updatedSettings.orientationMode)
    }
    
    @Test
    fun `settingsFlow emits when wakeLockMode changes`() = runTest {
        val initialSettings = manager.settingsFlow.value
        assertEquals(2, initialSettings.wakeLockMode)
        
        manager.wakeLockMode = 1
        
        val updatedSettings = manager.settingsFlow.value
        assertEquals(1, updatedSettings.wakeLockMode)
    }
    
    @Test
    fun `resetToDefaults updates settingsFlow`() = runTest {
        manager.isDarkTheme = false
        manager.showSeconds = true
        manager.showFlaps = false
        
        manager.resetToDefaults()
        
        val settings = manager.settingsFlow.value
        assertEquals(AppSettingsManager.DEFAULT_DARK_THEME, settings.isDarkTheme)
        assertEquals(AppSettingsManager.DEFAULT_SHOW_SECONDS, settings.showSeconds)
        assertEquals(AppSettingsManager.DEFAULT_SHOW_FLAPS, settings.showFlaps)
    }
    
    @Test
    fun `listener is called when settings change`() = runTest {
        var themeChanged = false
        var secondsChanged = false
        
        manager.listener = object : AppSettingsManager.Listener {
            override fun onFormatChanged(is24Hour: Boolean) {}
            override fun onThemeChanged(isDark: Boolean) {
                themeChanged = true
            }
            override fun onShowSecondsChanged(isShow: Boolean) {
                secondsChanged = true
            }
        }
        
        manager.isDarkTheme = false
        assertTrue(themeChanged)
        
        manager.showSeconds = true
        assertTrue(secondsChanged)
    }
    
    @Test
    fun `suppressListeners prevents listener callbacks`() = runTest {
        var themeChanged = false
        
        manager.listener = object : AppSettingsManager.Listener {
            override fun onFormatChanged(is24Hour: Boolean) {}
            override fun onThemeChanged(isDark: Boolean) {
                themeChanged = true
            }
        }
        
        manager.suppressListeners = true
        manager.isDarkTheme = false
        
        assertFalse(themeChanged)
    }
}
