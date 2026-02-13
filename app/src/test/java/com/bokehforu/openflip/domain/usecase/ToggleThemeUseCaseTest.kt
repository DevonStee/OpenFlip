package com.bokehforu.openflip.domain.usecase

import com.bokehforu.openflip.data.repository.SettingsRepositoryImpl
import com.bokehforu.openflip.test.fakes.FakeSettingsStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ToggleThemeUseCaseTest {

    @Test
    fun `toggle flips current theme`() {
        val repository = SettingsRepositoryImpl(FakeSettingsStore())
        val useCase = ToggleThemeUseCase(repository)
        val initial = repository.isDarkTheme()

        val next = useCase.toggle()

        assertEquals(!initial, next)
        assertEquals(next, repository.isDarkTheme())
    }

    @Test
    fun `set writes target theme`() {
        val repository = SettingsRepositoryImpl(FakeSettingsStore())
        val useCase = ToggleThemeUseCase(repository)

        useCase.set(true)

        assertTrue(repository.isDarkTheme())
    }
}
