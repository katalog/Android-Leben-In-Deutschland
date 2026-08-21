package com.moonkata.lebenindeutschland

import android.content.Context
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test

/**
 * End-to-end regression test for the first-launch flow: language, then Bundesland (added on
 * user request — previously only language was asked, Bundesland had to be set later from Mehr).
 *
 * Property initializers run in declaration order, so clearing SharedPreferences here happens
 * before `composeRule` (below) launches MainActivity — createAndroidComposeRule launches the
 * activity as part of applying the @Rule, which runs after the test instance (and therefore
 * every property initializer above the rule) is fully constructed. Clearing in @Before would be
 * too late: MainActivity.onCreate already reads the prefs to pick a start destination by then.
 */
class OnboardingFlowTest {
    private val setupDone = run {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        context.getSharedPreferences("lid_prefs", Context.MODE_PRIVATE).edit().clear().commit()
        // Otherwise the POST_NOTIFICATIONS system prompt covers the language list on first launch.
        instrumentation.uiAutomation.grantRuntimePermission(context.packageName, "android.permission.POST_NOTIFICATIONS")
    }

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun freshInstall_asksLanguageThenBundesland_thenLandsOnStart() {
        // Room prepopulation + the language screen render asynchronously on first launch.
        composeRule.waitUntil(timeoutMillis = 20_000) {
            composeRule.onAllNodesWithText("한국어").fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty()
        }
        composeRule.onNodeWithText("한국어").performClick()
        composeRule.onNodeWithText("Weiter").performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("Bayern").fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty()
        }
        composeRule.onNodeWithText("Bayern").performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("Guten Tag").fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty()
        }
        composeRule.onNodeWithText("Guten Tag").assertExists()
    }
}
