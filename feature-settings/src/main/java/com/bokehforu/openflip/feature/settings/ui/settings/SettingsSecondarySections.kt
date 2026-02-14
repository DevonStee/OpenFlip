package com.bokehforu.openflip.feature.settings.ui.settings

import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.bokehforu.openflip.feature.settings.R
import com.bokehforu.openflip.feature.settings.controller.HourlyChimeToggleResult
import com.bokehforu.openflip.feature.settings.ui.compose.SettingsActionItem
import com.bokehforu.openflip.feature.settings.ui.compose.SettingsDivider
import com.bokehforu.openflip.feature.settings.ui.compose.SettingsNavigationItem
import com.bokehforu.openflip.feature.settings.ui.compose.SettingsSwitchItem
import com.bokehforu.openflip.feature.settings.ui.theme.DangerRed
import com.bokehforu.openflip.core.ui.TestTags
import com.bokehforu.openflip.feature.settings.ui.theme.ToggleFeatureEnabledGreen
import com.bokehforu.openflip.feature.settings.viewmodel.SettingsViewModel
import com.bokehforu.openflip.core.settings.SettingsSleepTimerState
import com.bokehforu.openflip.domain.result.Result
import com.bokehforu.openflip.domain.usecase.ToggleHourlyChimeError
import java.util.Locale

@Composable
internal fun SettingsScreenWakeSection(
    settingsViewModel: SettingsViewModel,
    isDark: Boolean,
    onNavigateWakeLock: () -> Unit,
    onToggleOledProtection: (Boolean) -> Unit,
) {
    val wakeValue by settingsViewModel.wakeLockSelection.collectAsState()
    val wakeValueLabel = when (wakeValue) {
        0 -> stringResource(R.string.optionAlways)
        1 -> stringResource(R.string.optionWhileCharging)
        2 -> stringResource(R.string.optionSystemDefault)
        else -> ""
    }
    SettingsNavigationItem(
        iconRes = R.drawable.icon_settings_wakelock_24dp,
        title = stringResource(R.string.titleKeepScreenOn),
        valueText = wakeValueLabel,
        onClick = onNavigateWakeLock,
        testTag = TestTags.NAV_KEEP_SCREEN_ON
    )
    SettingsDivider()
    val oledProtect by settingsViewModel.isOledProtectionEnabled.collectAsState()
    SettingsSwitchItem(
        iconRes = R.drawable.icon_settings_oled_protection_24dp,
        title = stringResource(R.string.titleOledProtection),
        checked = oledProtect,
        onCheckedChange = onToggleOledProtection,
        description = stringResource(R.string.descriptionOledProtection),
        isDarkTheme = isDark,
        checkedTrackColor = ToggleFeatureEnabledGreen,
        checkedThumbColor = Color.White,
        testTag = TestTags.SWITCH_OLED_PROTECTION
    )
}

@Composable
internal fun SettingsScreensaverSection(
    onOpenScreensaverSettings: () -> Unit,
) {
    SettingsNavigationItem(
        iconRes = R.drawable.icon_settings_screensaver_24dp,
        title = stringResource(R.string.labelScreensaverMode),
        valueText = null,
        onClick = onOpenScreensaverSettings,
        description = stringResource(R.string.descriptionScreensaverMode),
        testTag = TestTags.NAV_SCREENSAVER
    )
}

@Composable
internal fun SettingsSleepTimerSection(
    timerState: SettingsSleepTimerState,
    onOpenSleepTimerPage: () -> Unit,
) {
    val isTimerActive = timerState.isActive
    val timerVal = if (isTimerActive) {
        val min = timerState.remainingSeconds / 60
        val sec = timerState.remainingSeconds % 60
        String.format(Locale.US, "%02d:%02d", min, sec)
    } else {
        stringResource(R.string.optionSleepNotSet)
    }

    SettingsNavigationItem(
        iconRes = R.drawable.icon_settings_timer_24dp,
        title = stringResource(R.string.titleSleepTimer),
        valueText = timerVal,
        onClick = onOpenSleepTimerPage,
        valueTextColor = if (isTimerActive) DangerRed else null,
        testTag = TestTags.NAV_SLEEP_TIMER
    )
}

