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
import com.moonkata.lebenindeutschland.ui.theme.LidSpace
import com.moonkata.lebenindeutschland.ui.theme.LidType
import com.moonkata.lebenindeutschland.ui.theme.Rule

/**
 * Not in the original design bundle — the design assumes a Bundesland is already chosen and
 * only shows it on Start. This bare list is the minimum needed to actually set one.
 */
@Composable
fun BundeslandPickerScreen(onChosen: (String) -> Unit) {
    Surface(modifier = Modifier.fillMaxSize().safeDrawingPadding(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                "Welches Bundesland?",
                style = LidType.display,
                modifier = Modifier.padding(top = 22.dp, start = LidSpace.gutter, end = LidSpace.gutter, bottom = 18.dp),
            )
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
