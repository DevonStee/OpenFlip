package com.bokehforu.openflip.widget.debug

import android.content.Context
import android.content.pm.ApplicationInfo

/**
 * Helper class for monitoring widget memory usage and detecting potential leaks.
 *
 * Modified to use in-memory state to comply with strict data layer isolation rules.
 */
object WidgetLeakDebugHelper {

    private data class WidgetStats(val updateCount: Int = 0, val lastUpdateTime: Long = 0)

    private const val MAX_UPDATES_WITHOUT_CLEANUP = 10000

    private val widgetStats = mutableMapOf<Int, WidgetStats>()
    private var isDebuggable: Boolean? = null

    /**
     * Logs widget updates for debugging purposes.
     * Tracks update frequency to detect potential memory issues.
     */
    fun logWidgetUpdate(context: Context, widgetId: Int, providerClass: Class<*>) {
        if (isDebuggable == null) initDebugPrefs(context.applicationContext)
        if (isDebuggable != true) return

        val count = getUpdateCount(widgetId) + 1
        widgetStats[widgetId] = WidgetStats(updateCount = count, lastUpdateTime = System.currentTimeMillis())

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
        return widgetStats[widgetId]?.updateCount ?: 0
    }

    /**
     * Gets all active widget IDs and their update counts.
     */
    fun getAllWidgetStats(): Map<Int, Int> {
        return widgetStats.mapValues { it.value.updateCount }
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
