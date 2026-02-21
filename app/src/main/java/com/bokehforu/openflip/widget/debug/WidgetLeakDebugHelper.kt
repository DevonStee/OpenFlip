package com.bokehforu.openflip.widget.debug

import android.content.Context
import android.content.pm.ApplicationInfo

/**
 * Helper class for monitoring widget memory usage and detecting potential leaks.
 *
 * Modified to use in-memory state to comply with strict data layer isolation rules.
 */
object WidgetLeakDebugHelper {

    private const val KEY_WIDGET_UPDATE_COUNT = "widget_update_count_%d"
    private const val KEY_LAST_UPDATE_TIME = "last_update_time_%d"
    private const val MAX_UPDATES_WITHOUT_CLEANUP = 10000

    private val debugPrefs = mutableMapOf<String, Any>()
    private var isDebuggable: Boolean = false
    private var isInitialized = false

    /**
     * Logs widget updates for debugging purposes.
     * Tracks update frequency to detect potential memory issues.
     */
    fun logWidgetUpdate(context: Context, widgetId: Int, providerClass: Class<*>) {
        val appContext = context.applicationContext
        initDebugPrefs(appContext)
        if (!isDebuggable) return

        val count = getUpdateCount(widgetId) + 1

        debugPrefs[KEY_WIDGET_UPDATE_COUNT.format(widgetId)] = count
        debugPrefs[KEY_LAST_UPDATE_TIME.format(widgetId)] = System.currentTimeMillis()

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
        if (!isDebuggable) return

        debugPrefs.remove(KEY_WIDGET_UPDATE_COUNT.format(widgetId))
        debugPrefs.remove(KEY_LAST_UPDATE_TIME.format(widgetId))
    }

    /**
     * Gets the update count for a specific widget.
     */
    fun getUpdateCount(widgetId: Int): Int {
        return (debugPrefs[KEY_WIDGET_UPDATE_COUNT.format(widgetId)] as? Int) ?: 0
    }

    /**
     * Gets all active widget IDs and their update counts.
     */
    fun getAllWidgetStats(): Map<Int, Int> {
        val activeStats = mutableMapOf<Int, Int>()
        debugPrefs.keys.forEach { key ->
            if (key.startsWith(KEY_WIDGET_UPDATE_COUNT.format("").replace("%d", ""))) {
                val widgetId = key.substringAfterLast("_").toIntOrNull()
                widgetId?.let {
                    activeStats[it] = getUpdateCount(it)
                }
            }
        }
        return activeStats
    }

    /**
     * Clears all debug data. Useful for testing.
     */
    fun clearAllDebugData() {
        if (!isDebuggable) return
        debugPrefs.clear()
    }

    private fun initDebugPrefs(context: Context) {
        if (isInitialized) return
        isDebuggable = (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        isInitialized = true
    }

    /**
     * Validates widget operations are safe to perform.
     */
    fun validateContext(context: Context?): Boolean {
        if (context == null) return false
        return try {
            context.packageName
            context.applicationInfo != null
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
        } catch (e: SecurityException) {
            onError(e)
        } catch (e: IllegalArgumentException) {
            onError(e)
        } catch (e: IllegalStateException) {
            onError(e)
        } catch (e: Exception) {
            if (isDebuggable) {
                android.util.Log.w("WidgetLeakDebugHelper", "Unexpected error in widget operation: ${e.message}")
            }
            android.util.Log.e("WidgetLeakDebugHelper", "Unexpected error in widget operation: ${e.message}", e)
            onError(e)
        }
    }
}
