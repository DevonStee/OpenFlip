package com.bokehforu.openflip.core.settings

interface OledProtectionController {
    fun setOledProtection(enabled: Boolean)
}

interface SleepTimerDialogProvider {
    fun openSleepTimerDialog()
    fun openCustomSleepTimerDialog()
}

interface ThemeTransitionProvider {
    fun requestThemeChange(isDark: Boolean, force: Boolean = false)
}
