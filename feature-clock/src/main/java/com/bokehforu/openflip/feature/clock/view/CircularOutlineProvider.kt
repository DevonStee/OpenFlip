package com.bokehforu.openflip.feature.clock.view

import android.graphics.Outline
import android.view.View
import android.view.ViewOutlineProvider

/**
 * Provides a circular outline for a view, used with clipToOutline
 * to ensure content only shows within the circular button area.
 */
class CircularOutlineProvider : ViewOutlineProvider() {
    override fun getOutline(view: View, outline: Outline) {
        // Set an oval outline that matches the square view's bounds to create a circle
        outline.setOval(0, 0, view.width, view.height)
    }
}
