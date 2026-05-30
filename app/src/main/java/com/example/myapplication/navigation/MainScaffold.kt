package com.example.myapplication.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.*
import com.example.myapplication.data.models.User
import com.example.myapplication.repository.UserRepository
import com.example.myapplication.ui.theme.screens.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Box


@Composable
fun MainScaffold(
    user: User?,
    onLogout: () -> Unit = {},
    onAccountDeleted: () -> Unit = {}
) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val userRepository = remember { UserRepository() }
    val firebaseUser = FirebaseAuth.getInstance().currentUser

    var userData by remember { mutableStateOf(user) }

    LaunchedEffect(user) {
        userData = user
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF1A1A1A),
                contentColor = Color.White
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val items = listOf(
                    NavItem("home", "Home", Icons.Default.Home),
                    NavItem("mapa", "Mapa", Icons.Default.Map),
                    NavItem("carreras", "Carreras", Icons.Default.EmojiEvents),
                    NavItem("chat", "Chat", Icons.Default.Chat),
                    NavItem("perfil", "Perfil", Icons.Default.Person)
                )

                items.forEach { item ->
                    NavigationBarItem(
                        selected = currentRoute == item.route,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                item.icon,
                                contentDescription = item.title,
                                tint = if (currentRoute == item.route) Color(0xFFFF9800) else Color.Gray
                            )
                        },
                        label = {
                            Text(
                                item.title,
                                color = if (currentRoute == item.route) Color(0xFFFF9800) else Color.Gray
                            )
                        }
                    )
                }
            }
        }
    ) { innerPadding ->

        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                Home()
            }

            composable("mapa") {
                Mapa()
            }

            composable("carreras") {
                Carreras(
                    onCrearCarrera = {
                        navController.navigate("crear_carrera")
                    },
                    onAbrirLobby = { sessionId ->
                        navController.navigate("lobby_carrera/$sessionId")
                    }
                )
            }

            composable("lobby_carrera/{sessionId}") { backStackEntry ->
                val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""

                LobbyCarreraScreen(
                    sessionId = sessionId,
                    onBack = {
                        navController.popBackStack()
                    },
                    onRaceStarted = { id ->
                        navController.navigate("carrera_activa/$id") {
                            popUpTo("lobby_carrera/$id") {
                                inclusive = true
                            }
                        }
                    }
                )
            }

            composable("carrera_activa/{sessionId}") { backStackEntry ->
                val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""

                CarreraActivaScreen(
                    sessionId = sessionId,
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable("crear_carrera") {
                CrearCarrera(
                    onCerrar = {
                        navController.popBackStack()
                    },
                    onCarreraCreada = {
                        navController.popBackStack()
                    }
                )
            }

            composable("chat") {
                Chat()
            }

            composable("perfil") {
                Perfil(
                    user = firebaseUser,
                    userData = userData,
                    onLogout = onLogout,
                    onAccountDeleted = onAccountDeleted,
                    onEditProfile = {
                        navController.navigate("edit_profile")
                    },
                    onVerAmigos = {
                        navController.navigate("lista_amigos")
                    },


                    onRefresh = {
                        scope.launch {
                            firebaseUser?.uid?.let { uid ->
                                userData = userRepository.getUser(uid)
                            }
                        }
                    }
                )
            }
            composable("lista_amigos") {
                ListaAmigos(
                    onCerrar = { navController.popBackStack() },
                    onBuscarAmigos = { navController.navigate("buscar_amigos") },
                    onVerSolicitudes = { navController.navigate("solicitudes") }
                )
            }

            composable("edit_profile") {
                val currentUserData = userData

                if (currentUserData != null) {
                    EditProfileScreen(
                        userData = currentUserData,
                        onSave = { updatedUser ->
                            userData = updatedUser
                            navController.popBackStack()
                        },
                        onCancel = {
                            navController.popBackStack()
                        }
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No se encontraron datos del perfil en Firestore",
                            color = Color.White
                        )
                    }
                }
            }


            composable("buscar_amigos") {
                BuscarAmigosScreen(onBack = { navController.popBackStack() })
            }

            composable("solicitudes") {
                SolicitudesScreen(onBack = { navController.popBackStack() })
            }


        }
    }
}

data class NavItem(
    val route: String,
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)