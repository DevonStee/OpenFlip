package com.bokehforu.openflip.widget

import com.bokehforu.openflip.R

/**
 * Split widget - uses split card design with top/bottom halves.
 * Uses added container IDs for scaling.
 *
 * Memory leak mitigations applied:
 * - Uses application context for all operations
 * - Overrides getTextViewIds() for proper text scaling
 * - No static references to activities or contexts
 */
class WidgetClockSplitProvider : WidgetClockBaseProvider() {
    override val layoutId: Int = R.layout.layout_widget_openflip_split
    override val hourCardId: Int = R.id.widgetHourCard
    override val minuteCardId: Int = R.id.widgetMinuteCard

    override fun getTextViewIds(): IntArray {
        return intArrayOf(
            R.id.clockHourTop,
            R.id.clockHourBottom,
            R.id.clockMinuteTop,
            R.id.clockMinuteBottom
        )
    }
}
