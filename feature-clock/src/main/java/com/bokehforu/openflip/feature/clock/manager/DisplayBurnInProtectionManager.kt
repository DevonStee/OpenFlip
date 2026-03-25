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

package com.bokehforu.openflip.feature.clock.manager

import android.os.Handler
import android.os.Looper
import android.view.View
import java.util.Random
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

/**
 * Manages subtle pixel shifting to prevent OLED burn-in.
 * Periodically moves the view instantly to a random offset within a small safety range.
 * Lifecycle-aware: auto-starts/stops based on lifecycle events.
 */
class DisplayBurnInProtectionManager(
    private val targetView: View,
    private val maxShiftPx: Float, // Converted pixels
    private val intervalMs: Long = 600 * 1000L, // Every 10 minutes
    private val shiftApplier: ((Float, Float) -> Unit)? = null
) : DefaultLifecycleObserver {

    private val handler = Handler(Looper.getMainLooper())
    private val random = Random()
    private var isEnabled = false
    private var onShiftListener: (() -> Unit)? = null

    private val shiftRunnable = object : Runnable {
        override fun run() {
            if (!isEnabled) return
            performShift()
            handler.postDelayed(this, intervalMs)
        }
    }

    /**
     * Enable protection. Actual start depends on Lifecycle RESUMED state if attached,
     * or immediate if not using lifecycle.
     */
    fun start() {
        isEnabled = true
        handler.removeCallbacks(shiftRunnable)
        handler.post(shiftRunnable)
    }

    /**
     * Stop the protection cycle and reset to center.
     */
    fun stop() {
        isEnabled = false
        handler.removeCallbacks(shiftRunnable)
        // Reset to center position
        applyShift(0f, 0f)
        notifyShift()
    }

    override fun onResume(owner: LifecycleOwner) {
        if (isEnabled) {
            start()
        }
    }

    override fun onPause(owner: LifecycleOwner) {
        // Stop ticker but preserve enabled state for resume
        handler.removeCallbacks(shiftRunnable)
        // Reset to center position for clean state while paused/backgrounded
        applyShift(0f, 0f)
        notifyShift()
    }

    /**
     * Complete cleanup of all callbacks to prevent memory leaks.
     * Call this when the view/context is being destroyed permanently.
     */
    fun cleanup() {
        isEnabled = false
        handler.removeCallbacksAndMessages(null)
        applyShift(0f, 0f)
        notifyShift()
    }

    fun setOnShiftListener(listener: (() -> Unit)?) {
        onShiftListener = listener
    }

    private fun performShift() {
        // Calculate random target within [-max, max]
        val targetX = (random.nextFloat() * 2 - 1) * maxShiftPx
        val targetY = (random.nextFloat() * 2 - 1) * maxShiftPx

        // Instant jump instead of animation
        applyShift(targetX, targetY)
        notifyShift()
    }

    private fun applyShift(targetX: Float, targetY: Float) {
        if (shiftApplier != null) {
            shiftApplier.invoke(targetX, targetY)
        } else {
            targetView.translationX = targetX
            targetView.translationY = targetY
        }
    }

    private fun notifyShift() {
        onShiftListener?.invoke()
    }
}
