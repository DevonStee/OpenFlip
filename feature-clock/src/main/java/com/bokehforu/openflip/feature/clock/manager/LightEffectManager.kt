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

import android.view.View
import android.view.ViewTreeObserver
import com.bokehforu.openflip.feature.clock.view.FullscreenFlipClockView

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

/**
 * Manages the dynamic light source effect that originates from a specific UI element (e.g., the toggle button)
 * and casts light onto the FlipClock.
 */
class LightEffectManager(
    private val clockView: FullscreenFlipClockView,
    private val sourceView: View,
    private val rootView: View
) : DefaultLifecycleObserver {

    private val globalLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
        updateLightSourcePosition()
    }
    
    
    private var isInitialized = false
    
    override fun onCreate(owner: LifecycleOwner) {
        init()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        destroy()
    }

    fun init() {
        if (isInitialized) return
        rootView.viewTreeObserver.addOnGlobalLayoutListener(globalLayoutListener)
        // Initial update after layout
        sourceView.post { updateLightSourcePosition() }
        isInitialized = true
    }
    
    fun destroy() {
        if (!isInitialized) return
        if (rootView.viewTreeObserver.isAlive) {
            rootView.viewTreeObserver.removeOnGlobalLayoutListener(globalLayoutListener)
        }
        isInitialized = false
    }

    fun updateLightSourcePosition() {
        // Wait for both views to have valid dimensions
        if (sourceView.width == 0 || sourceView.height == 0 ||
            clockView.width == 0 || clockView.height == 0) {
            clockView.setLightSourceVisible(false)
            return
        }

        // Don't update position if source is not visible (e.g., during rotation)
        // Instead, hide the light effect until button reappears
        if (!sourceView.isShown) {
            clockView.setLightSourceVisible(false)
            return
        }
        clockView.setLightSourceVisible(true)

        // Get source center in screen coordinates
        val sourceLocation = IntArray(2)
        sourceView.getLocationOnScreen(sourceLocation)
        val sourceCenterX = sourceLocation[0] + sourceView.width / 2f
        val sourceCenterY = sourceLocation[1] + sourceView.height / 2f
        
        // Get clock view position in screen coordinates
        val clockLocation = IntArray(2)
        clockView.getLocationOnScreen(clockLocation)
        
        // Convert to clock view local coordinates and account for OLED shift
        val lightX = sourceCenterX - clockLocation[0] - clockView.translationX
        val lightY = sourceCenterY - clockLocation[1] - clockView.translationY
        
        clockView.setLightSourcePosition(lightX, lightY)
    }
}
