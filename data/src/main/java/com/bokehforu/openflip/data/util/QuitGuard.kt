package com.bokehforu.openflip.data.util

import android.content.Context
import androidx.core.content.edit

/**
 * Guards against immediate relaunch after user explicitly quits.
 * Stores a timestamp so subsequent launches within a short window are ignored.
 */
object QuitGuard {
    private const val PREFS_NAME = "quit_guard_prefs"
    private const val KEY_LAST_QUIT_MS = "last_quit_ms"
    private const val COOLDOWN_MS = 3_000L

    fun markQuit(context: Context) {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putLong(KEY_LAST_QUIT_MS, System.currentTimeMillis()) }
    }

    fun shouldAbortLaunch(context: Context): Boolean {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastQuit = prefs.getLong(KEY_LAST_QUIT_MS, 0L)
        if (lastQuit == 0L) return false
        val elapsed = System.currentTimeMillis() - lastQuit
        return elapsed in 1..COOLDOWN_MS
    }
}
