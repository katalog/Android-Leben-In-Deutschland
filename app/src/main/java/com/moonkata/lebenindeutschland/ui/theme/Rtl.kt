package com.moonkata.lebenindeutschland.ui.theme

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.LayoutDirection

/** Arabic and Persian mirror the whole screen; German text stays LTR inside it — see plan.md "RTL". */
fun isRtlLanguage(appLanguageCode: String?): Boolean = appLanguageCode == "AR" || appLanguageCode == "FA"

/**
 * Wraps [content] so its ambient [LocalLayoutDirection] is RTL when [rtl] is true. Use around a
 * whole screen; use [GermanText] inside it for the German strings that must stay LTR regardless.
 */
@Composable
fun DirectionalContent(rtl: Boolean, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalLayoutDirection provides if (rtl) LayoutDirection.Rtl else LayoutDirection.Ltr, content = content)
}

/** German is always LTR even inside an RTL-mirrored screen (compose_starter/README.md). */
@Composable
fun GermanText(text: String, style: TextStyle, color: Color = Color.Unspecified, modifier: Modifier = Modifier) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Text(text, style = style, color = color, modifier = modifier)
    }
}
