package com.moonkata.lebenindeutschland.data

data class Language(val code: String, val native: String, val german: String)

object Languages {
    val all = listOf(
        Language("TR", "Türkçe", "Türkisch"),
        Language("KO", "한국어", "Koreanisch"),
        Language("AR", "العربية", "Arabisch"),
        Language("RU", "Русский", "Russisch"),
        Language("UK", "Українська", "Ukrainisch"),
        Language("EN", "English", "Englisch"),
        Language("FA", "فارسی", "Persisch"),
        Language("RO", "Română", "Rumänisch"),
        Language("ES", "Español", "Spanisch"),
    )
}
