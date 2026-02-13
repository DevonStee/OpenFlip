package com.bokehforu.openflip.core.controller.interfaces

/**
 * Time source that is safe for durations and countdowns.
 *
 * Uses the same time base as [android.os.SystemClock.elapsedRealtime] on Android,
 * but can be faked in unit tests.
 */
fun interface ElapsedTimeSource {
    fun elapsedRealtimeMs(): Long
}
