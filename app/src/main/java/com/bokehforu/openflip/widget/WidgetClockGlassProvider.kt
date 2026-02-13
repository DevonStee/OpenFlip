package com.bokehforu.openflip.widget

import com.bokehforu.openflip.R

/**
 * Glass widget - uses glass/frosted card design.
 * Uses container IDs for scaling.
 *
 * Memory leak mitigations applied:
 * - Inherits safe context usage and error handling from base class
 * - Proper text view identification for dynamic scaling
 * - Implements parent lifecycle methods for cleanup
 */
class WidgetClockGlassProvider : WidgetClockBaseProvider() {
    override val layoutId: Int = R.layout.layout_widget_openflip_glass
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
