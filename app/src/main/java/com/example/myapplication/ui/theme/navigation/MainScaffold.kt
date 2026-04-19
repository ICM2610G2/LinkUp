package com.example.myapplication.ui.theme.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.material3.Scaffold
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScaffold() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val mostrarBottomBar = currentRoute != "login"

    Scaffold(
        bottomBar = {
            if (mostrarBottomBar) {
                BottomBar(navController = navController)
            }
        }
    ) { innerPadding ->
        AppNavGraph(
            navController = navController,
            innerPadding,
            startDestination = "login"
        )
    }
}

/**
 * BottomBar:
 * Dibuja la barra inferior con 5 botones.
 *
 * @param navController: se usa para navegar a la ruta del item tocado.
 */
@Composable
fun BottomBar(navController: NavHostController) {

    /**
     * currentBackStackEntryAsState():
     * - Observa la navegación actual.
     * - Cada vez que cambie la pantalla, esto se actualiza y recompone la UI.
     */
    val backStackEntry by navController.currentBackStackEntryAsState()

    // route actual: "home", "mapa", etc.
    val currentRoute = backStackEntry?.destination?.route


    NavigationBar {

        // Recorremos los 5 items definidos en BottomNavItem.items
        BottomNavItem.items.forEach { item ->

            // selected = true si el item coincide con la pantalla actual
            val selected = currentRoute == item.route


            NavigationBarItem(
                selected = selected,

                onClick = {
                    navController.navigate(item.route) {
                        val startRoute = navController.graph.startDestinationRoute
                        if (startRoute != null) {
                            popUpTo(startRoute) { saveState = true }
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },

                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label
                    )
                },

                label = {
                    Text(text = item.label)
                },

                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFFFF9800),
                    selectedTextColor = Color(0xFFFF9800),
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}