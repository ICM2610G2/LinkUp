package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.theme.navigation.MainScaffold

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            // Si ya tienes tema, lo dejas y metes el MainScaffold dentro
            MyApplicationTheme {
                MainScaffold()
            }
        }
    }
}