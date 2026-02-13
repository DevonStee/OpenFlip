package com.bokehforu.openflip.domain.usecase

import com.bokehforu.openflip.data.repository.SettingsRepositoryImpl
import com.bokehforu.openflip.test.fakes.FakeSettingsStore
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsToggleUseCasesTest {

    @Test
    fun `update show seconds use case updates repository`() {
        val repository = SettingsRepositoryImpl(FakeSettingsStore())
        val useCase = UpdateShowSecondsUseCase(repository)
        useCase.execute(true)
        assertTrue(repository.showSeconds())
    }

    @Test
    fun `update show flaps use case updates repository`() {
        val repository = SettingsRepositoryImpl(FakeSettingsStore())
        val useCase = UpdateShowFlapsUseCase(repository)
        useCase.execute(false)
        assertFalse(repository.showFlaps())
    }

    @Test
    fun `update haptic and sound use cases update repository`() {
        val repository = SettingsRepositoryImpl(FakeSettingsStore())
        UpdateHapticEnabledUseCase(repository).execute(false)
        UpdateSoundEnabledUseCase(repository).execute(false)
        assertFalse(repository.isHapticEnabled())
        assertFalse(repository.isSoundEnabled())
    }
}
