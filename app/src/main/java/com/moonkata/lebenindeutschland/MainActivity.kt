package com.moonkata.lebenindeutschland

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.moonkata.lebenindeutschland.ui.AppNavHost
import com.moonkata.lebenindeutschland.ui.theme.LidTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LidTheme {
                AppNavHost()
            }
        }
    }
}
