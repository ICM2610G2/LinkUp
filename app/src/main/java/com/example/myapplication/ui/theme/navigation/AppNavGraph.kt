package com.example.myapplication.ui.theme.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

// Importamos tus pantallas según tu estructura real:

import com.example.myapplication.ui.theme.navigation.BottomNavItem.Home
import com.example.myapplication.ui.theme.navigation.BottomNavItem.Mapa
import com.example.myapplication.ui.theme.navigation.BottomNavItem.Carreras
import com.example.myapplication.ui.theme.navigation.BottomNavItem.Chat
import com.example.myapplication.ui.theme.navigation.BottomNavItem.Perfil
import com.example.myapplication.ui.theme.screens.Home
import com.example.myapplication.ui.theme.screens.Mapa
import com.example.myapplication.ui.theme.screens.Carreras
import com.example.myapplication.ui.theme.screens.Chat
import com.example.myapplication.ui.theme.screens.Login
import com.example.myapplication.ui.theme.screens.Perfil

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavGraph(
    navController: NavHostController,
    paddingValues: PaddingValues,
    startDestination: String = "login"
) {

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier.padding(paddingValues)
    ) {
        composable("login") {
            Login(onLoginSuccess = {
                navController.navigate(BottomNavItem.Home.route) {
                    popUpTo("login") { inclusive = true }
                }
            })
        }
        composable(BottomNavItem.Home.route) {
            Home()
        }
        composable(BottomNavItem.Mapa.route) {
            Mapa()
        }
        composable(BottomNavItem.Carreras.route) {
            Carreras()
        }
        composable(BottomNavItem.Chat.route) {
            Chat()
        }
        composable(BottomNavItem.Perfil.route) {
            // Muestra la pantalla Perfil
            Perfil()
        }
    }
}



