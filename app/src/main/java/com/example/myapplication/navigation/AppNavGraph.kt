package com.example.myapplication.navigation

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.auth.BiometricAuthManager
import com.example.myapplication.auth.EncryptedPreferences
import com.example.myapplication.auth.FirebaseAuthManager
import com.example.myapplication.data.models.User
import com.example.myapplication.repository.UserRepository
import com.example.myapplication.screens.carreraScreens.CarreraActivaScreen
import com.example.myapplication.screens.carreraScreens.CarreraScreen
import com.example.myapplication.screens.carreraScreens.Carreras
import com.example.myapplication.screens.grupoScreens.Chat
import com.example.myapplication.screens.homeScreens.CrearCarrera
import com.example.myapplication.screens.perfilScreens.EditProfileScreen
import com.example.myapplication.screens.homeScreens.Home
import com.example.myapplication.screens.LobbyCarreraScreen
import com.example.myapplication.screens.Login
import com.example.myapplication.screens.mapaScreens.Mapa
import com.example.myapplication.screens.perfilScreens.Perfil
import com.example.myapplication.screens.perfilScreens.ConfiguracionScreen
import com.example.myapplication.screens.perfilScreens.HistorialCarrerasScreen
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Mapa : Screen("mapa")
    object Carreras : Screen("carreras")
    object Chat : Screen("chat")
    object Perfil : Screen("perfil")
    object Login : Screen("login")
    object EditProfile : Screen("edit_profile")
    object EscanearQR : Screen("escanear_qr")
}

@Composable
fun AppNavGraph(
    modifier: Modifier = Modifier,
    isLoggedIn: Boolean,
    user: FirebaseUser? = null,
    onLogout: () -> Unit,
    onAccountDeleted: () -> Unit
) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val userRepository = remember { UserRepository() }

    var userData by remember { mutableStateOf<User?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(user) {
        isLoading = true
        userData = if (user != null) {
            userRepository.getUser(user.uid)
        } else {
            null
        }
        isLoading = false
    }

    NavHost(
        navController = navController,
        startDestination = if (isLoggedIn) Screen.Home.route else Screen.Login.route,
        modifier = modifier
    ) {
        composable(Screen.Login.route) {
            val dummyAuthManager = FirebaseAuthManager(
                LocalContext.current as androidx.appcompat.app.AppCompatActivity
            )
            val dummyBiometricManager = BiometricAuthManager(
                LocalContext.current as androidx.appcompat.app.AppCompatActivity
            )
            val dummyEncryptedPrefs = EncryptedPreferences(
                LocalContext.current
            )
            Login(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                authManager = dummyAuthManager,
                biometricManager = dummyBiometricManager,
                encryptedPrefs = dummyEncryptedPrefs
            )
        }

        composable(Screen.Home.route) {
            Home(
                onVerCarrera = { raceId ->
                    navController.navigate("carrera_detail/$raceId")
                },
                onAbrirLobby = { sessionId ->
                    navController.navigate("lobby_carrera/$sessionId")
                },
                onEscanearQR = { navController.navigate(Screen.EscanearQR.route) }
            )
        }

        composable(Screen.Mapa.route) {
            Mapa()
        }

        composable(Screen.Carreras.route) {
            Carreras(
                onCrearCarrera = {
                    navController.navigate("crear_carrera")
                },
                onVerCarrera = { raceId ->
                    navController.navigate("carrera_detail/$raceId")
                },
                onAbrirLobby = { sessionId ->
                    navController.navigate("lobby_carrera/$sessionId")
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

        composable("crear_carrera") {
            CrearCarrera(
                onCerrar = { navController.popBackStack() },
                onCarreraCreada = { navController.popBackStack() }
            )
        }

        composable("lobby_carrera/{sessionId}") { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
            LobbyCarreraScreen(
                sessionId = sessionId,
                onBack = { navController.popBackStack() },
                onRaceStarted = { id ->
                    navController.navigate("carrera_activa/$id")
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

        composable(Screen.Chat.route) {
            Chat()
        }

        composable(Screen.Perfil.route) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier,
                    color = Color(0xFFFF9800)
                )
            } else {
                Perfil(
                    user = user,
                    userData = userData,
                    onLogout = {
                        scope.launch {
                            onLogout()
                            navController.navigate(Screen.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    },
                    onAccountDeleted = {
                        scope.launch {
                            onAccountDeleted()
                            navController.navigate(Screen.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    },
                    onEditProfile = {
                        navController.navigate(Screen.EditProfile.route)
                    },
                    onVerAmigos = {
                        navController.navigate("lista_amigos")
                    },
                    onConfiguracion = {
                        navController.navigate("configuracion")
                    },
                    onHistorialCarreras = {
                        navController.navigate("historial_carreras")
                    },
                    onRefresh = {
                        scope.launch {
                            userData = user?.let { userRepository.getUser(it.uid) }
                        }
                    }
                )
            }
        }

        composable(Screen.EditProfile.route) {
            if (userData != null) {
                EditProfileScreen(
                    userData = userData!!,
                    onSave = { updatedUser ->
                        userData = updatedUser
                        navController.popBackStack()
                    },
                    onCancel = { navController.popBackStack() }
                )
            }
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
    }
}