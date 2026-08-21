package com.moonkata.lebenindeutschland.ui.more

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.moonkata.lebenindeutschland.data.Languages
import com.moonkata.lebenindeutschland.data.Topics
import com.moonkata.lebenindeutschland.data.UserPrefs
import com.moonkata.lebenindeutschland.ui.theme.BottomTab
import com.moonkata.lebenindeutschland.ui.theme.LidBottomBar
import com.moonkata.lebenindeutschland.ui.theme.LidSpace
import com.moonkata.lebenindeutschland.ui.theme.LidType
import com.moonkata.lebenindeutschland.ui.theme.Neutral600
import com.moonkata.lebenindeutschland.ui.theme.Rule

/**
 * Placeholder home for settings not built yet (translation language-pack management is Phase 6).
 * For now: shows the current translation language and Bundesland, and lets you re-pick the
 * language.
 */
@Composable
fun MehrScreen(onChangeLanguage: () -> Unit, onSelectTab: (BottomTab) -> Unit) {
    val context = LocalContext.current
    val prefs = UserPrefs(context)
    val language = Languages.all.find { it.code == prefs.selectedLanguage }
    val bundesland = prefs.bundesland?.let { Topics.bundeslandNames[it] }

    Surface(modifier = Modifier.fillMaxSize().statusBarsPadding(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Mehr", style = LidType.display, modifier = Modifier.padding(LidSpace.gutter))
                Rule()

                InfoRow("Übersetzungssprache", language?.native ?: "—", onClick = onChangeLanguage)
                InfoRow("Bundesland", bundesland ?: "—")
            }
            LidBottomBar(active = BottomTab.MEHR, onSelect = onSelectTab)
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String, onClick: (() -> Unit)? = null) {
    Column(modifier = Modifier.fillMaxWidth().let { if (onClick != null) it.clickable(onClick = onClick) else it }) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp, horizontal = LidSpace.gutter),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
        ) {
            Text(label, style = LidType.rowTitle)
            Text(value, style = LidType.rowTitle, color = Neutral600)
        }
        Rule()
    }
}
