package com.bokehforu.openflip.domain.usecase

import com.bokehforu.openflip.domain.repository.SettingsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateShowSecondsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    fun execute(enabled: Boolean) {
        settingsRepository.setShowSeconds(enabled)
    }

    fun toggle(): Boolean {
        val next = !settingsRepository.showSeconds()
        settingsRepository.setShowSeconds(next)
        return next
    }
}
