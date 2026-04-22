package com.example.myapplication.ui.theme.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.google.firebase.auth.FirebaseUser
import com.example.myapplication.ui.theme.screens.Home
import com.example.myapplication.ui.theme.screens.Mapa
import com.example.myapplication.ui.theme.screens.Carreras
import com.example.myapplication.ui.theme.screens.Chat
import com.example.myapplication.ui.theme.screens.Perfil

@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String = BottomNavItem.Home.route,
    user: FirebaseUser?,  // ← Recibir el usuario
    onLogout: () -> Unit  // ← Callback para cerrar sesión
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
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
            Perfil(
                user = user,
                onLogout = onLogout
            )
        }
    }
}