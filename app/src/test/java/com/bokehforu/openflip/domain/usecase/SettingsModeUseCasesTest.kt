package com.bokehforu.openflip.domain.usecase

import com.bokehforu.openflip.data.repository.SettingsRepositoryImpl
import com.bokehforu.openflip.test.fakes.FakeSettingsStore
import org.junit.Assert.assertEquals
import org.junit.Test

class SettingsModeUseCasesTest {

    @Test
    fun `update orientation use case updates repository`() {
        val repository = SettingsRepositoryImpl(FakeSettingsStore())
        UpdateOrientationModeUseCase(repository).execute(3)
        assertEquals(3, repository.getOrientationMode())
    }

    @Test
    fun `update wake lock use case updates repository`() {
        val repository = SettingsRepositoryImpl(FakeSettingsStore())
        UpdateWakeLockModeUseCase(repository).execute(1)
        assertEquals(1, repository.getWakeLockMode())
    }
}
