package com.bokehforu.openflip.feature.settings.ui.settings

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bokehforu.openflip.feature.settings.R

@Composable
@Suppress("UNUSED_PARAMETER")
internal fun SettingsMainListScaffold(
    isSheetExpanded: Boolean,
    onExpandSheet: () -> Unit,
    onDismissSheet: () -> Unit,
    sectionTimeDisplay: @Composable () -> Unit,
    sectionAppearance: @Composable () -> Unit,
    sectionScreenWake: @Composable () -> Unit,
    sectionScreensaver: @Composable () -> Unit,
    sectionSleepTimer: @Composable () -> Unit,
    sectionFeedback: @Composable () -> Unit,
    sectionReset: @Composable () -> Unit,
    sectionQuit: @Composable () -> Unit,
    sectionInformation: @Composable () -> Unit,
) {
    val horizontalPadding = dimensionResource(R.dimen.spacingMlarge)
    val sectionPadding = dimensionResource(R.dimen.settingsSectionPaddingVert)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        userScrollEnabled = true,
        contentPadding = PaddingValues(start = horizontalPadding, end = horizontalPadding, bottom = 24.dp)
    ) {
        item(key = "section_time_header") {
            SettingsSectionHeader(
                text = stringResource(R.string.sectionTimeDisplay),
                verticalPadding = sectionPadding
            )
        }
        item(key = "section_time_group") { sectionTimeDisplay() }
        item(key = "section_appearance_header") {
            SettingsSectionHeader(
                text = stringResource(R.string.sectionAppearance),
                verticalPadding = sectionPadding
            )
        }
        item(key = "section_appearance_group") { sectionAppearance() }

        item(key = "section_screenwake_header") {
            SettingsSectionHeader(
                text = stringResource(R.string.sectionScreenWake),
                verticalPadding = sectionPadding
            )
        }
        item(key = "section_screenwake_group") { sectionScreenWake() }
        item(key = "section_screensaver_group") { sectionScreensaver() }
        item(key = "section_sleep_timer_group") { sectionSleepTimer() }
        item(key = "section_feedback_header") {
            SettingsSectionHeader(
                text = stringResource(R.string.sectionFeedback),
                verticalPadding = sectionPadding
            )
        }
        item(key = "section_feedback_group") { sectionFeedback() }
        item(key = "section_reset_group") { sectionReset() }
        item(key = "section_quit_group") { sectionQuit() }
        item(key = "section_info_header") {
            SettingsSectionHeader(
                text = stringResource(R.string.sectionInformation),
                verticalPadding = sectionPadding
            )
        }
        item(key = "section_info_group") { sectionInformation() }
    }
}
