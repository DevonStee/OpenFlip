/*
 * Copyright (C) 2026 DevonStee
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.bokehforu.openflip.feature.clock.view.theme

import android.content.Context
import android.util.TypedValue
import androidx.core.content.ContextCompat
import com.bokehforu.openflip.feature.clock.R
import com.bokehforu.openflip.feature.clock.view.card.FlipCardComponent
import com.bokehforu.openflip.core.R as CoreR

/**
 * Handles applying theme colors to FlipCardComponents.
 * Uses dynamic attribute resolution for flexibility.
 */
class FlipClockThemeApplier(
    private val context: Context,
    private val hourCard: FlipCardComponent,
    private val minuteCard: FlipCardComponent
) {

    fun applyTheme(isDark: Boolean) {
        val themeResId = if (isDark) CoreR.style.Theme_OpenFlip_Dark else CoreR.style.Theme_OpenFlip_Light
        val themedContext = android.view.ContextThemeWrapper(context, themeResId)
        
        val textColor = resolveColor(themedContext, CoreR.attr.cardTextColor)
        val cardColor = resolveColor(themedContext, CoreR.attr.cardBackgroundColor)
        val shadowColor = resolveColor(themedContext, CoreR.attr.cardShadowColor)
        val shadowEdgeColor = resolveColor(themedContext, CoreR.attr.cardShadowEdgeColor)
        val rimColor = resolveColor(themedContext, CoreR.attr.cardRimHighlightColor)
        val cutHighlight = resolveColor(themedContext, CoreR.attr.cardCutHighlightColor)
        val cutShadow = resolveColor(themedContext, CoreR.attr.cardCutShadowColor)
        val noiseColor = resolveColor(themedContext, CoreR.attr.cardNoiseColor)

        hourCard.updateColors(textColor, cardColor, shadowColor, shadowEdgeColor, rimColor, cutHighlight, cutShadow, noiseColor)
        minuteCard.updateColors(textColor, cardColor, shadowColor, shadowEdgeColor, rimColor, cutHighlight, cutShadow, noiseColor)
    }
    
    private fun resolveColor(context: Context, attrId: Int): Int {
        val typedValue = TypedValue()
        val theme = context.theme
        if (theme.resolveAttribute(attrId, typedValue, true)) {
            if (typedValue.resourceId != 0) {
                 return ContextCompat.getColor(context, typedValue.resourceId)
            }
            return typedValue.data
        }
        return android.graphics.Color.MAGENTA
    }
}
