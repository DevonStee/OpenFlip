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
 * Solid widget - uses solid/opaque card design.
 * Uses container IDs for scaling.
 *
 * Memory leak mitigations applied:
 * - Inherits safe context usage and error handling from base class
 * - Proper text view identification for dynamic scaling
 * - Implements parent lifecycle methods for cleanup
 */
class WidgetClockSolidProvider : WidgetClockBaseProvider() {
    override val layoutId: Int = R.layout.layout_widget_openflip_solid
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
