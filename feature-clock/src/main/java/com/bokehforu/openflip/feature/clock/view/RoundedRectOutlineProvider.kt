package com.bokehforu.openflip.feature.clock.view

import android.graphics.Outline
import android.view.View
import android.view.ViewOutlineProvider

/**
 * Provides a rounded rectangle outline for a view, used with clipToOutline
 * to ensure content only shows within the rounded rectangle area.
 */
class RoundedRectOutlineProvider(private val cornerRadiusPx: Float) : ViewOutlineProvider() {
    override fun getOutline(view: View, outline: Outline) {
        outline.setRoundRect(0, 0, view.width, view.height, cornerRadiusPx)
    }
}
