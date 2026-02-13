package com.bokehforu.openflip.feature.clock.controller

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.bokehforu.openflip.core.R as CoreR
import com.bokehforu.openflip.feature.clock.manager.TimeSecondsTicker
import com.bokehforu.openflip.feature.clock.viewmodel.FullscreenClockViewModel
import com.bokehforu.openflip.feature.clock.ui.controller.FlipAnimationsController
import com.bokehforu.openflip.feature.clock.view.FullscreenFlipClockView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Controller responsible for monitoring system time and updating the clock UI.
 * Handles:
 * - Time tick broadcasts (minutes)
 * - Seconds ticker (seconds)
 * - Time format formatting (12h/24h)
 */
class TimeManagementController(
    private val context: Context,
    private val viewModel: FullscreenClockViewModel,
    private val clockView: FullscreenFlipClockView,
    private val flipAnimationsController: FlipAnimationsController,
    lifecycleOwner: LifecycleOwner
) : DefaultLifecycleObserver {

    /**
     * Reference to TimeTravelController for mutual exclusion.
     * When time travel is active, automatic time updates are paused.
     */
    var timeTravelController: com.bokehforu.openflip.feature.clock.controller.TimeTravelController? = null
    
    /**
     * Callback triggered when the hour changes.
     */
    var onHourChanged: (() -> Unit)? = null
    private var lastHour: String? = null

    private val secondsTicker = TimeSecondsTicker {
        updateSeconds()
    }

    private val timeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_TIME_TICK ||
                intent?.action == Intent.ACTION_TIME_CHANGED ||
                intent?.action == Intent.ACTION_TIMEZONE_CHANGED) {
                updateTime()
            }
        }
    }
    
    private var isReceiverRegistered = false

    init {
        lifecycleOwner.lifecycle.addObserver(this)
        // Also register ticker to lifecycle
        lifecycleOwner.lifecycle.addObserver(secondsTicker)
    }

    override fun onResume(owner: LifecycleOwner) {
        if (!isReceiverRegistered) {
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_TIME_TICK)
                addAction(Intent.ACTION_TIME_CHANGED)
                addAction(Intent.ACTION_TIMEZONE_CHANGED)
            }
            context.registerReceiver(timeReceiver, filter)
            isReceiverRegistered = true
        }
        updateTime(animate = false)
        startSecondsTimer()
    }

    override fun onPause(owner: LifecycleOwner) {
        if (isReceiverRegistered) {
            try {
                context.unregisterReceiver(timeReceiver)
            } catch (_: IllegalArgumentException) {
                // Receiver might not be registered
            }
            isReceiverRegistered = false
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        // Clear references to prevent memory leaks
        timeTravelController = null
        onHourChanged = null
    }

    /**
     * Cleanup for manual removal before re-inflation (orientation change)
     */
    fun cleanup(lifecycleOwner: LifecycleOwner) {
        // Fix for Orientation Change Leak:
        // Explicitly unregister receiver and stop ticker when this controller is discarded.
        if (isReceiverRegistered) {
            try {
                context.unregisterReceiver(timeReceiver)
            } catch (_: IllegalArgumentException) {
                // Receiver might not be registered
            }
            isReceiverRegistered = false
        }
        
        secondsTicker.cleanup()
        
        lifecycleOwner.lifecycle.removeObserver(this)
        lifecycleOwner.lifecycle.removeObserver(secondsTicker)
        timeTravelController = null
        onHourChanged = null
    }

    fun updateTime(animate: Boolean = true) {
         // Skip automatic time updates when user is actively using time travel
         // This prevents animation conflicts between knob rotation and system time ticks
         if (timeTravelController?.isActive == true) {
             return
         }

         // Mode 0: 1-12 AM/PM, Mode 1: 00-23, Mode 2: 0-23
         val mode = viewModel.uiState.value.timeFormatMode
         val pattern = when(mode) {
             0 -> context.getString(CoreR.string.format12H)
             1 -> context.getString(CoreR.string.format24HZero)
             2 -> context.getString(CoreR.string.format24HNoZero)
             else -> context.getString(CoreR.string.format24HZero)
         }

         // Forces Locale.US for digits - custom Gluqlo font only supports Latin numerals
         val sdf = SimpleDateFormat("$pattern:mm", Locale.US)
         val currentTime = sdf.format(Date())
         val parts = currentTime.split(":")

         var amPm: String? = null
         if (mode == 0) {
             val amPmFormat = SimpleDateFormat(context.getString(CoreR.string.formatAmPm), Locale.US)
             amPm = amPmFormat.format(Date())
         }

         if (parts.size == 2) {
             val currentHour = parts[0]
             if (lastHour != null && lastHour != currentHour) {
                 onHourChanged?.invoke()
             }
             lastHour = currentHour

             val shouldAnimate = animate && viewModel.uiState.value.showFlaps
             clockView.setTime(parts[0], parts[1], shouldAnimate, amPm)
         }
     }

     fun updateSeconds() {
         if (!viewModel.uiState.value.showSeconds) return

         val timestamp = System.currentTimeMillis()
         val seconds = (timestamp / 1000) % 60
         val displaySeconds = if (seconds == 0L) 60L else seconds
         val formattedSeconds = String.format(Locale.US, "%02d", displaySeconds)
         flipAnimationsController.animateIfNeeded(formattedSeconds)
     }

     fun startSecondsTimer() {
         secondsTicker.setEnabled(viewModel.uiState.value.showSeconds)
     }

    fun stopSecondsTimer() {
        secondsTicker.setEnabled(false)
    }
}
