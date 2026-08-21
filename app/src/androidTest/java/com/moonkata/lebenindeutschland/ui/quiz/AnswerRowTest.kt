package com.moonkata.lebenindeutschland.ui.quiz

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.moonkata.lebenindeutschland.ui.theme.LidTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class AnswerRowTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun unansweredRow_invokesCallbackOnClick() {
        var clicks = 0
        composeRule.setContent {
            LidTheme { AnswerRow('A', "Berlin", AnswerRowState.UNANSWERED) { clicks++ } }
        }

        composeRule.onNodeWithText("Berlin").performClick()

        assertEquals(1, clicks)
    }

    @Test
    fun answeredRow_ignoresFurtherClicks() {
        var clicks = 0
        composeRule.setContent {
            LidTheme { AnswerRow('A', "Berlin", AnswerRowState.CORRECT) { clicks++ } }
        }

        composeRule.onNodeWithText("Berlin").performClick()

        assertEquals("a row that already shows correct/wrong state must not be clickable", 0, clicks)
    }

    @Test
    fun translationText_rendersWhenProvided() {
        composeRule.setContent {
            LidTheme { AnswerRow('A', "Berlin", AnswerRowState.UNANSWERED, translation = "베를린") {} }
        }

        composeRule.onNodeWithText("베를린").assertExists()
    }
}
