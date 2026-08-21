package com.moonkata.lebenindeutschland.data

import android.content.Context

/** Small persisted settings — language, translation toggle, home Bundesland. Plain SharedPreferences is enough for three scalar values. */
class UserPrefs(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("lid_prefs", Context.MODE_PRIVATE)

    var selectedLanguage: String?
        get() = prefs.getString(KEY_LANGUAGE, null)
        set(value) = prefs.edit().putString(KEY_LANGUAGE, value).apply()

    var bundesland: String?
        get() = prefs.getString(KEY_BUNDESLAND, null)
        set(value) = prefs.edit().putString(KEY_BUNDESLAND, value).apply()

    var translationsVisible: Boolean
        get() = prefs.getBoolean(KEY_TRANSLATIONS_VISIBLE, true)
        set(value) = prefs.edit().putBoolean(KEY_TRANSLATIONS_VISIBLE, value).apply()

    companion object {
        private const val KEY_LANGUAGE = "selected_language"
        private const val KEY_BUNDESLAND = "bundesland"
        private const val KEY_TRANSLATIONS_VISIBLE = "translations_visible"
    }
}
