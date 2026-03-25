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
