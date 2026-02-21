package com.bokehforu.openflip.domain.usecase

import com.bokehforu.openflip.data.repository.SettingsRepositoryImpl
import com.bokehforu.openflip.test.fakes.FakeSettingsStore
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for settings toggle operations via SettingsRepository directly.
 * Proxy UseCases were removed in favor of direct repository calls.
 */
class SettingsToggleUseCasesTest {

    @Test
    fun `update show seconds use case updates repository`() {
        val repository = SettingsRepositoryImpl(FakeSettingsStore())
        val useCase = UpdateShowSecondsUseCase(repository)
        useCase.execute(true)
        assertTrue(repository.showSeconds())
    }

    @Test
    fun `repository setShowFlaps updates correctly`() {
        val repository = SettingsRepositoryImpl(FakeSettingsStore())
        repository.setShowFlaps(false)
        assertFalse(repository.showFlaps())
    }

    @Test
    fun `repository set haptic and sound updates correctly`() {
        val repository = SettingsRepositoryImpl(FakeSettingsStore())
        repository.setHapticEnabled(false)
        repository.setSoundEnabled(false)
        assertFalse(repository.isHapticEnabled())
        assertFalse(repository.isSoundEnabled())
    }
}
