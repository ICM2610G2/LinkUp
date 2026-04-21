package com.example.myapplication

import android.location.Geocoder
import android.os.Bundle
import android.os.StrictMode
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.theme.navigation.MainScaffold
import org.osmdroid.config.Configuration

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val policy =
            StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        Configuration.getInstance().userAgentValue = "AndroidApp"
        setContent {
            // Si ya tienes tema, lo dejas y metes el MainScaffold dentro
            MyApplicationTheme {
                MainScaffold()
            }
        }
        lateinit var geocoder: Geocoder
        geocoder = Geocoder(this)
    }
}