package com.bokehforu.openflip.feature.clock.manager

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * A lifecycle-aware ticker that triggers a callback every second,
 * synchronized with the system clock.
 *
 * Uses Coroutines and repeatOnLifecycle to ensure execution ONLY happens
 * when the app is in the RESUMED state (visible to user).
 */
class TimeSecondsTicker(
    private val onTick: () -> Unit
) : DefaultLifecycleObserver {

    private var job: Job? = null
    private var isEnabled = false

    /**
     * Enable or disable the ticker.
     * Even if enabled, it will only run if the lifecycle is in RESUMED state.
     */
    fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
        // The repeatOnLifecycle block automatically handles the start/stop
        // based on the isEnabled flag check inside the loop effectively.
        // However, we can simply rely on the fact that if it's running,
        // it checks the flag, or better, we can recreate the loop if needed.
        // For simplicity with repeatOnLifecycle, we let the loop run but
        // guard the callback, OR we can launch/cancel.
        
        // Given the simplicity, we'll let existing implementation patterns guide us,
        // but repeatOnLifecycle is best designed to run *while* a condition is met.
        // Here, we just rely on the lifecycle. The 'isEnabled' toggling
        // is simpler to handle by just guarding the tick callback or
        // cancelling the job if we wanted to be rigorous, but guarding is safer/easier.
    }

    override fun onCreate(owner: LifecycleOwner) {
        job = owner.lifecycleScope.launch {
            owner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                var lastTickSecond = -1L
                
                while (true) {
                    if (isEnabled) {
                         val now = System.currentTimeMillis()
                         val currentSecond = now / 1000

                         // Only tick if we're in a new second
                         if (currentSecond != lastTickSecond) {
                             lastTickSecond = currentSecond
                             onTick()
                         }
                    }

                    // Sync to ~50ms after the next second boundary
                    val now = System.currentTimeMillis()
                    val delayMs = 1000L - (now % 1000L) + 50L
                    delay(delayMs)
                }
            }
        }
    }
    
    // cleanup() is no longer strictly necessary for memory leaks as 
    // lifecycleScope cancels itself, but good for explicit disabling.
    fun cleanup() {
        isEnabled = false
        job?.cancel()
        job = null
    }
}
