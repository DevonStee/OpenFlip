package com.bokehforu.openflip.feature.chime

import android.app.AlarmManager
import android.app.AlarmManager.AlarmClockInfo
import android.app.PendingIntent
import android.content.pm.ApplicationInfo
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.bokehforu.openflip.domain.repository.SettingsRepository
import com.bokehforu.openflip.feature.clock.manager.AppLifecycleMonitor
import com.bokehforu.openflip.feature.clock.ui.FullscreenClockActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HourlyChimeManager @Inject constructor(
    private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val lifecycleMonitor: AppLifecycleMonitor
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val isDebuggable = (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    private val scope = CoroutineScope(Dispatchers.Main)
    private var settingsObserverJob: Job? = null

    companion object {
        private const val TAG = "HourlyChime"
        private const val REQUEST_CODE_CHIME = 1001
        private const val REQUEST_CODE_CHIME_RETRY = 1002
        private const val REQUEST_CODE_CHIME_SHOW = 1003
        const val EXTRA_SCHEDULED_AT_MILLIS = "scheduled_at_millis"
        const val EXTRA_IS_TEST = "is_test"
        const val EXTRA_IS_RETRY = "is_retry"
        const val EXTRA_RETRY_COUNT = "retry_count"
    }

    init {
        logDebug("HourlyChimeManager initialized")
        initializeIfEnabled()
        observeSettingsChanges()
    }

    private fun initializeIfEnabled() {
        logDebug("Initializing - enabled: ${settingsRepository.isHourlyChimeEnabled()}, canSchedule: ${canScheduleExactAlarms()}")
        if (settingsRepository.isHourlyChimeEnabled() && canScheduleExactAlarms()) {
            scheduleNextChime()
        } else {
            Log.w(TAG, "Cannot initialize - enabled: ${settingsRepository.isHourlyChimeEnabled()}, canSchedule: ${canScheduleExactAlarms()}")
        }
    }

    private fun observeSettingsChanges() {
        settingsObserverJob = scope.launch {
            settingsRepository.isHourlyChimeEnabledFlow.collectLatest { isEnabled ->
                logDebug("Settings changed - enabled: $isEnabled")
                if (isEnabled && canScheduleExactAlarms()) {
                    scheduleNextChime()
                } else {
                    cancelAllAlarms()
                }
            }
        }
    }

    fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    private fun getNextChimeInfo(): ChimeInfo {
        val calendar = Calendar.getInstance()
        val currentMinute = calendar.get(Calendar.MINUTE)

        val nextChimeMinute = when {
            currentMinute < 15 -> 15
            currentMinute < 30 -> 30
            currentMinute < 45 -> 45
            else -> {
                calendar.add(Calendar.HOUR_OF_DAY, 1)
                0
            }
        }

        calendar.set(Calendar.MINUTE, nextChimeMinute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val chimeCount = ChimeScheduleUtils.resolveChimeCountForCalendar(calendar)

        return ChimeInfo(calendar.timeInMillis, chimeCount)
    }

    /**
     * Schedules the next quarter after the current time.
     * Used when the calculated next chime is in the past (edge case handling).
     */
    fun scheduleNextQuarter() {
        val calendar = Calendar.getInstance()
        val currentMinute = calendar.get(Calendar.MINUTE)
        val currentSecond = calendar.get(Calendar.SECOND)

        // Round up to the next quarter
        val nextQuarterMinute = when {
            currentMinute < 15 -> 15
            currentMinute < 30 -> 30
            currentMinute < 45 -> 45
            else -> {
                calendar.add(Calendar.HOUR_OF_DAY, 1)
                0
            }
        }

        calendar.set(Calendar.MINUTE, nextQuarterMinute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        // If we're exactly on a quarter, move to the next one
        if (currentMinute == nextQuarterMinute && currentSecond == 0) {
            when (currentMinute) {
                0 -> calendar.set(Calendar.MINUTE, 15)
                15 -> calendar.set(Calendar.MINUTE, 30)
                30 -> calendar.set(Calendar.MINUTE, 45)
                45 -> {
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.add(Calendar.HOUR_OF_DAY, 1)
                }
            }
        }

        val chimeCount = ChimeScheduleUtils.resolveChimeCountForCalendar(calendar)

        val triggerTime = calendar.timeInMillis

        val intent = Intent(context, HourlyChimeReceiver::class.java).apply {
            action = HourlyChimeReceiver.ACTION_CHIME
            putExtra(HourlyChimeReceiver.EXTRA_CHIME_COUNT, chimeCount)
            putExtra(EXTRA_SCHEDULED_AT_MILLIS, triggerTime)
            putExtra(EXTRA_IS_RETRY, false)
            putExtra(EXTRA_RETRY_COUNT, 0)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_CHIME,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
        scheduleAlarmClock(triggerTime, pendingIntent)

        logDebug("Next quarter scheduled for: $triggerTime (count: $chimeCount)")
    }

    fun scheduleNextChime() {
        if (!settingsRepository.isHourlyChimeEnabled()) {
            Log.w(TAG, "Cannot schedule - feature disabled")
            return
        }
        if (!canScheduleExactAlarms()) {
            Log.e(TAG, "Cannot schedule - no permission")
            return
        }

        val chimeInfo = getNextChimeInfo()
        val delayMillis = chimeInfo.timeMillis - System.currentTimeMillis()

        logDebug("Scheduling chime - count: ${chimeInfo.count}, delay: ${delayMillis}ms, time: ${chimeInfo.timeMillis}")

        if (delayMillis <= 0) {
            Log.w(TAG, "Delay is negative or zero, skipping to next quarter")
            scheduleNextQuarter()
            return
        }

        val intent = Intent(context, HourlyChimeReceiver::class.java).apply {
            action = HourlyChimeReceiver.ACTION_CHIME
            putExtra(HourlyChimeReceiver.EXTRA_CHIME_COUNT, chimeInfo.count)
            putExtra(EXTRA_SCHEDULED_AT_MILLIS, chimeInfo.timeMillis)
            putExtra(EXTRA_IS_RETRY, false)
            putExtra(EXTRA_RETRY_COUNT, 0)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_CHIME,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)

        val triggerTime = chimeInfo.timeMillis

        scheduleAlarmClock(triggerTime, pendingIntent)

        logDebug("Alarm scheduled for: $triggerTime")
    }

    fun testChime(hour: Int = 6, minute: Int = 0) {
        logDebug("Test chime requested for $hour:$minute")
        if (!settingsRepository.isHourlyChimeEnabled()) {
            Log.w(TAG, "Test failed - feature disabled")
            return
        }

        val testCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val chimeCount = ChimeScheduleUtils.resolveChimeCountForCalendar(testCalendar)

        val intent = Intent(context, HourlyChimeReceiver::class.java).apply {
            action = HourlyChimeReceiver.ACTION_CHIME
            putExtra(HourlyChimeReceiver.EXTRA_CHIME_COUNT, chimeCount)
            putExtra(EXTRA_IS_TEST, true)
            putExtra(EXTRA_IS_RETRY, false)
            putExtra(EXTRA_RETRY_COUNT, 0)
        }
        context.sendBroadcast(intent)
        logDebug("Test broadcast sent with count: $chimeCount")
    }

    fun scheduleRetryChime(chimeCount: Int, scheduledAtMillis: Long, retryCount: Int) {
        if (!settingsRepository.isHourlyChimeEnabled()) return
        if (!canScheduleExactAlarms()) return

        val now = System.currentTimeMillis()
        if (scheduledAtMillis <= now + 1000L) return

        val intent = Intent(context, HourlyChimeReceiver::class.java).apply {
            action = HourlyChimeReceiver.ACTION_CHIME
            putExtra(HourlyChimeReceiver.EXTRA_CHIME_COUNT, chimeCount)
            putExtra(EXTRA_SCHEDULED_AT_MILLIS, scheduledAtMillis)
            putExtra(EXTRA_IS_TEST, false)
            putExtra(EXTRA_IS_RETRY, true)
            putExtra(EXTRA_RETRY_COUNT, retryCount)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_CHIME_RETRY,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
        scheduleAlarmClock(scheduledAtMillis, pendingIntent)

        logDebug("Retry chime scheduled - count: $chimeCount, retryCount: $retryCount, target: $scheduledAtMillis")
    }

    private fun cancelAllAlarms() {
        logDebug("Cancelling all alarms")
        val normalIntent = Intent(context, HourlyChimeReceiver::class.java)
        val normalPendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_CHIME,
            normalIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(normalPendingIntent)

        val retryIntent = Intent(context, HourlyChimeReceiver::class.java)
        val retryPendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_CHIME_RETRY,
            retryIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(retryPendingIntent)

        val showIntent = buildShowIntent()
        alarmManager.cancel(showIntent)
    }

    private fun scheduleAlarmClock(triggerTime: Long, pendingIntent: PendingIntent) {
        val showIntent = buildShowIntent()
        val alarmClockInfo = AlarmClockInfo(triggerTime, showIntent)
        alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
    }

    private fun buildShowIntent(): PendingIntent {
        val intent = Intent(context, FullscreenClockActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context,
            REQUEST_CODE_CHIME_SHOW,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun release() {
        logDebug("Releasing manager")
        cancelAllAlarms()
        settingsObserverJob?.cancel()
    }

    private data class ChimeInfo(
        val timeMillis: Long,
        val count: Int
    )

    private fun logDebug(message: String) {
        if (isDebuggable) {
            Log.d(TAG, message)
        }
    }
}
