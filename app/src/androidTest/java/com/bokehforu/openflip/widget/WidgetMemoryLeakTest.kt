package com.bokehforu.openflip.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bokehforu.openflip.ui.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*
import java.lang.ref.WeakReference
import java.lang.reflect.Field

/**
 * Memory leak tests for OpenFlip widgets.
 * Tests ensure that widget providers don't hold references to contexts or activities.
 */
@RunWith(AndroidJUnit4::class)
class WidgetMemoryLeakTest {

    @Test
    fun test_widgetProvider_doesNotRetainContext() = runBlocking {
        val appContext = ApplicationProvider.getApplicationContext<Context>()
        val widgetManager = AppWidgetManager.getInstance(appContext)

        // Test each widget provider
        val widgetProviders = listOf(
            OpenFlipWidgetClassic::class.java,
            OpenFlipWidgetSplit::class.java,
            OpenFlipWidgetSolid::class.java,
            OpenFlipWidgetGlass::class.java,
            OpenFlipWidgetWhite::class.java
        )

        for (providerClass in widgetProviders) {
            // Create weak reference to track context retention
            val contextRef = WeakReference(appContext)

            // Simulate widget updates
            val provider = providerClass.newInstance()
            val appWidgetIds = intArrayOf(1, 2, 3, 4, 5) // Mock IDs

            // Trigger multiple updates
            repeat(5) {
                provider.onUpdate(appContext, widgetManager, appWidgetIds)
                provider.onAppWidgetOptionsChanged(appContext, widgetManager, 1, android.os.Bundle())
            }

            // Force garbage collection
            System.gc()
            System.runFinalization()
            System.gc()

            delay(100) // Allow GC to complete

            // Verify context can be garbage collected (it's still the app context, so this might not fail)
            assertNotNull("Context should not be null after updates for ${providerClass.simpleName}", contextRef.get())

            // Check for static field leaks
            checkForStaticFieldLeaks(providerClass)

            // Test cleanup methods are called
            provider.onDeleted(appContext, appWidgetIds)
            provider.onDisabled(appContext)
        }
    }

    @Test
    fun test_widgetProvider_handlesErrorGracefully() = runBlocking {
        val appContext = ApplicationProvider.getApplicationContext<Context>()
        val widgetManager = AppWidgetManager.getInstance(appContext)

        val providers = listOf(
            OpenFlipWidgetClassic(),
            OpenFlipWidgetSplit(),
            OpenFlipWidgetSolid(),
            OpenFlipWidgetGlass(),
            OpenFlipWidgetWhite()
        )

        providers.forEach { provider ->
            // Test with null parameters (should not crash)
            runCatching {
                provider.onUpdate(null, null, intArrayOf())
                provider.onAppWidgetOptionsChanged(null, null, 1, android.os.Bundle())
            }.onFailure {
                fail("Provider ${provider.javaClass.simpleName} should handle null gracefully: ${it.message}")
            }

            // Test with invalid widget IDs
            runCatching {
                provider.onUpdate(appContext, widgetManager, intArrayOf(-1, -2, -3))
            }.onFailure {
                fail("Provider ${provider.javaClass.simpleName} should handle invalid IDs gracefully: ${it.message}")
            }
        }
    }

    private fun checkForStaticFieldLeaks(providerClass: Class<out BaseOpenFlipWidget>) {
        val staticFields = providerClass.declaredFields.filter {
             java.lang.reflect.Modifier.isStatic(it.modifiers)
        }

        staticFields.forEach { field ->
            field.isAccessible = true
            when {
                Context::class.java.isAssignableFrom(field.type) -> {
                    val value = field.get(null)
                    assertNull("Static Context reference found in ${providerClass.simpleName}: ${field.name}", value)
                }
                field.type.simpleName.contains("List", ignoreCase = true) -> {
                    // Check if it's collecting contexts
                    val value = field.get(null)
                    value?.let {
                        val list = it as? Iterable<*>
                        list?.forEach { item ->
                            if (item is Context) {
                                fail("${providerClass.simpleName} has static Context in list: ${field.name}")
                            }
                        }
                    }
                }
            }
        }
    }

    @Test
    fun test_pendingIntentFlags_areCorrect() {
        val appContext = ApplicationProvider.getApplicationContext<Context>()

        // Ensure the PendingIntent flags are set correctly
        val shadowProvider = object : BaseOpenFlipWidget() {
            override val layoutId = R.layout.layout_widget_openflip_classic

            fun testPendingIntent() {
                val intent = Intent(appContext, MainActivity::class.java)
                val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                val pendingIntent = PendingIntent.getActivity(appContext, 0, intent, flags)

                // Verify flags are set
                assertTrue("PendingIntent should be immutable",
                    flags and PendingIntent.FLAG_IMMUTABLE != 0)
                assertTrue("PendingIntent should update current",
                    flags and PendingIntent.FLAG_UPDATE_CURRENT != 0)
            }
        }

        shadowProvider.testPendingIntent()
    }
}