@Composable
internal fun SettingsFeedbackSection(
    settingsViewModel: SettingsViewModel,
    isDark: Boolean,
    packageNameProvider: () -> String,
    onToggleHaptic: (Boolean) -> Unit,
    onToggleSound: (Boolean) -> Unit,
    onToggleHourlyChime: (Boolean) -> HourlyChimeToggleResult,
    onOpenAlarmPermissionSettings: (android.content.Intent) -> Unit,
    onTestChime: () -> Unit,
) {
    val context = LocalContext.current
    val haptic by settingsViewModel.isHapticEnabled.collectAsState()
    SettingsSwitchItem(
        iconRes = R.drawable.icon_feedback_haptic_24dp,
        title = stringResource(R.string.labelHapticFeedback),
        checked = haptic,
        onCheckedChange = onToggleHaptic,
        isDarkTheme = isDark,
        testTag = TestTags.SWITCH_HAPTIC
    )
    SettingsDivider()
    val sound by settingsViewModel.isSoundEnabled.collectAsState()
    SettingsSwitchItem(
        iconRes = R.drawable.icon_feedback_sound_24dp,
        title = stringResource(R.string.labelSoundFeedback),
        checked = sound,
        onCheckedChange = onToggleSound,
        isDarkTheme = isDark,
        testTag = TestTags.SWITCH_SOUND
    )
    SettingsDivider()
    val hourlyChime by settingsViewModel.isHourlyChimeEnabled.collectAsState()
    var showPermissionDialog by remember { mutableStateOf(false) }

    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.hourly_chime_permission_title),
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.hourly_chime_permission_message),
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPermissionDialog = false
                        val packageUri = "package:${packageNameProvider()}".toUri()
                        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            android.content.Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, packageUri)
                        } else {
                            android.content.Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageUri)
                        }
                        onOpenAlarmPermissionSettings(intent)
                    }
                ) {
                    Text(
                        text = stringResource(R.string.action_go_settings),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    Text(
                        text = stringResource(R.string.action_permission_cancel),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    SettingsSwitchItem(
        iconRes = R.drawable.icon_toggle_hourly_chime_24dp,
        title = stringResource(R.string.labelHourlyChime),
        checked = hourlyChime,
        onCheckedChange = { enabled ->
            val result = onToggleHourlyChime(enabled)
            if (result is Result.Failure) {
                if (result.error is ToggleHourlyChimeError.PermissionRequired) {
                    showPermissionDialog = true
                    return@SettingsSwitchItem
                }
                Toast.makeText(
                    context,
                    context.getString(R.string.errorHourlyChimeToggleFailed),
                    Toast.LENGTH_SHORT
                ).show()
            }
        },
        isDarkTheme = isDark,
        testTag = TestTags.SWITCH_HOURLY_CHIME
    )

    if (hourlyChime) {
        SettingsDivider()
        SettingsActionItem(
            iconRes = R.drawable.icon_test_chime_24dp,
            title = stringResource(R.string.title_test_chime),
            iconSize = 24.dp,
            iconHeight = 30.dp,
            iconOffsetY = 1.dp,
            minHeight = 64.dp,
            onClick = onTestChime,
            testTag = TestTags.ACTION_TEST_CHIME
        )
    }
}

@Composable
internal fun SettingsResetSection(
    onReset: () -> Unit,
) {
    SettingsActionItem(
        iconRes = R.drawable.icon_action_reset_24dp,
        title = stringResource(R.string.labelResetApp),
        backgroundColor = ToggleFeatureEnabledGreen,
        contentColor = Color.White,
        iconSize = 32.dp,
        onClick = onReset,
        testTag = TestTags.ACTION_RESET
    )
}

@Composable
internal fun SettingsQuitSection(
    onQuit: () -> Unit,
) {
    SettingsActionItem(
        iconRes = R.drawable.icon_action_quit_app_24dp,
        title = stringResource(R.string.labelQuitApp),
        backgroundColor = DangerRed,
        contentColor = Color.White,
        onClick = onQuit,
        testTag = TestTags.ACTION_QUIT
    )
}

@Composable
internal fun SettingsInformationSection(
    onNavigateVersion: () -> Unit,
    onNavigateAbout: () -> Unit,
    onOpenOriginalApp: () -> Unit,
    onContact: () -> Unit,
) {
    SettingsNavigationItem(
        iconRes = R.drawable.icon_settings_information_24dp,
        title = stringResource(R.string.titleVersion),
        valueText = stringResource(R.string.labelVersionValue),
        onClick = onNavigateVersion,
        testTag = TestTags.NAV_VERSION
    )
    SettingsDivider()
    SettingsNavigationItem(
        iconRes = R.drawable.icon_settings_about_24dp,
        title = stringResource(R.string.titleAbout),
        valueText = null,
        onClick = onNavigateAbout,
        testTag = TestTags.NAV_ABOUT
    )
    SettingsDivider()
    SettingsNavigationItem(
        iconRes = R.drawable.icon_external_website_24dp,
        title = stringResource(R.string.labelOriginalAppIos),
        valueText = null,
        onClick = onOpenOriginalApp,
        testTag = TestTags.NAV_ORIGINAL_APP
    )
    SettingsDivider()
    SettingsNavigationItem(
        iconRes = R.drawable.icon_external_bug_report_24dp,
        title = stringResource(R.string.labelContactMe),
        valueText = null,
        onClick = onContact,
        testTag = TestTags.NAV_CONTACT
    )
}
