package com.bokehforu.openflip.feature.settings.ui.settings

import android.content.Intent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.bokehforu.openflip.feature.settings.R
import com.bokehforu.openflip.feature.settings.controller.HourlyChimeToggleResult
import com.bokehforu.openflip.feature.settings.ui.compose.SettingsCardGroup
import com.bokehforu.openflip.feature.settings.ui.compose.AboutPage
import com.bokehforu.openflip.feature.settings.ui.compose.VersionPage
import com.bokehforu.openflip.feature.settings.viewmodel.SettingsViewModel
import com.bokehforu.openflip.core.settings.SettingsSleepTimerState
import com.bokehforu.openflip.core.ui.TestTags

private enum class SettingsPage {
    MAIN,
    TIME_FORMAT,
    ORIENTATION,
    VERSION,
    ABOUT,
    WAKE_LOCK,
    SLEEP_TIMER
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SettingsComposeSheet(
    visible: Boolean,
    settingsViewModel: SettingsViewModel,
    sleepTimerState: SettingsSleepTimerState,
    onDismiss: () -> Unit,
    onPerformClickFeedback: () -> Unit,
    onSetInteracting: (Boolean) -> Unit,
    onApplyThemeTransition: (Boolean) -> Unit,
    onSetOledProtection: (Boolean) -> Unit,
    onStartSleepTimer: (Int) -> Unit,
    onStopSleepTimer: () -> Unit,
    onOpenCustomSleepTimerDialog: () -> Unit,
    onOpenScreensaverSettings: () -> Unit,
    onOpenAlarmPermissionSettings: (Intent) -> Unit,
    onToggleHourlyChime: (Boolean) -> HourlyChimeToggleResult,
    onTestChime: () -> Unit,
    onOpenOriginalApp: () -> Unit,
    onContact: () -> Unit,
    onQuitApp: () -> Unit,
    onResetToDefaults: () -> Unit,
    packageNameProvider: () -> String,
) {
    if (!visible) return

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    val sheetHeightFraction = if (isLandscape) 0.86f else 0.90f
    val sheetHeight = (configuration.screenHeightDp * sheetHeightFraction).dp
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    var page by rememberSaveable { mutableStateOf(SettingsPage.MAIN) }
    val isDark by settingsViewModel.isDarkTheme.collectAsState()

    LaunchedEffect(visible) {
        page = SettingsPage.MAIN
        onSetInteracting(visible)
        if (visible) {
            sheetState.expand()
        }
    }

    ModalBottomSheet(
        onDismissRequest = {
            onSetInteracting(false)
            onDismiss()
        },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        dragHandle = null  // 移除拖拽手柄，减少误触
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(sheetHeight)
                .testTag(TestTags.SETTINGS_SHEET)
        ) {
            SettingsSheetHeader(
                page = page,
                onBack = { page = SettingsPage.MAIN },
                onClose = {
                    onSetInteracting(false)
                    onDismiss()
                }
            )

            Box(modifier = Modifier.weight(1f)) {
                AnimatedContent(
                    targetState = page,
                    label = "settings_page_transition",
                    transitionSpec = {
                        val isGoingBack = targetState == SettingsPage.MAIN
                        val enterTransition = if (isGoingBack) {
                            slideInHorizontally(
                                animationSpec = tween(
                                    durationMillis = 240,
                                    easing = LinearOutSlowInEasing
                                ),
                                initialOffsetX = { -it }
                            ) + fadeIn(
                                animationSpec = tween(
                                    durationMillis = 180,
                                    delayMillis = 20,
                                    easing = LinearOutSlowInEasing
                                )
                            ) + scaleIn(
                                initialScale = 0.98f,
                                animationSpec = tween(
                                    durationMillis = 240,
                                    easing = LinearOutSlowInEasing
                                )
                            )
                        } else {
                            slideInHorizontally(
                                animationSpec = tween(
                                    durationMillis = 240,
                                    easing = LinearOutSlowInEasing
                                ),
                                initialOffsetX = { it }
                            ) + fadeIn(
                                animationSpec = tween(
                                    durationMillis = 180,
                                    delayMillis = 20,
                                    easing = LinearOutSlowInEasing
                                )
                            ) + scaleIn(
                                initialScale = 0.98f,
                                animationSpec = tween(
                                    durationMillis = 240,
                                    easing = LinearOutSlowInEasing
                                )
                            )
                        }
                        val exitTransition = if (isGoingBack) {
                            slideOutHorizontally(
                                animationSpec = tween(
                                    durationMillis = 200,
                                    easing = FastOutSlowInEasing
                                ),
                                targetOffsetX = { it / 3 }
                            ) + fadeOut(
                                animationSpec = tween(
                                    durationMillis = 120,
                                    easing = FastOutSlowInEasing
                                )
                            ) + scaleOut(
                                targetScale = 0.985f,
                                animationSpec = tween(
                                    durationMillis = 160,
                                    easing = FastOutSlowInEasing
                                )
                            )
                        } else {
                            slideOutHorizontally(
                                animationSpec = tween(
                                    durationMillis = 200,
                                    easing = FastOutSlowInEasing
                                ),
                                targetOffsetX = { -it / 3 }
                            ) + fadeOut(
                                animationSpec = tween(
                                    durationMillis = 120,
                                    easing = FastOutSlowInEasing
                                )
                            ) + scaleOut(
                                targetScale = 0.985f,
                                animationSpec = tween(
                                    durationMillis = 160,
                                    easing = FastOutSlowInEasing
                                )
                            )
                        }
                        enterTransition.togetherWith(exitTransition)
                    }
                ) { targetPage ->
                    when (targetPage) {
                        SettingsPage.MAIN -> {
                            SettingsMainListScaffold(
                                isSheetExpanded = true,
                                onExpandSheet = {},
                                onDismissSheet = {
                                    onSetInteracting(false)
                                    onDismiss()
                                },
                                sectionTimeDisplay = {
                                    SettingsCardGroup {
                                        SettingsTimeDisplaySection(
                                            settingsViewModel = settingsViewModel,
                                            isDark = isDark,
                                            onNavigateTimeFormat = {
                                                onPerformClickFeedback()
                                                page = SettingsPage.TIME_FORMAT
                                            },
                                            onToggleShowSeconds = {
                                                onPerformClickFeedback()
                                                settingsViewModel.setShowSeconds(it)
                                            }
                                        )
                                    }
                                },
                                sectionAppearance = {
                                    SettingsCardGroup {
                                        SettingsAppearanceSection(
                                            settingsViewModel = settingsViewModel,
                                            isDark = isDark,
                                            onToggleShowFlaps = {
                                                onPerformClickFeedback()
                                                settingsViewModel.setShowFlaps(it)
                                            },
                                            onToggleSwipeToDim = {
                                                onPerformClickFeedback()
                                                settingsViewModel.setSwipeToDimEnabled(it)
                                            },
                                            onToggleLightMode = { isLightModeEnabled ->
                                                onPerformClickFeedback()
                                                onApplyThemeTransition(!isLightModeEnabled)
                                            },
                                            onNavigateOrientation = {
                                                onPerformClickFeedback()
                                                page = SettingsPage.ORIENTATION
                                            },
                                            onToggleScale = {
                                                onPerformClickFeedback()
                                                settingsViewModel.setScaleEnabled(it)
                                            },
                                            onToggleTimedBulbOff = {
                                                onPerformClickFeedback()
                                                settingsViewModel.setTimedBulbOffEnabled(it)
                                            }
                                        )
                                    }
                                },
                                sectionScreenWake = {
                                    SettingsCardGroup {
                                        SettingsScreenWakeSection(
                                            settingsViewModel = settingsViewModel,
                                            isDark = isDark,
                                            onNavigateWakeLock = {
                                                onPerformClickFeedback()
                                                page = SettingsPage.WAKE_LOCK
                                            },
                                            onToggleOledProtection = {
                                                onPerformClickFeedback()
                                                settingsViewModel.setOledProtectionEnabled(it)
                                                onSetOledProtection(it)
                                            }
                                        )
                                    }
                                },
                                sectionScreensaver = {
                                    SettingsCardGroup {
                                        SettingsScreensaverSection(onOpenScreensaverSettings = {
                                            onPerformClickFeedback()
                                            onOpenScreensaverSettings()
                                        })
                                    }
                                },
                                sectionSleepTimer = {
                                    SettingsCardGroup {
                                        SettingsSleepTimerSection(
                                            timerState = sleepTimerState,
                                            onOpenSleepTimerPage = {
                                                onPerformClickFeedback()
                                                page = SettingsPage.SLEEP_TIMER
                                            }
                                        )
                                    }
                                },
                                sectionFeedback = {
                                    SettingsCardGroup {
                                        SettingsFeedbackSection(
                                            settingsViewModel = settingsViewModel,
                                            isDark = isDark,
                                            packageNameProvider = packageNameProvider,
                                            onToggleHaptic = {
                                                onPerformClickFeedback()
                                                settingsViewModel.setHapticEnabled(it)
                                            },
                                            onToggleSound = {
                                                onPerformClickFeedback()
                                                settingsViewModel.setSoundEnabled(it)
                                            },
                                            onToggleHourlyChime = { enabled ->
                                                onPerformClickFeedback()
                                                onToggleHourlyChime(enabled)
                                            },
                                            onOpenAlarmPermissionSettings = onOpenAlarmPermissionSettings,
                                            onTestChime = {
                                                onPerformClickFeedback()
                                                onTestChime()
                                            }
                                        )
                                    }
                                },
                                sectionReset = {
                                    SettingsCardGroup {
                                        SettingsResetSection(onReset = {
                                            onPerformClickFeedback()
                                            onResetToDefaults()
                                        })
                                    }
                                },
                                sectionQuit = {
                                    SettingsCardGroup {
                                        SettingsQuitSection(onQuit = {
                                            onPerformClickFeedback()
                                            onQuitApp()
                                        })
                                    }
                                },
                                sectionInformation = {
                                    SettingsCardGroup {
                                        SettingsInformationSection(
                                            onNavigateVersion = {
                                                onPerformClickFeedback()
                                                page = SettingsPage.VERSION
                                            },
                                            onNavigateAbout = {
                                                onPerformClickFeedback()
                                                page = SettingsPage.ABOUT
                                            },
                                            onOpenOriginalApp = onOpenOriginalApp,
                                            onContact = onContact
                                        )
                                    }
                                }
                            )
                        }

                        SettingsPage.TIME_FORMAT -> {
                            SettingsSubpageScaffold {
                                SettingsTimeFormatPage(
                                    settingsViewModel = settingsViewModel,
                                    isDark = isDark,
                                    onSetMode = {
                                        onPerformClickFeedback()
                                        settingsViewModel.setTimeFormatMode(it)
                                    }
                                )
                            }
                        }

                        SettingsPage.ORIENTATION -> {
                            SettingsSubpageScaffold {
                                SettingsOrientationPage(
                                    settingsViewModel = settingsViewModel,
                                    isDark = isDark,
                                    onSetMode = {
                                        onPerformClickFeedback()
                                        settingsViewModel.setOrientationMode(it)
                                    }
                                )
                            }
                        }

                        SettingsPage.WAKE_LOCK -> {
                            SettingsSubpageScaffold {
                                SettingsWakeLockPage(
                                    settingsViewModel = settingsViewModel,
                                    isDark = isDark,
                                    onSetMode = {
                                        onPerformClickFeedback()
                                        settingsViewModel.setWakeLockMode(it)
                                    }
                                )
                            }
                        }

                        SettingsPage.SLEEP_TIMER -> {
                            SettingsSubpageScaffold {
                                SettingsSleepTimerPage(
                                    timerState = sleepTimerState,
                                    onStartSleepTimer = onStartSleepTimer,
                                    onStopSleepTimer = onStopSleepTimer,
                                    onNavigateBack = { page = SettingsPage.MAIN },
                                    onOpenCustomTimerDialog = onOpenCustomSleepTimerDialog
                                )
                            }
                        }

                        SettingsPage.VERSION -> VersionPage(isDarkTheme = isDark)
                        SettingsPage.ABOUT -> AboutPage(isDarkTheme = isDark)
                    }
                }
                
                // Gradient overlay for smooth header transition
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .align(Alignment.TopCenter)
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.surface,
                                    androidx.compose.ui.graphics.Color.Transparent
                                )
                            )
                        )
                )
            }
        }
    }
}

