package com.example.lid.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font as GoogleFont
import androidx.compose.ui.text.googlefonts.GoogleFont as GF
import androidx.compose.ui.text.googlefonts.GoogleFontProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Requires: implementation "androidx.compose.ui:ui-text-google-fonts:<version>"
// and res/values/font_certs.xml (see compose_starter/README.md).
private val provider = GoogleFontProvider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs,
)

val Archivo = FontFamily(
    GoogleFont(GF("Archivo"), provider, FontWeight.Normal),
    GoogleFont(GF("Archivo"), provider, FontWeight.Medium),
    GoogleFont(GF("Archivo"), provider, FontWeight.SemiBold),
    GoogleFont(GF("Archivo"), provider, FontWeight.Bold),
    GoogleFont(GF("Archivo"), provider, FontWeight.ExtraBold),
)

/**
 * Translation text may be Korean, Arabic or Cyrillic. Archivo has no Hangul or Arabic,
 * so translated strings must use this family, not [Archivo].
 */
val I18n = FontFamily(
    GoogleFont(GF("Noto Sans"), provider, FontWeight.Normal),
    GoogleFont(GF("Noto Sans KR"), provider, FontWeight.Normal),
    GoogleFont(GF("Noto Sans KR"), provider, FontWeight.Bold),
    GoogleFont(GF("Noto Sans Arabic"), provider, FontWeight.Normal),
    GoogleFont(GF("Noto Sans Arabic"), provider, FontWeight.SemiBold),
)

private const val TNUM = "tnum"

object LidType {
    /** Home greeting, language-picker heading. */
    val display = TextStyle(
        fontFamily = Archivo, fontWeight = FontWeight.ExtraBold,
        fontSize = 32.sp, lineHeight = 34.sp, letterSpacing = (-0.035).em,
    )
    /** The question itself. */
    val question = TextStyle(
        fontFamily = Archivo, fontWeight = FontWeight.ExtraBold,
        fontSize = 26.sp, lineHeight = 28.6.sp, letterSpacing = (-0.03).em,
    )
    /** White heading inside a red poster panel. */
    val poster = TextStyle(
        fontFamily = Archivo, fontWeight = FontWeight.ExtraBold,
        fontSize = 27.sp, lineHeight = 28.sp, letterSpacing = (-0.035).em,
    )
    /** The 28 in "28 / 33". */
    val scoreNumeral = TextStyle(
        fontFamily = Archivo, fontWeight = FontWeight.ExtraBold,
        fontSize = 74.sp, lineHeight = 61.sp, letterSpacing = (-0.05).em,
        fontFeatureSettings = TNUM,
    )
    val answer = TextStyle(
        fontFamily = Archivo, fontWeight = FontWeight.Medium,
        fontSize = 15.sp, lineHeight = 20.sp,
    )
    val answerCorrect = answer.copy(fontWeight = FontWeight.SemiBold)
    val answerLetter = TextStyle(
        fontFamily = Archivo, fontWeight = FontWeight.ExtraBold,
        fontSize = 14.sp, lineHeight = 19.sp,
    )
    /** Question translation. */
    val translation = TextStyle(
        fontFamily = I18n, fontWeight = FontWeight.Medium,
        fontSize = 15.sp, lineHeight = 22.sp,
    )
    /** Answer translation. */
    val translationSmall = TextStyle(
        fontFamily = I18n, fontWeight = FontWeight.Normal,
        fontSize = 13.5f.sp, lineHeight = 20.sp,
    )
    /** All-caps kickers, counters, tab labels. */
    val label = TextStyle(
        fontFamily = Archivo, fontWeight = FontWeight.Bold,
        fontSize = 11.sp, lineHeight = 11.sp, letterSpacing = 0.10.em,
        fontFeatureSettings = TNUM,
    )
    val rowTitle = TextStyle(
        fontFamily = Archivo, fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp, lineHeight = 19.sp,
    )
    val explanation = TextStyle(
        fontFamily = Archivo, fontWeight = FontWeight.Normal,
        fontSize = 13.sp, lineHeight = 20.sp, color = Neutral700,
    )
}

/** Material3 mapping, for components that read the theme rather than LidType directly. */
val LidTypography = Typography(
    displayLarge = LidType.display,
    headlineMedium = LidType.question,
    bodyLarge = LidType.answer,
    bodyMedium = LidType.explanation,
    labelSmall = LidType.label,
)
