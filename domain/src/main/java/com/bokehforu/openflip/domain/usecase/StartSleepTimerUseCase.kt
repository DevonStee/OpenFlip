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

import com.bokehforu.openflip.domain.result.DomainError
import com.bokehforu.openflip.domain.result.Result
import javax.inject.Inject
import javax.inject.Singleton

data class SleepTimerStartSpec(
    val durationSeconds: Long,
    val originalDurationMinutes: Int,
    val endTimeMillis: Long
)

sealed class StartSleepTimerError(
    override val code: String,
    override val message: String? = null
) : DomainError {
    data class InvalidDuration(val minutes: Int) : StartSleepTimerError(
        code = "INVALID_DURATION",
        message = "Sleep timer duration must be at least 1 minute."
    )

    data class DurationTooLarge(
        val minutes: Int,
        val maxMinutes: Int
    ) : StartSleepTimerError(
        code = "DURATION_TOO_LARGE",
        message = "Sleep timer duration must be at most $maxMinutes minutes."
    )
}

@Singleton
class StartSleepTimerUseCase @Inject constructor() {
    fun execute(minutes: Int, nowMillis: Long = System.currentTimeMillis()): Result<SleepTimerStartSpec> {
        if (minutes < MIN_MINUTES) {
            return Result.Failure(StartSleepTimerError.InvalidDuration(minutes))
        }
        if (minutes > MAX_MINUTES) {
            return Result.Failure(StartSleepTimerError.DurationTooLarge(minutes, MAX_MINUTES))
        }

        val durationSeconds = minutes * 60L
        return Result.Success(
            SleepTimerStartSpec(
                durationSeconds = durationSeconds,
                originalDurationMinutes = minutes,
                endTimeMillis = nowMillis + (durationSeconds * 1000L)
            )
        )
    }

    companion object {
        const val MIN_MINUTES = 1
        const val MAX_MINUTES = 300
    }
}
