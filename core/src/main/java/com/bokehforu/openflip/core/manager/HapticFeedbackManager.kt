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

package com.bokehforu.openflip.core.manager

import android.os.Build
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import com.bokehforu.openflip.core.controller.interfaces.HapticsProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HapticFeedbackManager @Inject constructor(private val vibrator: Vibrator?) : HapticsProvider {
    private var isEnabled: Boolean = true

    // Priority system: UI interactions suppress animation haptics briefly
    private var lastUiHapticTime: Long = 0L
    private val uiHapticSuppressionMs = 300L

    override fun setHapticEnabled(enabled: Boolean) {
        isEnabled = enabled
    }

    override fun performClick() {
        lastUiHapticTime = SystemClock.elapsedRealtime()
        tick()
    }

    override fun performLongPress() {
        lastUiHapticTime = SystemClock.elapsedRealtime()
        heavyClick()
    }

    override fun performToggle() {
        // Animation haptic - skip if suppressed by recent UI interaction
        if (isUiHapticActive()) return
        bounce()
    }

    override fun performScale() {
        lastUiHapticTime = SystemClock.elapsedRealtime()
        tick()
    }

    override fun performSecondsTick() {
        // Animation haptic - skip if suppressed by recent UI interaction
        if (isUiHapticActive()) return
        softTick()
    }

    private fun isUiHapticActive(): Boolean {
        return SystemClock.elapsedRealtime() - lastUiHapticTime < uiHapticSuppressionMs
    }

    fun tick() {
        if (!canVibrate("tick")) return

        when {
            // Android 11 (R, API 30)+: lightweight TICK – ideal for knob detents
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
            }
            // Android 10 (Q, API 29): no TICK, use CLICK as fallback
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            }
            else -> {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(15)
            }
        }
    }

    fun heavyClick() {
        if (!canVibrate("heavyClick")) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(50)
        }
    }

    fun clockTick() {
        if (!canVibrate("clockTick")) return
        tick()
    }

    fun bounce() {
        if (!canVibrate("bounce")) return

        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            }
            else -> {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(30)
            }
        }
    }

    /**
     * Very soft haptic feedback for seconds ticker animation.
     * Uses EFFECT_TICK which is lighter than EFFECT_CLICK to avoid "double click" feeling.
     */
    private fun softTick() {
        if (!canVibrate("softTick")) return

        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                // Q doesn't have TICK, use minimal one-shot vibration
                vibrator?.vibrate(VibrationEffect.createOneShot(8, VibrationEffect.DEFAULT_AMPLITUDE))
            }
            else -> {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(8)
            }
        }
    }

    private fun canVibrate(action: String): Boolean {
        if (!isEnabled) {
            return false
        }
        if (vibrator == null) {
            return false
        }
        return true
    }
}
