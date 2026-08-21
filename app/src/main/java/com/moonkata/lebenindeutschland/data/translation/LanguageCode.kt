package com.moonkata.lebenindeutschland.data.translation

import com.google.mlkit.nl.translate.TranslateLanguage

/** Maps this app's 2-letter language codes (see Languages.kt) to ML Kit's TranslateLanguage constants. */
object LanguageCode {
    private val toMlKit = mapOf(
        "TR" to TranslateLanguage.TURKISH,
        "KO" to TranslateLanguage.KOREAN,
        "AR" to TranslateLanguage.ARABIC,
        "RU" to TranslateLanguage.RUSSIAN,
        "UK" to TranslateLanguage.UKRAINIAN,
        "EN" to TranslateLanguage.ENGLISH,
        "FA" to TranslateLanguage.PERSIAN,
        "RO" to TranslateLanguage.ROMANIAN,
        "ES" to TranslateLanguage.SPANISH,
    )

    const val GERMAN = TranslateLanguage.GERMAN

    fun mlKit(appCode: String): String = toMlKit.getValue(appCode)
}
