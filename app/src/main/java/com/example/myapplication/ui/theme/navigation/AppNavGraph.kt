package com.example.myapplication.ui.theme.navigation

import androidx.compose.runtime.Composable
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
import com.example.myapplication.ui.theme.screens.Perfil

@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String = BottomNavItem.Home.route
) {

    //  NavHost: Es el contenedor que muestra la pantalla correspondiente a la ruta actual.
     // Cada vez que se navega se cambia el destino y se renderiza el composable asociado.

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
       // Composable para cada pantalla, con su ruta y contenido.

        composable(BottomNavItem.Home.route) {
            // Muestra la pantalla Home
            Home()
        }

        composable(BottomNavItem.Mapa.route) {
            // Muestra la pantalla Mapa
            Mapa()
        }

        composable(BottomNavItem.Carreras.route) {
            // Muestra la pantalla Carreras
            Carreras()
        }

        composable(BottomNavItem.Chat.route) {
            // Muestra la pantalla Chat
            Chat()
        }

        composable(BottomNavItem.Perfil.route) {
            // Muestra la pantalla Perfil
            Perfil()
        }
    }
}



