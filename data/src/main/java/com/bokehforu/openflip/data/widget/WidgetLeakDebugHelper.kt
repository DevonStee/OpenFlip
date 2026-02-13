package com.bokehforu.openflip.data.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import androidx.core.content.edit
import java.lang.ref.WeakReference

/**
 * Helper class for monitoring widget memory usage and detecting potential leaks.
 * Only active in debug builds via LeakCanary.
 */
object WidgetLeakDebugHelper {

    private const val PREFS_NAME = "widget_debug_prefs"
    private const val KEY_WIDGET_UPDATE_COUNT = "widget_update_count_%d"
    private const val KEY_LAST_UPDATE_TIME = "last_update_time_%d"
    private const val MAX_UPDATES_WITHOUT_CLEANUP = 10000

    private var debugPrefs: SharedPreferences? = null
    private var isDebuggable: Boolean = false


    /**
     * Logs widget updates for debugging purposes.
     * Tracks update frequency to detect potential memory issues.
     */
    fun logWidgetUpdate(context: Context, widgetId: Int, providerClass: Class<*>) {
        val appContext = context.applicationContext
        initDebugPrefs(appContext)
        if (!isDebuggable) return

        // Track update count for this widget
        val count = getUpdateCount(widgetId) + 1

        debugPrefs?.edit { putInt(KEY_WIDGET_UPDATE_COUNT.format(widgetId), count) }
        debugPrefs?.edit { putLong(KEY_LAST_UPDATE_TIME.format(widgetId), System.currentTimeMillis()) }

        // Warn if too many updates without cleanup
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

        debugPrefs?.edit { remove(KEY_WIDGET_UPDATE_COUNT.format(widgetId)) }
        debugPrefs?.edit { remove(KEY_LAST_UPDATE_TIME.format(widgetId)) }
    }

    /**
     * Gets the update count for a specific widget.
     */
    fun getUpdateCount(widgetId: Int): Int {
        return debugPrefs?.getInt(KEY_WIDGET_UPDATE_COUNT.format(widgetId), 0) ?: 0
    }

    /**
     * Gets all active widget IDs and their update counts.
     */
    fun getAllWidgetStats(): Map<Int, Int> {
        val activeStats = mutableMapOf<Int, Int>()
        debugPrefs?.all?.keys?.forEach { key ->
            if (key.startsWith(KEY_WIDGET_UPDATE_COUNT.format("").replace("%d", ""))) {
                // Extract widget ID from key
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

        debugPrefs?.edit { clear() }
    }

    private fun initDebugPrefs(context: Context) {
        isDebuggable = (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        if (debugPrefs == null) {
            debugPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
    }

    /**
     * Validates widget operations are safe to perform.
     * Can be called to ensure the context is still valid.
     */
    fun validateContext(context: Context?): Boolean {
        if (context == null) return false

        return try {
            // Try to access basic context properties
            context.packageName
            context.applicationInfo != null
            true
        } catch (e: Exception) {
            // Context no longer valid (e.g., after process death)
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
            // Log unexpected errors
            if (isDebuggable) {
                android.util.Log.w("WidgetLeakDebugHelper", "Unexpected error in widget operation: ${e.message}")
            }
            android.util.Log.e("WidgetLeakDebugHelper", "Unexpected error in widget operation: ${e.message}", e)
            onError(e)
        }
    }
}
