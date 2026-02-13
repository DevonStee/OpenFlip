package com.bokehforu.openflip.domain.usecase

import com.bokehforu.openflip.domain.repository.SettingsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateHapticEnabledUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    fun execute(enabled: Boolean) {
        settingsRepository.setHapticEnabled(enabled)
    }
}
