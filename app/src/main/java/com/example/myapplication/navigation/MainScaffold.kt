package com.example.myapplication.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
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
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.myapplication.screens.perfilScreens.BuscarAmigos
import com.example.myapplication.screens.carreraScreens.CarreraActivaScreen
import com.example.myapplication.screens.carreraScreens.Carreras
import com.example.myapplication.screens.grupoScreens.Chat
import com.example.myapplication.screens.homeScreens.CrearCarrera
import com.example.myapplication.screens.perfilScreens.EditProfileScreen
import com.example.myapplication.screens.homeScreens.EscanearQR
import com.example.myapplication.screens.homeScreens.InvitarNFC
import com.example.myapplication.screens.homeScreens.Home
import com.example.myapplication.screens.perfilScreens.ListaAmigos
import com.example.myapplication.screens.LobbyCarreraScreen
import com.example.myapplication.screens.mapaScreens.Mapa
import com.example.myapplication.screens.perfilScreens.Perfil
import com.example.myapplication.screens.perfilScreens.ConfiguracionScreen
import com.example.myapplication.screens.perfilScreens.HistorialCarrerasScreen
import com.example.myapplication.screens.Solicitudes
import com.example.myapplication.screens.carreraScreens.CarreraScreen
import com.example.myapplication.screens.grupoScreens.ListaChatsScreen
import com.example.myapplication.screens.grupoScreens.ChatDetailScreen



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
                    NavItem("home", "Inicio", Icons.Default.Home),
                    NavItem("mapa", "Mapa", Icons.Default.Map),
                    NavItem("carreras", "Carreras", Icons.Default.EmojiEvents),
                    NavItem("lista_chats", "Chat", Icons.AutoMirrored.Filled.Chat),
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
                Home(
                    onEscanearQR = { navController.navigate(Screen.EscanearQR.route) },
                    onVerCarrera = { raceId ->
                        navController.navigate("carrera_detail/$raceId")
                    },
                    onAbrirLobby = { sessionId ->
                        navController.navigate("lobby_carrera/$sessionId")
                    }
                )
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
                    },
                    onVerCarrera = { raceId ->
                        navController.navigate("carrera_detail/$raceId")
                    },
                )
            }

            composable("lobby_carrera/{sessionId}") { backStackEntry ->
                val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
                LobbyCarreraScreen(
                    sessionId = sessionId,
                    onBack = { navController.popBackStack() },
                    onRaceStarted = { id ->
                        navController.navigate("carrera_activa/$id") {
                            popUpTo("lobby_carrera/$id") { inclusive = true }
                        }
                    }
                )
            }

            composable("carrera_detail/{raceId}") { backStackEntry ->
                val raceId = backStackEntry.arguments?.getString("raceId") ?: ""
                CarreraScreen(
                    raceId = raceId,
                    onBack = { navController.popBackStack() },
                    onAbrirLobby = { sessionId ->
                        navController.navigate("lobby_carrera/$sessionId") {
                            popUpTo("carreras")
                        }
                    }
                )
            }

            composable("carrera_activa/{sessionId}") { backStackEntry ->
                val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
                CarreraActivaScreen(
                    sessionId = sessionId,
                    onBack = { navController.popBackStack() }
                )
            }

            composable("crear_carrera") {
                CrearCarrera(
                    onCerrar = { navController.popBackStack() },
                    onCarreraCreada = { navController.popBackStack() }
                )
            }

            composable("lista_chats") {
                ListaChatsScreen(
                    onChatClick = { chatId, chatName, isGroup, isReadOnly ->
                        navController.navigate("chat_detail/$chatId/$chatName/$isGroup/$isReadOnly")
                    }
                )
            }

            composable("chat_detail/{chatId}/{chatName}/{isGroup}/{isReadOnly}") { backStackEntry ->
                val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
                val chatName = backStackEntry.arguments?.getString("chatName") ?: ""
                val isGroup = backStackEntry.arguments?.getString("isGroup")?.toBoolean() ?: false
                val isReadOnly = backStackEntry.arguments?.getString("isReadOnly")?.toBoolean() ?: false
                ChatDetailScreen(
                    chatId = chatId,
                    chatName = chatName,
                    isGroup = isGroup,
                    isReadOnly = isReadOnly,
                    onBack = { navController.popBackStack() }
                )
            }

            composable("perfil") {
                Perfil(
                    user = firebaseUser,
                    userData = userData,
                    onLogout = onLogout,
                    onAccountDeleted = onAccountDeleted,
                    onEditProfile = { navController.navigate("edit_profile") },
                    onVerAmigos = { navController.navigate("lista_amigos") },
                    onConfiguracion = { navController.navigate("configuracion") },
                    onHistorialCarreras = { navController.navigate("historial_carreras") },
                    onRefresh = {
                        scope.launch {
                            firebaseUser?.uid?.let { uid ->
                                userData = userRepository.getUser(uid)
                            }
                        }
                    }
                )
            }

            composable("configuracion") {
                ConfiguracionScreen(
                    userData = userData,
                    onBack = { navController.popBackStack() }
                )
            }

            composable("historial_carreras") {
                HistorialCarrerasScreen(
                    onBack = { navController.popBackStack() }
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
                        onCancel = { navController.popBackStack() }
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No se encontraron datos del perfil en Firestore", color = Color.White)
                    }
                }
            }

            composable("invitar_nfc") {
                InvitarNFC(
                    onCerrar = { navController.popBackStack() },
                    onEscanearQR = { navController.navigate("escanear_qr") }
                )
            }

            composable("escanear_qr") {
                EscanearQR(
                    onBack = { navController.popBackStack() },
                    onInviteSent = { navController.popBackStack() }
                )
            }

            composable("buscar_amigos") {
                BuscarAmigos(onBack = { navController.popBackStack() })
            }

            composable("solicitudes") {
                Solicitudes(onBack = { navController.popBackStack() })
            }
        }
    }
}

data class NavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
)