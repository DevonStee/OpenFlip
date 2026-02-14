package com.bokehforu.openflip.feature.settings.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.bokehforu.openflip.feature.settings.R
import com.bokehforu.openflip.feature.settings.ui.compose.SettingsDivider
import com.bokehforu.openflip.feature.settings.ui.compose.SettingsNavigationItem
import com.bokehforu.openflip.feature.settings.ui.compose.SettingsSwitchItem
import com.bokehforu.openflip.feature.settings.ui.theme.LightSurface
import com.bokehforu.openflip.feature.settings.ui.theme.ToggleFeatureEnabledGreen
import com.bokehforu.openflip.feature.settings.viewmodel.SettingsViewModel
import com.bokehforu.openflip.core.ui.TestTags

@Composable
internal fun SettingsTimeDisplaySection(
    settingsViewModel: SettingsViewModel,
    isDark: Boolean,
    onNavigateTimeFormat: () -> Unit,
    onToggleShowSeconds: (Boolean) -> Unit,
) {
    val timeValue by settingsViewModel.timeFormatSelection.collectAsState()
    val timeValueLabel = when (timeValue) {
        0 -> stringResource(R.string.option12HAmpm)
        1 -> stringResource(R.string.option0023)
        2 -> stringResource(R.string.option023)
        else -> ""
    }
    SettingsNavigationItem(
        iconRes = when (timeValue) {
            1 -> R.drawable.icon_settings_format_24h_00_24dp
            2 -> R.drawable.icon_settings_format_24h_0_24dp
            else -> R.drawable.icon_settings_format_12h_24dp
        },
        title = stringResource(R.string.titleTimeFormat),
        valueText = timeValueLabel,
        onClick = onNavigateTimeFormat,
        testTag = TestTags.NAV_TIME_FORMAT
    )
    SettingsDivider()
    val showSeconds by settingsViewModel.showSeconds.collectAsState()
    SettingsSwitchItem(
        iconRes = R.drawable.icon_settings_seconds_24dp,
        title = stringResource(R.string.titleShowSeconds),
        checked = showSeconds,
        onCheckedChange = onToggleShowSeconds,
        description = stringResource(R.string.descriptionShowSeconds),
        isDarkTheme = isDark,
        testTag = TestTags.SWITCH_SHOW_SECONDS
    )
}

@Composable
internal fun SettingsAppearanceSection(
    settingsViewModel: SettingsViewModel,
    isDark: Boolean,
    onToggleShowFlaps: (Boolean) -> Unit,
    onToggleSwipeToDim: (Boolean) -> Unit,
    onToggleLightMode: (Boolean) -> Unit,
    onNavigateOrientation: () -> Unit,
    onToggleScale: (Boolean) -> Unit,
    onToggleTimedBulbOff: (Boolean) -> Unit,
) {
    val showFlaps by settingsViewModel.showFlaps.collectAsState()
    SettingsSwitchItem(
        iconRes = R.drawable.icon_settings_flaps_24dp,
        title = stringResource(R.string.titleShowFlaps),
        checked = showFlaps,
        onCheckedChange = onToggleShowFlaps,
        isDarkTheme = isDark,
        testTag = TestTags.SWITCH_SHOW_FLAPS
    )
    SettingsDivider()
    val swipeToDim by settingsViewModel.isSwipeToDimEnabled.collectAsState()
    SettingsSwitchItem(
        iconRes = R.drawable.icon_settings_brightness_24dp,
        title = stringResource(R.string.titleSwipeToDim),
        checked = swipeToDim,
        onCheckedChange = onToggleSwipeToDim,
        isDarkTheme = isDark,
        testTag = TestTags.SWITCH_SWIPE_TO_DIM
    )
    SettingsDivider()
    val lightMode = !isDark
    SettingsSwitchItem(
        iconRes = R.drawable.icon_settings_theme_light_24dp,
        title = stringResource(R.string.titleLightMode),
        checked = lightMode,
        onCheckedChange = onToggleLightMode,
        isDarkTheme = isDark,
        checkedTrackColor = LightSurface,
        checkedThumbColor = Color.White,
        testTag = TestTags.SWITCH_LIGHT_MODE
    )
    SettingsDivider()
    val orientValue by settingsViewModel.orientationSelection.collectAsState()
    val orientValueLabel = when (orientValue) {
        0 -> stringResource(R.string.optionAutomatic)
        1 -> stringResource(R.string.optionPortrait)
        2 -> stringResource(R.string.optionLandscapeLeft)
        3 -> stringResource(R.string.optionLandscapeRight)
        else -> ""
    }
    SettingsNavigationItem(
        iconRes = R.drawable.icon_settings_orientation_24dp,
        title = stringResource(R.string.titleScreenOrientation),
        valueText = orientValueLabel,
        onClick = onNavigateOrientation,
        testTag = TestTags.NAV_ORIENTATION
    )
    SettingsDivider()
    val pinchScale by settingsViewModel.isScaleEnabled.collectAsState()
    SettingsSwitchItem(
        iconRes = R.drawable.icon_settings_scale_24dp,
        title = stringResource(R.string.titlePinchToReduce),
        checked = pinchScale,
        onCheckedChange = onToggleScale,
        isDarkTheme = isDark,
        testTag = TestTags.SWITCH_PINCH_SCALE
    )
    SettingsDivider()
    val bulbOff by settingsViewModel.isTimedBulbOffEnabled.collectAsState()
    SettingsSwitchItem(
        iconRes = R.drawable.icon_settings_timed_bulb_24dp,
        title = stringResource(R.string.settings_15s_timed_bulb_off),
        checked = bulbOff,
        onCheckedChange = onToggleTimedBulbOff,
        isDarkTheme = isDark,
        checkedTrackColor = ToggleFeatureEnabledGreen,
        checkedThumbColor = Color.White,
        testTag = TestTags.SWITCH_TIMED_BULB_OFF
    )
}
