package com.moonkata.lebenindeutschland.ui.quiz

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.moonkata.lebenindeutschland.ui.theme.Accent
import com.moonkata.lebenindeutschland.ui.theme.Accent100
import com.moonkata.lebenindeutschland.ui.theme.Accent700
import com.moonkata.lebenindeutschland.ui.theme.LidSpace
import com.moonkata.lebenindeutschland.ui.theme.LidType
import com.moonkata.lebenindeutschland.ui.theme.Neutral200
import com.moonkata.lebenindeutschland.ui.theme.Neutral600
import com.moonkata.lebenindeutschland.ui.theme.Rule

enum class AnswerRowState { UNANSWERED, CORRECT, WRONG_PICKED, WRONG_UNPICKED }

@Composable
fun AnswerRow(letter: Char, text: String, state: AnswerRowState, onClick: () -> Unit) {
    val rowBackground = when (state) {
        AnswerRowState.CORRECT -> Accent100
        AnswerRowState.WRONG_PICKED -> Neutral200
        else -> Color.Transparent
    }
    val letterColor = when (state) {
        AnswerRowState.CORRECT -> Accent700
        AnswerRowState.WRONG_PICKED -> Neutral600
        else -> Color.Unspecified
    }
    val textStyle = if (state == AnswerRowState.CORRECT) LidType.answerCorrect else LidType.answer
    val textColor = if (state == AnswerRowState.WRONG_PICKED) Neutral600 else Color.Unspecified

    Box(modifier = Modifier.fillMaxWidth().background(rowBackground)) {
        Row(
            // IntrinsicSize.Min: without it, this Row inherits the unbounded height offered by
            // the scrolling column above, so fillMaxHeight() below fills the whole screen instead
            // of just this row.
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .clickable(enabled = state == AnswerRowState.UNANSWERED, onClick = onClick),
        ) {
            Box(
                modifier = Modifier
                    .width(LidSpace.accentBar)
                    .fillMaxHeight()
                    .background(if (state == AnswerRowState.CORRECT) Accent else Color.Transparent),
            )
            Row(modifier = Modifier.padding(vertical = 14.dp, horizontal = LidSpace.gutter)) {
                Text(letter.toString(), style = LidType.answerLetter, color = letterColor, modifier = Modifier.width(16.dp))
                Box(modifier = Modifier.width(12.dp))
                Text(text, style = textStyle, color = textColor)
            }
        }
    }
    Rule()
}
