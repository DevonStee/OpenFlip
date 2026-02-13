package com.bokehforu.openflip.feature.chime

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.PowerManager
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import com.bokehforu.openflip.domain.repository.SettingsRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@AndroidEntryPoint
class HourlyChimeReceiver : BroadcastReceiver() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var hourlyChimeManager: HourlyChimeManager

    private val scope = CoroutineScope(Dispatchers.Main)

    companion object {
        private const val TAG = "HourlyChimeReceiver"
        const val ACTION_CHIME = "com.bokehforu.openflip.action.CHIME"
        const val EXTRA_CHIME_COUNT = "chime_count"
        private const val MAX_RETRY_COUNT = 3
    }

    override fun onReceive(context: Context, intent: Intent) {
        val debugEnabled = isDebuggable(context)
        logDebug(debugEnabled, "onReceive called with action: ${intent.action}")
        if (intent.action != ACTION_CHIME) {
            Log.w(TAG, "Invalid action: ${intent.action}")
            return
        }

        val chimeCount = intent.getIntExtra(EXTRA_CHIME_COUNT, 1)
        val scheduledAtMillis = intent.getLongExtra(HourlyChimeManager.EXTRA_SCHEDULED_AT_MILLIS, -1L)
        val isTest = intent.getBooleanExtra(HourlyChimeManager.EXTRA_IS_TEST, false)
        val isRetry = intent.getBooleanExtra(HourlyChimeManager.EXTRA_IS_RETRY, false)
        val retryCount = intent.getIntExtra(HourlyChimeManager.EXTRA_RETRY_COUNT, 0)
        logDebug(debugEnabled, "Chime count: $chimeCount")
        val pendingResult = goAsync()

        scope.launch {
            try {
                logDebug(debugEnabled, "Checking settings - enabled: ${settingsRepository.isHourlyChimeEnabled()}")
                if (!settingsRepository.isHourlyChimeEnabled()) {
                    Log.w(TAG, "Feature disabled, skipping")
                    pendingResult.finish()
                    return@launch
                }

                val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
                val isScreenOn = powerManager.isInteractive
                val isAppForeground = ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)

                logDebug(debugEnabled, "Screen on: $isScreenOn, App foreground: $isAppForeground")

                val isEarly = !isTest && scheduledAtMillis > 0L && System.currentTimeMillis() < scheduledAtMillis - 1_000L
                if (isEarly && retryCount < MAX_RETRY_COUNT) {
                    val nextRetryCount = retryCount + 1
                    logDebug(
                        debugEnabled,
                        "Early trigger detected. Scheduling retry #$nextRetryCount for $scheduledAtMillis"
                    )
                    hourlyChimeManager.scheduleRetryChime(chimeCount, scheduledAtMillis, nextRetryCount)
                    pendingResult.finish()
                    return@launch
                }

                val resolvedChimeCount = resolveChimeCount(
                    requestedCount = chimeCount,
                    scheduledAtMillis = scheduledAtMillis,
                    isTest = isTest,
                    debugEnabled = debugEnabled
                )

                val shouldPlay = if (isTest) {
                    true
                } else {
                    isScreenOn && isAppForeground && isValidScheduledTrigger(scheduledAtMillis)
                }

                if (shouldPlay) {
                    logDebug(debugEnabled, "Starting chime foreground service with count: $resolvedChimeCount")
                    ChimeForegroundService.start(context, resolvedChimeCount, isTest)
                    logDebug(debugEnabled, "Chime foreground service started")
                } else {
                    Log.w(
                        TAG,
                        "Conditions not met - screen: $isScreenOn, foreground: $isAppForeground, shouldPlay: $shouldPlay, isTest: $isTest, isRetry: $isRetry, retryCount: $retryCount"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error playing chime", e)
            } finally {
                // Always schedule the next quarter chime to keep chain consistent.
                hourlyChimeManager.scheduleNextChime()
                logDebug(debugEnabled, "Finishing broadcast")
                pendingResult.finish()
            }
        }
    }

    private fun resolveChimeCount(
        requestedCount: Int,
        scheduledAtMillis: Long,
        isTest: Boolean,
        debugEnabled: Boolean
    ): Int {
        val sanitizedRequested = requestedCount.coerceIn(1, 12)
        if (isTest) return sanitizedRequested

        val expected = if (scheduledAtMillis > 0L) {
            ChimeScheduleUtils.resolveChimeCountForTime(scheduledAtMillis)
        } else {
            ChimeScheduleUtils.resolveChimeCountForTime(System.currentTimeMillis())
        }

        if (expected != sanitizedRequested) {
            Log.w(
                TAG,
                "Chime count mismatch detected. requested=$sanitizedRequested, expected=$expected, scheduledAt=$scheduledAtMillis"
            )
        } else {
            logDebug(debugEnabled, "Chime count verified: $expected")
        }
        return expected
    }

    private fun isValidScheduledTrigger(scheduledAtMillis: Long): Boolean {
        val now = System.currentTimeMillis()
        if (scheduledAtMillis > 0L) {
            // Reject early deliveries; allow only a very small late grace window for precision.
            if (now < scheduledAtMillis - 1_000L) return false
            // Allow reasonable late delivery (Doze / batching) but still prevent stale alarms.
            if (now - scheduledAtMillis > 120_000L) return false
            return true
        }

        // Backward compatibility for old pending intents without scheduled timestamp.
        val minute = Calendar.getInstance().get(Calendar.MINUTE)
        return minute == 0 || minute == 15 || minute == 30 || minute == 45
    }

    private fun logDebug(debugEnabled: Boolean, message: String) {
        if (debugEnabled) {
            Log.d(TAG, message)
        }
    }

    private fun isDebuggable(context: Context): Boolean {
        return (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    }
}
