package com.moonkata.lebenindeutschland.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

enum class BottomTab(val label: String) {
    UEBEN("ÜBEN"), PRUEFUNG("PRÜFUNG"), FORTSCHRITT("FORTSCHRITT"), MEHR("MEHR")
}

/** The design's 4-cell bottom bar: 42 dp cells separated by 2 dp vertical rules, active tab in accent700. */
@Composable
fun LidBottomBar(active: BottomTab, onSelect: (BottomTab) -> Unit) {
    Rule()
    Row(modifier = Modifier.fillMaxWidth().navigationBarsPadding().height(42.dp)) {
        BottomTab.entries.forEachIndexed { index, tab ->
            if (index > 0) {
                Box(modifier = Modifier.width(2.dp).fillMaxHeight().background(Divider))
            }
            Box(
                modifier = Modifier.weight(1f).fillMaxHeight().clickable { onSelect(tab) },
                contentAlignment = Alignment.Center,
            ) {
                Text(tab.label, style = LidType.label, color = if (tab == active) Accent700 else Neutral700)
            }
        }
    }
}
