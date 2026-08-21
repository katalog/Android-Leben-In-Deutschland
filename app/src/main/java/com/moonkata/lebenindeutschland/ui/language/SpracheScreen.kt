package com.moonkata.lebenindeutschland.ui.language

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moonkata.lebenindeutschland.data.Language
import com.moonkata.lebenindeutschland.data.Languages
import com.moonkata.lebenindeutschland.ui.theme.Accent
import com.moonkata.lebenindeutschland.ui.theme.Accent100
import com.moonkata.lebenindeutschland.ui.theme.Accent700
import com.moonkata.lebenindeutschland.ui.theme.Divider
import com.moonkata.lebenindeutschland.ui.theme.LidButton
import com.moonkata.lebenindeutschland.ui.theme.LidSpace
import com.moonkata.lebenindeutschland.ui.theme.LidType
import com.moonkata.lebenindeutschland.ui.theme.Neutral600
import com.moonkata.lebenindeutschland.ui.theme.Neutral700
import com.moonkata.lebenindeutschland.ui.theme.Rule

@Composable
fun SpracheScreen(onLanguageChosen: (String) -> Unit) {
    var selected by remember { mutableStateOf<Language?>(null) }

    Surface(modifier = Modifier.fillMaxSize().safeDrawingPadding(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.padding(top = 22.dp, start = LidSpace.gutter, end = LidSpace.gutter, bottom = 18.dp)) {
                Text("SCHRITT 1 VON 2", style = LidType.label, color = Accent700)
                Text(
                    "In welcher Sprache sollen wir übersetzen?",
                    style = LidType.display,
                    modifier = Modifier.padding(top = LidSpace.x2),
                )
                Text(
                    "Die Fragen bleiben immer auf Deutsch. Die Prüfung ist auf Deutsch.",
                    style = LidType.explanation,
                    modifier = Modifier.padding(top = LidSpace.x2),
                )
            }
            Rule()

            Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                Languages.all.forEach { language ->
                    LanguageRow(language, isSelected = selected == language) { selected = language }
                }
            }

            Rule()
            Row(modifier = Modifier.padding(vertical = LidSpace.x4, horizontal = LidSpace.gutter)) {
                LidButton(
                    onClick = { selected?.let { onLanguageChosen(it.code) } },
                    enabled = selected != null,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Weiter") }
            }
        }
    }
}

@Composable
private fun LanguageRow(language: Language, isSelected: Boolean, onClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().background(if (isSelected) Accent100 else Color.Transparent)) {
        Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
            Box(modifier = Modifier.width(LidSpace.accentBar).fillMaxHeight().background(if (isSelected) Accent else Color.Transparent))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 13.dp, horizontal = LidSpace.gutter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(language.native, style = LidType.rowTitle)
                    Text(
                        language.german,
                        style = LidType.rowTitle.copy(fontSize = 12.sp, fontWeight = FontWeight.Normal, letterSpacing = 0.sp),
                        color = Neutral600,
                    )
                }
                CodeChip(language.code, isSelected)
            }
        }
    }
    Rule()
}

@Composable
private fun CodeChip(code: String, isSelected: Boolean) {
    val background = if (isSelected) Accent else Color.Transparent
    val contentColor = if (isSelected) Color.White else Neutral700
    Box(
        modifier = Modifier
            .background(background)
            .padding(horizontal = 7.dp, vertical = 5.dp),
    ) {
        Text(code, style = LidType.label, color = contentColor)
    }
}
