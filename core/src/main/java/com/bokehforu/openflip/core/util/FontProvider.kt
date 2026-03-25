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

package com.bokehforu.openflip.core.util

import android.content.Context
import android.graphics.Typeface

object FontProvider {
    @Volatile
    private var clockTypeface: Typeface? = null

    @Volatile
    private var clockBoldTypeface: Typeface? = null

    @Volatile
    private var uiTypeface: Typeface? = null

    fun getClockTypeface(context: Context): Typeface {
        return clockTypeface ?: synchronized(this) {
            clockTypeface ?: loadClockTypeface(context).also {
                clockTypeface = it
            }
        }
    }

    fun getClockBoldTypeface(context: Context): Typeface {
        return clockBoldTypeface ?: synchronized(this) {
            clockBoldTypeface ?: Typeface.create(getClockTypeface(context), Typeface.BOLD).also {
                clockBoldTypeface = it
            }
        }
    }

    fun getUiTypeface(): Typeface {
        return uiTypeface ?: synchronized(this) {
            uiTypeface ?: Typeface.create("sans-serif-medium", Typeface.NORMAL).also {
                uiTypeface = it
            }
        }
    }

    private fun loadClockTypeface(context: Context): Typeface {
        val fontId = com.bokehforu.openflip.core.R.font.openflip_font
        if (fontId != 0) {
            runCatching { return context.resources.getFont(fontId) }
        }
        return Typeface.create("sans-serif", Typeface.NORMAL)
    }
}