@Composable
private fun SettingsSubpageScaffold(
    content: @Composable () -> Unit,
) {
    val horizontalPadding = dimensionResource(R.dimen.spacingMlarge)
    LazyColumn(
        modifier = Modifier
            .fillMaxHeight()
            .padding(horizontal = horizontalPadding),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item(key = "subpage_content") {
            SettingsCardGroup { content() }
        }
    }
}

@Composable
private fun SettingsSheetHeader(
    page: SettingsPage,
    onBack: () -> Unit,
    onClose: () -> Unit,
) {
    val title = when (page) {
        SettingsPage.MAIN -> stringResource(R.string.titleSettings)
        SettingsPage.TIME_FORMAT -> stringResource(R.string.titleTimeFormat)
        SettingsPage.ORIENTATION -> stringResource(R.string.titleScreenOrientation)
        SettingsPage.VERSION -> stringResource(R.string.titleVersion)
        SettingsPage.ABOUT -> stringResource(R.string.titleAbout)
        SettingsPage.WAKE_LOCK -> stringResource(R.string.titleKeepScreenOn)
        SettingsPage.SLEEP_TIMER -> stringResource(R.string.titleSleepTimer)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            if (page != SettingsPage.MAIN) {
                SheetHeaderIconButton(
                    iconRes = R.drawable.icon_navigation_back_24dp,
                    contentDescription = stringResource(R.string.titleSettings),
                    onClick = onBack,
                    testTag = TestTags.SETTINGS_HEADER_BACK
                )
            }
        }
        Text(
            text = title.uppercase(java.util.Locale.getDefault()),
            style = MaterialTheme.typography.titleLarge,
            letterSpacing = 0.12.em,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
            SheetHeaderIconButton(
                iconRes = R.drawable.icon_navigation_close_24dp,
                contentDescription = stringResource(R.string.titleSettings),
                onClick = onClose,
                testTag = TestTags.SETTINGS_HEADER_CLOSE
            )
        }
    }
}

@Composable
private fun SheetHeaderIconButton(
    iconRes: Int,
    contentDescription: String,
    onClick: () -> Unit,
    testTag: String? = null,
) {
    val tagModifier = if (testTag != null) Modifier.testTag(testTag) else Modifier
    Surface(
        modifier = tagModifier

            .size(56.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = contentDescription,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
