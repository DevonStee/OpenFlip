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

package com.bokehforu.openflip.widget.debug

import android.content.Context
import android.content.pm.ApplicationInfo

/**
 * Helper class for monitoring widget memory usage and detecting potential leaks.
 *
 * Modified to use in-memory state to comply with strict data layer isolation rules.
 */
object WidgetLeakDebugHelper {

    private const val MAX_UPDATES_WITHOUT_CLEANUP = 10000

    private val widgetStats = mutableMapOf<Int, Int>()
    private var isDebuggable: Boolean? = null

    /**
     * Logs widget updates for debugging purposes.
     * Tracks update frequency to detect potential memory issues.
     */
    fun logWidgetUpdate(context: Context, widgetId: Int, providerClass: Class<*>) {
        if (isDebuggable == null) initDebugPrefs(context.applicationContext)
        if (isDebuggable != true) return

        val count = (widgetStats[widgetId] ?: 0) + 1
        widgetStats[widgetId] = count

        if (count > MAX_UPDATES_WITHOUT_CLEANUP) {
            android.util.Log.w(
                "WidgetLeakDebugHelper",
                "Widget $widgetId (${providerClass.simpleName}) has updated $count times without cleanup. Potential memory leak!"
            )
        }
    }

    /**
     * Logs widget deletion for debugging.
     */
    fun logWidgetDeleted(widgetId: Int) {
        if (isDebuggable != true) return
        widgetStats.remove(widgetId)
    }

    /**
     * Gets the update count for a specific widget.
     */
    fun getUpdateCount(widgetId: Int): Int {
        return widgetStats[widgetId] ?: 0
    }

    /**
     * Gets all active widget IDs and their update counts.
     */
    fun getAllWidgetStats(): Map<Int, Int> {
        return widgetStats.toMap()
    }

    /**
     * Clears all debug data. Useful for testing.
     */
    fun clearAllDebugData() {
        if (isDebuggable != true) return
        widgetStats.clear()
    }

    private fun initDebugPrefs(context: Context) {
        if (isDebuggable != null) return
        isDebuggable = (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    }

    /**
     * Validates widget operations are safe to perform.
     */
    fun validateContext(context: Context?): Boolean {
        if (context == null) return false
        return try {
            context.packageName
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Safely executes widget operations with error handling.
     */
    fun safeExecuteOperation(
        context: Context,
        operation: () -> Unit,
        onError: (Exception) -> Unit = {}
    ) {
        if (!validateContext(context)) {
            onError(IllegalStateException("Invalid context"))
            return
        }

        try {
            operation()
        } catch (e: Exception) {
            android.util.Log.e("WidgetLeakDebugHelper", "Unexpected error in widget operation: ${e.message}", e)
            onError(e)
        }
    }
}
