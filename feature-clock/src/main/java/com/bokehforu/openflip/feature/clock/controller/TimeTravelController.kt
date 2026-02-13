package com.bokehforu.openflip.feature.clock.controller

import android.os.Handler
import android.os.Looper
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

/**
 * Manages the "Time Travel" entertainment feature's virtual time state.
 *
 * Responsibilities:
 * - Tracks knob rotation and maps to time offset
 * - Maintains virtual time relative to system time
 * - Detects rotation direction and triggers appropriate flip animation
 * - Handles hour/day boundary crossings
 * - Auto-recovers to real time after idle period
 */
class TimeTravelController(
    private val timeFormatModeProvider: () -> Int,
    private val onTimeUpdate: (hour: String, minute: String, isDecreasing: Boolean, amPm: String?) -> Unit
) : DefaultLifecycleObserver {

    companion object {
        // Mapping: 1.5 rotations (540 degrees) = 1 minute
        private const val DEGREES_PER_MINUTE = 540f
        private const val RECOVERY_DELAY_MS = 1700L
    }

    // State
    private var _isActive = false

    /** Public accessor to check if time travel is currently active */
    val isActive: Boolean
        get() = _isActive

    private var initialRotationDegrees = 0f
    private var accumulatedMinutes = 0
    private var lastDisplayedMinute = -1
    private var lastDisplayedHour = -1

    // Recovery mechanism
    private val handler = Handler(Looper.getMainLooper())
    private val recoveryRunnable = Runnable { recoverToRealTime() }

    /**
     * Called when the knob rotates.
     * @param totalDegrees Cumulative rotation angle from InfiniteKnobView
     */
    fun onRotationChanged(totalDegrees: Float) {
        if (!_isActive) {
            // First rotation: enter time travel mode
            _isActive = true
            initialRotationDegrees = totalDegrees // Capture current knob position
            accumulatedMinutes = 0
            // Capture current displayed time
            val now = Calendar.getInstance()
            lastDisplayedMinute = now.get(Calendar.MINUTE)
            lastDisplayedHour = now.get(Calendar.HOUR_OF_DAY)
        }

        // Calculate delta relative to the START of this interaction instance
        val deltaFromStart = totalDegrees - initialRotationDegrees

        // Convert delta to minutes
        val newAccumulatedMinutes = (deltaFromStart / DEGREES_PER_MINUTE).toInt()

        if (newAccumulatedMinutes != accumulatedMinutes) {
            val isIncreasing = newAccumulatedMinutes > accumulatedMinutes
            accumulatedMinutes = newAccumulatedMinutes
            updateVirtualTime(isIncreasing)
        }

        // Reset recovery timer on any rotation
        handler.removeCallbacks(recoveryRunnable)
        handler.postDelayed(recoveryRunnable, RECOVERY_DELAY_MS)
    }

    /**
     * Calculate and display virtual time based on accumulated offset.
     */
    private fun updateVirtualTime(isIncreasing: Boolean) {
        val now = Calendar.getInstance()
        now.add(Calendar.MINUTE, accumulatedMinutes)

        val newMinute = now.get(Calendar.MINUTE)
        val newHour = now.get(Calendar.HOUR_OF_DAY)

        // Only trigger animation if time actually changed
        if (newMinute == lastDisplayedMinute && newHour == lastDisplayedHour) {
            return
        }

        lastDisplayedMinute = newMinute
        lastDisplayedHour = newHour

         val timeFormatMode = timeFormatModeProvider()
        val pattern = when (timeFormatMode) {
            0 -> "h"      // 1-12 AM/PM
            1 -> "HH"     // 00-23
            2 -> "H"      // 0-23
            else -> "HH"
        }

        val sdf = SimpleDateFormat("$pattern:mm", Locale.US)
        val virtualTime = sdf.format(now.time)
        val parts = virtualTime.split(":")

        var amPm: String? = null
        if (timeFormatMode == 0) {
            val amPmFormat = SimpleDateFormat("a", Locale.US)
            amPm = amPmFormat.format(now.time)
        }

        if (parts.size == 2) {
            onTimeUpdate(parts[0], parts[1], !isIncreasing, amPm)
        }
    }

    /**
     * Recover to real system time.
     */
    private fun recoverToRealTime() {
        if (!_isActive) return

        val now = Calendar.getInstance()
        val virtualTime = Calendar.getInstance()
        virtualTime.add(Calendar.MINUTE, accumulatedMinutes)

        // Determine if we need to go forward or backward to reach real time
        val minutesToRecover = ((now.timeInMillis - virtualTime.timeInMillis) / 60000).toInt()

        // Reset state
        _isActive = false
        initialRotationDegrees = 0f
        accumulatedMinutes = 0
        lastDisplayedMinute = -1
        lastDisplayedHour = -1

        // Execute recovery animation
        if (minutesToRecover != 0) {
            animateRecovery(minutesToRecover)
        }
    }

    /**
     * Animate back to real time from virtual time.
     */
    private fun animateRecovery(minuteDelta: Int) {
        val isDecreasing = minuteDelta < 0

        val now = Calendar.getInstance()
         val timeFormatMode = timeFormatModeProvider()
        val pattern = when (timeFormatMode) {
            0 -> "h"
            1 -> "HH"
            2 -> "H"
            else -> "HH"
        }

        val sdf = SimpleDateFormat("$pattern:mm", Locale.US)
        val realTime = sdf.format(now.time)
        val parts = realTime.split(":")

        var amPm: String? = null
        if (timeFormatMode == 0) {
            val amPmFormat = SimpleDateFormat("a", Locale.US)
            amPm = amPmFormat.format(now.time)
        }

        if (parts.size == 2) {
            onTimeUpdate(parts[0], parts[1], isDecreasing, amPm)
        }
    }

    /**
     * Cancel pending recovery and reset state.
     */
    fun reset() {
        handler.removeCallbacks(recoveryRunnable)
        _isActive = false
        initialRotationDegrees = 0f
        accumulatedMinutes = 0
        lastDisplayedMinute = -1
        lastDisplayedHour = -1
    }

    /**
     * Clean up resources automatically when lifecycle owner is destroyed.
     */
    override fun onDestroy(owner: LifecycleOwner) {
        handler.removeCallbacks(recoveryRunnable)
    }

    /**
     * Manual cleanup if needed (deprecated in favor of lifecycle observation)
     */
    fun destroy() {
        handler.removeCallbacks(recoveryRunnable)
    }
}
