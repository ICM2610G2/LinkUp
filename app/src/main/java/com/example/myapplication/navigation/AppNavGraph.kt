package com.example.myapplication.navigation

import android.content.ContextWrapper
import androidx.activity.compose.LocalActivity
import android.app.Activity
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myapplication.auth.BiometricAuthManager
import com.example.myapplication.auth.EncryptedPreferences
import com.example.myapplication.auth.FirebaseAuthManager
import com.example.myapplication.data.models.User
import com.example.myapplication.repository.UserRepository
import com.example.myapplication.screens.*
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

    // Estado para userData desde Firestore
    var userData by remember { mutableStateOf<User?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // Cargar userData cuando cambia el usuario
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
            val context = LocalContext.current
            val fragmentActivity = remember(context) {
                var currentContext = context
                while (currentContext is ContextWrapper) {
                    if (currentContext is FragmentActivity) break
                    currentContext = currentContext.baseContext
                }
                currentContext as FragmentActivity
            }

            val dummyAuthManager = remember(fragmentActivity) {
                FirebaseAuthManager(fragmentActivity)
            }
            val dummyBiometricManager = remember(fragmentActivity) {
                BiometricAuthManager(fragmentActivity)
            }
            val dummyEncryptedPrefs = remember(context) {
                EncryptedPreferences(context)
            }

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
            Home()
        }

        composable(Screen.Mapa.route) {
            Mapa()
        }

        composable(Screen.Carreras.route) {
            Carreras()
        }

        composable(Screen.Chat.route) {
            Chat()
        }

        composable(Screen.Perfil.route) {
            if (isLoading) {
                // Mostrar loading mientras carga
                androidx.compose.material3.CircularProgressIndicator(
                    modifier = Modifier,
                    color = androidx.compose.ui.graphics.Color(0xFFFF9800)
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
                    onCancel = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
