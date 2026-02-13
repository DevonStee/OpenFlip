package com.bokehforu.openflip.data.repository

import com.bokehforu.openflip.test.fakes.FakeSettingsStore
import com.bokehforu.openflip.core.settings.Settings
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsRepositoryImplTest {

    private val store = FakeSettingsStore()
    private val repository = SettingsRepositoryImpl(store)

    @Test
    fun `setDarkTheme updates store and flow`() = runTest {
        repository.setDarkTheme(false)
        assertFalse(repository.isDarkTheme())
        assertFalse(store.isDarkTheme)
        assertFalse(repository.isDarkThemeFlow.first())
    }

    @Test
    fun `setTimeFormatMode updates store`() {
        repository.setTimeFormatMode(2)
        assertEquals(2, repository.getTimeFormatMode())
        assertEquals(2, store.timeFormatMode)
    }

    @Test
    fun `show seconds and flaps updates store`() {
        repository.setShowSeconds(true)
        repository.setShowFlaps(false)
        assertTrue(repository.showSeconds())
        assertFalse(repository.showFlaps())
    }

    @Test
    fun `haptic and sound updates store`() {
        repository.setHapticEnabled(false)
        repository.setSoundEnabled(false)
        assertFalse(repository.isHapticEnabled())
        assertFalse(repository.isSoundEnabled())
    }

    @Test
    fun `orientation and wake lock updates store`() {
        repository.setOrientationMode(2)
        repository.setWakeLockMode(1)
        assertEquals(2, repository.getOrientationMode())
        assertEquals(1, repository.getWakeLockMode())
    }

    @Test
    fun `setHourlyChimeEnabled updates store and flow`() = runTest {
        repository.setHourlyChimeEnabled(true)
        assertTrue(repository.isHourlyChimeEnabled())
        assertTrue(store.isHourlyChimeEnabled)
        assertTrue(repository.isHourlyChimeEnabledFlow.first())
    }

    @Test
    fun `swipe scale oled timed bulb updates store`() {
        repository.setSwipeToDimEnabled(false)
        repository.setScaleEnabled(false)
        repository.setOledProtectionEnabled(false)
        repository.setTimedBulbOffEnabled(true)

        assertFalse(repository.isSwipeToDimEnabled())
        assertFalse(repository.isScaleEnabled())
        assertFalse(repository.isOledProtectionEnabled())
        assertTrue(repository.isTimedBulbOffEnabled())
    }

    @Test
    fun `resetToDefaults delegates to store`() {
        repository.setDarkTheme(false)
        repository.setShowSeconds(true)
        repository.setTimedBulbOffEnabled(true)

        repository.resetToDefaults()

        val defaults = Settings()
        assertEquals(defaults.isDarkTheme, repository.isDarkTheme())
        assertEquals(defaults.showSeconds, repository.showSeconds())
        assertEquals(defaults.isTimedBulbOffEnabled, repository.isTimedBulbOffEnabled())
    }
}
