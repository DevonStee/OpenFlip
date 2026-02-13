package com.bokehforu.openflip.widget

import com.bokehforu.openflip.R

/**
 * Classic widget - uses simple single-piece card design.
 * Uses direct TextClock IDs for scaling.
 *
 * Memory leak mitigations applied:
 * - Text view IDs specified for proper scaling
 * - No static references maintained
 * - Inherits safe context usage from base class
 */
class WidgetClockClassicProvider : WidgetClockBaseProvider() {
    override val layoutId: Int = R.layout.layout_widget_openflip_classic
    override val hourCardId: Int = R.id.clockHour
    override val minuteCardId: Int = R.id.clockMinute

    override fun getTextViewIds(): IntArray {
        return intArrayOf(
            R.id.clockHour,
            R.id.clockMinute
        )
    }
}
