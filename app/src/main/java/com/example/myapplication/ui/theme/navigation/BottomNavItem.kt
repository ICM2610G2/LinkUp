package com.example.myapplication.ui.theme.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

// Representa cada opción de la barra inferior (Bottom Navigation).
sealed class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    // Cada object es una pestaña (singleton).
    object Home : BottomNavItem("home", "Inicio", Icons.Default.Home)
    object Mapa : BottomNavItem("mapa", "Mapa", Icons.Default.Map)
    object Carreras : BottomNavItem("carreras", "Carreras", Icons.Default.DirectionsCar)
    object Chat : BottomNavItem("chat", "Chat", Icons.Default.Chat)
    object Perfil : BottomNavItem("perfil", "Perfil", Icons.Default.Person)

    companion object {
        val items = listOf(Home, Mapa, Carreras, Chat, Perfil)
    }
}