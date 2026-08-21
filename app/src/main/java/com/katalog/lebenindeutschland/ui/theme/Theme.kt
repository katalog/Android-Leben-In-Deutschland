package com.katalog.lebenindeutschland.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private val LidColors = lightColorScheme(
    primary = Accent,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    background = Bg,
    onBackground = Ink,
    surface = Surface,
    onSurface = Ink,
    outline = Divider,
)

/** Radius is 0 everywhere. This is a rule of the design system, not a default. */
private val LidShapes = Shapes(
    extraSmall = RoundedCornerShape(0.dp),
    small = RoundedCornerShape(0.dp),
    medium = RoundedCornerShape(0.dp),
    large = RoundedCornerShape(0.dp),
    extraLarge = RoundedCornerShape(0.dp),
)

@Composable
fun LidTheme(content: @Composable () -> Unit) =
    MaterialTheme(
        colorScheme = LidColors,
        typography = LidTypography,
        shapes = LidShapes,
        content = content,
    )

/** The 2 dp rule that draws all structure. Use instead of Material's Divider. */
@Composable
fun Rule(modifier: Modifier = Modifier) =
    Box(modifier.fillMaxWidth().height(2.dp).background(Divider))

object LidSpace {
    val x1 = 4.dp
    val x2 = 8.dp
    val x3 = 12.dp
    val x4 = 16.dp
    val x6 = 24.dp
    val x8 = 32.dp

    /** Every screen's horizontal padding. */
    val gutter = 20.dp
    /** Leading accent bar on a selected / correct row. */
    val accentBar = 4.dp
    val rule = 2.dp
}
