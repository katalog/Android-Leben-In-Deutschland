package com.moonkata.lebenindeutschland.ui.language

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.moonkata.lebenindeutschland.ui.theme.LidTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SpracheScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun allNineLanguagesAreListed() {
        composeRule.setContent { LidTheme { SpracheScreen(onLanguageChosen = {}) } }

        listOf("Türkçe", "한국어", "العربية", "Русский", "Українська", "English", "فارسی", "Română", "Español")
            .forEach { composeRule.onNodeWithText(it).assertExists() }
    }

    @Test
    fun weiterStaysDisabledUntilARowIsPicked_thenReportsThatLanguagesCode() {
        var chosen: String? = null
        composeRule.setContent { LidTheme { SpracheScreen(onLanguageChosen = { chosen = it }) } }

        composeRule.onNodeWithText("Weiter").assertIsNotEnabled()

        composeRule.onNodeWithText("한국어").performClick()
        composeRule.onNodeWithText("Weiter").assertIsEnabled().performClick()

        assertEquals("KO", chosen)
    }
}
