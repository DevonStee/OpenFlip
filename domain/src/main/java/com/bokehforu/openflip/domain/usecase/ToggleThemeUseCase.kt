package com.bokehforu.openflip.domain.usecase

import com.bokehforu.openflip.domain.repository.SettingsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ToggleThemeUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    fun toggle(): Boolean {
        val next = !settingsRepository.isDarkTheme()
        settingsRepository.setDarkTheme(next)
        return next
    }

    fun set(isDark: Boolean) {
        settingsRepository.setDarkTheme(isDark)
    }
}
