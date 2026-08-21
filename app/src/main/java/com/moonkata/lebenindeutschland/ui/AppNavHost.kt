package com.moonkata.lebenindeutschland.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.moonkata.lebenindeutschland.data.LidDatabase
import com.moonkata.lebenindeutschland.ui.theme.LidType
import kotlinx.coroutines.delay

private object Routes {
    const val PLACEHOLDER = "placeholder"
}

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.PLACEHOLDER) {
        composable(Routes.PLACEHOLDER) { PlaceholderScreen() }
    }
}

/**
 * Phase 0/1 scaffold check: confirms the Room DB prepopulates from assets. Replaced by
 * SprachePicker once Phase 2 starts.
 */
@Composable
private fun PlaceholderScreen() {
    val context = LocalContext.current
    var status by remember { mutableStateOf("문제 데이터 불러오는 중…") }

    LaunchedEffect(Unit) {
        val db = LidDatabase.get(context)
        // onCreate's prepopulate runs on a separate coroutine; poll briefly for it to land.
        repeat(20) {
            val count = db.questionDao().count()
            if (count > 0) {
                val sample = db.questionDao().randomGeneral(1).firstOrNull()
                status = "문제 ${count}개 로드됨\n\n${sample?.textDe ?: ""}"
                return@LaunchedEffect
            }
            delay(250)
        }
        status = "문제 데이터를 찾지 못했습니다."
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Leben in Deutschland", style = LidType.display)
            Text(status, style = LidType.explanation, modifier = Modifier.padding(top = 24.dp))
        }
    }
}
