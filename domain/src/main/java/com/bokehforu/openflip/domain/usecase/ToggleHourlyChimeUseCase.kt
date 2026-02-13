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
