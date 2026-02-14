package com.bokehforu.openflip.feature.settings.ui.compose

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bokehforu.openflip.feature.settings.R
import com.bokehforu.openflip.feature.settings.ui.theme.*

/**
 * Container for a group of settings items.
 */
@Composable
fun SettingsCardGroup(
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column(content = content)
    }
}

/**
 * Switch item for boolean settings.
 */
@Composable
fun SettingsSwitchItem(
    @DrawableRes iconRes: Int?,
    title: String,
    description: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    isDarkTheme: Boolean,
    checkedTrackColor: Color? = null,
    checkedThumbColor: Color? = null,
    testTag: String? = null
) {
    val switchColors = SwitchDefaults.colors(
        checkedThumbColor = checkedThumbColor ?: if (isDarkTheme) SwitchCheckedThumbDark else SwitchCheckedThumbLight,
        checkedTrackColor = checkedTrackColor ?: if (isDarkTheme) SwitchCheckedTrackDark else SwitchCheckedTrackLight,
        checkedBorderColor = checkedTrackColor ?: if (isDarkTheme) SwitchCheckedBorderDark else SwitchCheckedBorderLight,
        uncheckedThumbColor = if (isDarkTheme) SwitchUncheckedThumbDark else SwitchUncheckedThumbLight,
        uncheckedTrackColor = if (isDarkTheme) SwitchUncheckedTrackDark else SwitchUncheckedTrackLight,
        uncheckedBorderColor = if (isDarkTheme) SwitchUncheckedBorderDark else SwitchUncheckedBorderLight
    )

    val tagModifier = if (testTag != null) Modifier.testTag(testTag) else Modifier
    Row(
        modifier = tagModifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
                onClick = { onCheckedChange(!checked) }
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = if (description.isNullOrEmpty()) Alignment.CenterVertically else Alignment.Top
    ) {
        if (iconRes != null) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.requiredSize(28.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(16.dp))
        }

        Column(
            modifier = Modifier.weight(1f).padding(end = 12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Light,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (!description.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 14.sp
                )
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = null,
            colors = switchColors,
            modifier = Modifier.align(Alignment.CenterVertically)
        )
    }
}

/**
 * A neutral gray divider that avoids the purple tint of outlineVariant.
 */
@Composable
fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
    )
}

/**
 * Navigation item for settings with balanced vertical centering.
 */
@Composable
fun SettingsNavigationItem(
    @DrawableRes iconRes: Int,
    title: String,
    valueText: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    description: String? = null,
    valueTextColor: Color? = null,
    testTag: String? = null
) {
    val tagModifier = if (testTag != null) Modifier.testTag(testTag) else Modifier
    Row(
        modifier = tagModifier
            .then(modifier)
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.requiredSize(28.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Light,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (!description.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 14.sp
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 8.dp)
        ) {
            if (valueText != null) {
                Text(
                    text = valueText,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = valueTextColor ?: MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }

            Icon(
                painter = painterResource(id = R.drawable.icon_navigation_chevron_right_24dp),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Unified list-style Action item with full background color support.
 */
@Composable
fun SettingsActionItem(
    @DrawableRes iconRes: Int,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.Transparent,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    description: String? = null,
    iconSize: androidx.compose.ui.unit.Dp = 28.dp,
    iconHeight: androidx.compose.ui.unit.Dp? = null,
    iconOffsetY: androidx.compose.ui.unit.Dp = 0.dp,
    minHeight: androidx.compose.ui.unit.Dp = 56.dp,
    testTag: String? = null
) {
    val tagModifier = if (testTag != null) Modifier.testTag(testTag) else Modifier
    Row(
        modifier = tagModifier
            .then(modifier)
            .fillMaxWidth()
            .heightIn(min = minHeight)
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(28.dp)
                .heightIn(min = 28.dp),
            contentAlignment = Alignment.Center
        ) {
            val iconModifier = if (iconHeight != null) {
                Modifier.width(iconSize).height(iconHeight).offset(y = iconOffsetY)
            } else {
                Modifier.requiredSize(iconSize)
            }
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = contentColor,
                modifier = iconModifier
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Light,
                color = contentColor
            )
            if (!description.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = contentColor.copy(alpha = 0.7f),
                    lineHeight = 14.sp
                )
            }
        }
    }
}

/**
 * Radio option item for settings with selection state highlighting.
 */
@Composable
fun SettingsRadioItem(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean,
    testTag: String? = null
) {
    val backgroundColor = if (isSelected) {
        if (isDarkTheme) RadioSelectedDark else RadioSelectedLight
    } else {
        Color.Transparent
    }

    val tagModifier = if (testTag != null) Modifier.testTag(testTag) else Modifier
    Row(
        modifier = tagModifier
            .then(modifier)
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .background(backgroundColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Light,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
