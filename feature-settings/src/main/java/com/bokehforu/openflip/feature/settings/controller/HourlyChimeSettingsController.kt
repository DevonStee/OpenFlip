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

package com.bokehforu.openflip.feature.settings.controller

import com.bokehforu.openflip.domain.usecase.ToggleHourlyChimeUseCase
import com.bokehforu.openflip.domain.gateway.HourlyChimeTester
import com.bokehforu.openflip.domain.result.Result
import javax.inject.Inject
import javax.inject.Singleton

typealias HourlyChimeToggleResult = Result<Unit>

@Singleton
class HourlyChimeSettingsController @Inject constructor(
    private val toggleHourlyChimeUseCase: ToggleHourlyChimeUseCase,
    private val hourlyChimeTester: HourlyChimeTester
) {

    fun handleToggle(enabled: Boolean): HourlyChimeToggleResult {
        return toggleHourlyChimeUseCase.execute(enabled)
    }

    fun testChime(hour: Int, minute: Int) {
        hourlyChimeTester.testChime(hour, minute)
    }
}
