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

import android.os.Build
import com.bokehforu.openflip.domain.repository.SettingsRepository
import com.bokehforu.openflip.domain.gateway.HourlyChimeScheduler
import com.bokehforu.openflip.domain.result.DomainError
import com.bokehforu.openflip.domain.result.Result
import javax.inject.Inject
import javax.inject.Singleton

sealed class ToggleHourlyChimeError(
    override val code: String,
    override val message: String? = null
) : DomainError {
    data object PermissionRequired : ToggleHourlyChimeError(
        code = "PERMISSION_REQUIRED",
        message = "Exact alarm permission is required to enable hourly chime."
    )

    data class Unexpected(
        val detail: String?
    ) : ToggleHourlyChimeError(
        code = "UNEXPECTED",
        message = detail
    )
}

@Singleton
class ToggleHourlyChimeUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val hourlyChimeScheduler: HourlyChimeScheduler
) {
    fun execute(enabled: Boolean): Result<Unit> {
        if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!hourlyChimeScheduler.canScheduleExactAlarms()) {
                return Result.Failure(ToggleHourlyChimeError.PermissionRequired)
            }
        }

        return try {
            settingsRepository.setHourlyChimeEnabled(enabled)
            if (enabled) {
                hourlyChimeScheduler.scheduleNextChime()
            }
            Result.Success(Unit)
        } catch (exception: Exception) {
            Result.Failure(
                error = ToggleHourlyChimeError.Unexpected(exception.message),
                cause = exception
            )
        }
    }
}
