package com.bokehforu.openflip.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.bokehforu.openflip.R
import com.bokehforu.openflip.data.widget.WidgetLeakDebugHelper
import com.bokehforu.openflip.feature.clock.ui.FullscreenClockActivity

/**
 * Base abstract class for all OpenFlip widgets.
 *
 * Font handling:
 * - Custom fonts are applied via TextAppearance in XML layouts
 * - The openflip_font.xml font-family provides cross-device compatibility
 * - Note: RemoteViews does not support programmatic Typeface setting
 *
 * Memory leak prevention:
 * - Uses application context instead of activity context
 * - Avoids storing references to contexts or views
 * - Uses static PendingIntent flags to prevent context leaks
 */
abstract class WidgetClockBaseProvider : AppWidgetProvider() {

    abstract val layoutId: Int

    /**
     * IDs of TextClock views for font application.
     * Subclasses should override to provide their text view IDs.
     */
    open val hourCardId: Int = 0
    open val minuteCardId: Int = 0

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val appContext = context.applicationContext
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(appContext, appWidgetManager, appWidgetId)
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: android.os.Bundle
    ) {
        val appContext = context.applicationContext
        updateAppWidget(appContext, appWidgetManager, appWidgetId)
    }

    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        WidgetLeakDebugHelper.logWidgetUpdate(context, appWidgetId, this::class.java)

        if (!isValidContext(context)) {
            return
        }

        val views = RemoteViews(context.packageName, layoutId)

        // Set up click handler to launch FullscreenClockActivity
        try {
            val intent = Intent(context, FullscreenClockActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context, appWidgetId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.openflipWidgetContainer, pendingIntent)
        } catch (_: IllegalArgumentException) {
            // Widget click handler setup failed, widget still works but won't launch app
        }

        // Push update to widget
        try {
            appWidgetManager.updateAppWidget(appWidgetId, views)
        } catch (_: SecurityException) {
            // Expected on some devices, widget will retry on next update
        } catch (_: IllegalStateException) {
            // Expected during shutdown, no action needed
        }
    }

    /**
     * Validates that the context is still valid and not a stale reference.
     */
    private fun isValidContext(context: Context?): Boolean {
        if (context == null) return false
        if (context.applicationContext == null) return false

        return try {
            context.packageManager.getApplicationInfo(context.packageName, 0)
            true
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Returns a list of TextView IDs for font application.
     * Subclasses can override to provide specific text view IDs.
     */
    protected open fun getTextViewIds(): IntArray {
        return intArrayOf()
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        appWidgetIds.forEach { appWidgetId ->
            cleanupWidgetResources(context, appWidgetId)
        }
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        cleanupAllWidgetResources(context)
    }

    protected open fun cleanupWidgetResources(context: Context, appWidgetId: Int) {
        WidgetLeakDebugHelper.logWidgetDeleted(appWidgetId)
    }

    protected open fun cleanupAllWidgetResources(context: Context) {
        WidgetLeakDebugHelper.clearAllDebugData()
    }
}
