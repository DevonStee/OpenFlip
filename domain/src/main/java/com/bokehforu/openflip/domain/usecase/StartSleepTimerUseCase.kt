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
