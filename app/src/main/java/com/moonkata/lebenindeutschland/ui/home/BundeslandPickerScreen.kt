package com.moonkata.lebenindeutschland.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.moonkata.lebenindeutschland.data.Topics
import com.moonkata.lebenindeutschland.ui.theme.Accent700
import com.moonkata.lebenindeutschland.ui.theme.LidSpace
import com.moonkata.lebenindeutschland.ui.theme.LidType
import com.moonkata.lebenindeutschland.ui.theme.Rule

/**
 * Not in the original design bundle — the design assumes a Bundesland is already chosen and
 * only shows it on Start. This bare list is the minimum needed to actually set one. Doubles as
 * onboarding step 2 (the language screen's "SCHRITT 1 VON 2" kicker implied a step 2).
 */
@Composable
fun BundeslandPickerScreen(isOnboardingStep: Boolean = false, onChosen: (String) -> Unit) {
    Surface(modifier = Modifier.fillMaxSize().safeDrawingPadding(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.padding(top = 22.dp, start = LidSpace.gutter, end = LidSpace.gutter, bottom = 18.dp)) {
                if (isOnboardingStep) {
                    Text("SCHRITT 2 VON 2", style = LidType.label, color = Accent700)
                }
                Text(
                    "Welches Bundesland?",
                    style = LidType.display,
                    modifier = Modifier.padding(top = if (isOnboardingStep) LidSpace.x2 else 0.dp),
                )
                if (isOnboardingStep) {
                    Text(
                        "Für 3 der 33 Prüfungsfragen und zum Üben von Landesfragen.",
                        style = LidType.explanation,
                        modifier = Modifier.padding(top = LidSpace.x2),
                    )
                }
            }
            Rule()
            Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                Topics.bundeslandNames.forEach { (code, name) ->
                    Column(modifier = Modifier.fillMaxWidth().clickable { onChosen(code) }) {
                        Text(name, style = LidType.rowTitle, modifier = Modifier.padding(vertical = 14.dp, horizontal = LidSpace.gutter))
                        Rule()
                    }
                }
            }
        }
    }
}
