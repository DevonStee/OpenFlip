package com.bokehforu.openflip.feature.settings.ui.settings

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bokehforu.openflip.feature.settings.R
import com.bokehforu.openflip.feature.settings.ui.compose.SettingsActionItem
import com.bokehforu.openflip.feature.settings.ui.compose.SettingsDivider
import com.bokehforu.openflip.feature.settings.ui.compose.SettingsRadioItem
import com.bokehforu.openflip.feature.settings.ui.theme.DangerRed
import com.bokehforu.openflip.feature.settings.viewmodel.SettingsViewModel
import com.bokehforu.openflip.core.settings.SettingsSleepTimerState

@Composable
internal fun SettingsTimeFormatPage(
    settingsViewModel: SettingsViewModel,
    isDark: Boolean,
    onSetMode: (Int) -> Unit,
) {
    val selectedMode by settingsViewModel.timeFormatSelection.collectAsState()
    SettingsRadioItem(
        title = stringResource(R.string.option12HAmpm),
        isSelected = selectedMode == 0,
        onClick = { onSetMode(0) },
        isDarkTheme = isDark
    )
    SettingsDivider()
    SettingsRadioItem(
        title = stringResource(R.string.option0023),
        isSelected = selectedMode == 1,
        onClick = { onSetMode(1) },
        isDarkTheme = isDark
    )
    SettingsDivider()
    SettingsRadioItem(
        title = stringResource(R.string.option023),
        isSelected = selectedMode == 2,
        onClick = { onSetMode(2) },
        isDarkTheme = isDark
    )
}

@Composable
internal fun SettingsOrientationPage(
    settingsViewModel: SettingsViewModel,
    isDark: Boolean,
    onSetMode: (Int) -> Unit,
) {
    val selectedMode by settingsViewModel.orientationSelection.collectAsState()
    SettingsRadioItem(
        title = stringResource(R.string.optionAutomatic),
        isSelected = selectedMode == 0,
        onClick = { onSetMode(0) },
        isDarkTheme = isDark
    )
    SettingsDivider()
    SettingsRadioItem(
        title = stringResource(R.string.optionPortrait),
        isSelected = selectedMode == 1,
        onClick = { onSetMode(1) },
        isDarkTheme = isDark
    )
    SettingsDivider()
    SettingsRadioItem(
        title = stringResource(R.string.optionLandscapeLeft),
        isSelected = selectedMode == 2,
        onClick = { onSetMode(2) },
        isDarkTheme = isDark
    )
    SettingsDivider()
    SettingsRadioItem(
        title = stringResource(R.string.optionLandscapeRight),
        isSelected = selectedMode == 3,
        onClick = { onSetMode(3) },
        isDarkTheme = isDark
    )
    SettingsDivider()
    Text(
        text = stringResource(R.string.descriptionAutomaticOrientation),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        lineHeight = 18.sp
    )
}

@Composable
internal fun SettingsWakeLockPage(
    settingsViewModel: SettingsViewModel,
    isDark: Boolean,
    onSetMode: (Int) -> Unit,
) {
    val selectedMode by settingsViewModel.wakeLockSelection.collectAsState()
    SettingsRadioItem(
        title = stringResource(R.string.optionAlways),
        isSelected = selectedMode == 0,
        onClick = { onSetMode(0) },
        isDarkTheme = isDark
    )
    SettingsDivider()
    SettingsRadioItem(
        title = stringResource(R.string.optionWhileCharging),
        isSelected = selectedMode == 1,
        onClick = { onSetMode(1) },
        isDarkTheme = isDark
    )
    SettingsDivider()
    SettingsRadioItem(
        title = stringResource(R.string.optionSystemDefault),
        isSelected = selectedMode == 2,
        onClick = { onSetMode(2) },
        isDarkTheme = isDark
    )
}

@Composable
internal fun SettingsSleepTimerPage(
    timerState: SettingsSleepTimerState,
    onStartSleepTimer: (Int) -> Unit,
    onStopSleepTimer: () -> Unit,
    onNavigateBack: () -> Unit,
    onOpenCustomTimerDialog: () -> Unit,
) {
    val isTimerActive = timerState.isActive
    Text(
        text = stringResource(R.string.descriptionSleepTimerHint),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )
    SettingsDivider()

    SettingsActionItem(
        iconRes = R.drawable.icon_sleep_15_min,
        title = stringResource(R.string.optionSleep15Min),
        onClick = { onStartSleepTimer(15); onNavigateBack() }
    )
    SettingsDivider()
    SettingsActionItem(
        iconRes = R.drawable.icon_sleep_30_min,
        title = stringResource(R.string.optionSleep30Min),
        onClick = { onStartSleepTimer(30); onNavigateBack() }
    )
    SettingsDivider()
    SettingsActionItem(
        iconRes = R.drawable.icon_sleep_1_hour,
        title = stringResource(R.string.optionSleep1Hour),
        onClick = { onStartSleepTimer(60); onNavigateBack() }
    )
    SettingsDivider()
    SettingsActionItem(
        iconRes = R.drawable.icon_sleep_2_hours,
        title = stringResource(R.string.optionSleep2Hours),
        onClick = { onStartSleepTimer(120); onNavigateBack() }
    )
    SettingsDivider()
    SettingsActionItem(
        iconRes = R.drawable.icon_sleep_3_hours,
        title = stringResource(R.string.optionSleep3Hours),
        onClick = { onStartSleepTimer(180); onNavigateBack() }
    )
    SettingsDivider()
    SettingsActionItem(
        iconRes = R.drawable.icon_sleep_custom,
        title = stringResource(R.string.optionSleepCustom),
        onClick = {
            onNavigateBack()
            onOpenCustomTimerDialog()
        }
    )

    if (isTimerActive) {
        SettingsDivider()
        SettingsActionItem(
            iconRes = R.drawable.icon_action_reset_24dp,
            title = stringResource(R.string.actionStopTimer),
            backgroundColor = DangerRed,
            contentColor = androidx.compose.ui.graphics.Color.White,
            onClick = {
                onStopSleepTimer()
                onNavigateBack()
            }
        )
    }
}
