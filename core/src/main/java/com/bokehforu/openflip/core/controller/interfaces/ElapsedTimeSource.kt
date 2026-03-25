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

package com.bokehforu.openflip.core.controller.interfaces

/**
 * Time source that is safe for durations and countdowns.
 *
 * Uses the same time base as [android.os.SystemClock.elapsedRealtime] on Android,
 * but can be faked in unit tests.
 */
fun interface ElapsedTimeSource {
    fun elapsedRealtimeMs(): Long
}
