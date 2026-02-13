package com.bokehforu.openflip.domain.usecase

import com.bokehforu.openflip.domain.repository.SettingsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateOrientationModeUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    fun execute(mode: Int) {
        settingsRepository.setOrientationMode(mode)
    }
}
