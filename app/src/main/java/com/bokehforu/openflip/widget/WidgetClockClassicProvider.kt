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
