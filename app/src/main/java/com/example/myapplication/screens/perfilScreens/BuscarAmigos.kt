package com.example.myapplication.screens.perfilScreens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.model.BuscarAmigosViewModel
import com.example.myapplication.repository.FriendsRepository
import com.example.myapplication.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun BuscarAmigos(
    onBack: () -> Unit,
    viewModel: BuscarAmigosViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val friendsRepository = remember { FriendsRepository() }
    val userRepository = remember { UserRepository() }

    val state by viewModel.buscarAmigosState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.updateIsLoadingUser(true)
        viewModel.updateCurrentUser(userRepository.getCurrentUser())
        viewModel.updateIsLoadingUser(false)
    }

    fun searchUser() {
        val query = state.searchQuery.trim().lowercase()
        if (query.isBlank()) return

        if (state.isLoadingUser) {
            viewModel.updateErrorMessage("Espere un momento...")
            return
        }

        scope.launch {
            viewModel.updateIsSearching(true)
            viewModel.updateErrorMessage(null)
            viewModel.updateSearchResult(null)
            viewModel.updateFriendStatus(null)

            val user = friendsRepository.searchUserByGameId(query)

            when {
                user == null -> {
                    viewModel.updateErrorMessage("No se encontró ningún usuario con Game ID: $query")
                }
                user.uid == state.currentUser?.uid -> {
                    viewModel.updateErrorMessage("Ese es tu propio Game ID")
                }
                else -> {
                    viewModel.updateSearchResult(user)
                    viewModel.updateFriendStatus(friendsRepository.getFriendshipStatus(user.uid))
                }
            }
            viewModel.updateIsSearching(false)
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1A1A1A))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, null, tint = Color.White)
            }
            Text(
                "Buscar amigos",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Buscar por Game ID", color = Color.Gray, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextField(
                        value = state.searchQuery,
                        onValueChange = {
                            viewModel.updateSearchQuery(it)
                            if (state.searchResult != null || state.errorMessage != null) {
                                viewModel.updateSearchResult(null)
                                viewModel.updateErrorMessage(null)
                                viewModel.updateFriendStatus(null)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Ej: linkup#1234", color = Color.Gray) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF252525),
                            unfocusedContainerColor = Color(0xFF252525),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color(0xFFFF9800),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Button(
                        onClick = { searchUser() },
                        enabled = !state.isSearching && state.searchQuery.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (state.isSearching) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Search, null, tint = Color.White)
                        }
                    }
                }
            }
        }

        if (state.errorMessage != null) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0x33FF4444)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    state.errorMessage!!,
                    color = Color(0xFFFF6B6B),
                    modifier = Modifier.padding(12.dp),
                    fontSize = 14.sp
                )
            }
        }

        state.searchResult?.let { user ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(Color(0xFFFF9800), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            user.displayName.take(2).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            user.displayName,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(user.gameId, color = Color.Gray, fontSize = 12.sp)
                        Text(
                            "${user.totalPoints} pts · ${user.totalPlacesVisited} lugares",
                            color = Color.Gray.copy(alpha = 0.6f),
                            fontSize = 11.sp
                        )
                    }

                    when (state.friendStatus) {
                        "accepted" -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Check,
                                    null,
                                    tint = Color(0xFF22C55E),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Amigos", color = Color(0xFF22C55E), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                        "pending" -> {
                            Text(
                                "Solicitud\nenviada",
                                color = Color(0xFFE9C46A),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )
                        }
                        "blocked" -> {
                            Text("Bloqueado", color = Color(0xFFEF4444), fontSize = 13.sp)
                        }
                        null -> {
                            Button(
                                onClick = {
                                    scope.launch {
                                        viewModel.updateIsSending(true)
                                        viewModel.updateErrorMessage(null)
                                        friendsRepository.sendFriendRequest(user.uid).fold(
                                            onSuccess = {
                                                viewModel.updateFriendStatus("Pendiente")
                                            },
                                            onFailure = { e ->
                                                viewModel.updateErrorMessage(e.message)
                                            }
                                        )
                                        viewModel.updateIsSending(false)
                                    }
                                },
                                enabled = !state.isSending,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                if (state.isSending) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(Icons.Default.PersonAdd, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Agregar", fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Tu Game ID", color = Color.Gray, fontSize = 12.sp)

                    TextButton(
                        onClick = {
                            scope.launch {
                                viewModel.updateIsLoadingUser(true)
                                viewModel.updateCurrentUser(userRepository.getCurrentUser())
                                viewModel.updateIsLoadingUser(false)
                                if (state.currentUser?.gameId.isNullOrEmpty()) {
                                    viewModel.updateErrorMessage("⚠️ Game ID no encontrado. Reintentando...")
                                    val firebaseUser = FirebaseAuth.getInstance().currentUser
                                    if (firebaseUser != null && state.currentUser != null) {
                                        val newGameId = "linkup#${(1000..9999).random()}"
                                        val updatedUser = state.currentUser!!.copy(gameId = newGameId)
                                        userRepository.updateUser(updatedUser)
                                        viewModel.updateCurrentUser(updatedUser)
                                    }
                                }
                            }
                        }
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            null,
                            tint = Color(0xFFFF9800),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Recargar", color = Color(0xFFFF9800), fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                if (state.isLoadingUser) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color(0xFFFF9800),
                        strokeWidth = 2.dp
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            state.currentUser?.gameId?.takeIf { it.isNotEmpty() } ?: "No disponible",
                            color = if (state.currentUser?.gameId.isNullOrEmpty()) Color.Red else Color(0xFFFF9800),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )

                        if (!state.currentUser?.gameId.isNullOrEmpty()) {
                            val clipboardManager = LocalContext.current.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            IconButton(
                                onClick = {
                                    val clip = ClipData.newPlainText("Game ID", state.currentUser!!.gameId)
                                    clipboardManager.setPrimaryClip(clip)
                                    Toast.makeText(
                                        context,
                                        "Game ID copiado",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            ) {
                                Icon(
                                    Icons.Default.ContentCopy,
                                    null,
                                    tint = Color(0xFFFF9800),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
                Text(
                    "Comparte este código con tus amigos",
                    color = Color.Gray,
                    fontSize = 12.sp
                )

                if (!state.isLoadingUser && state.currentUser?.gameId.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "⚠️ Error: No se encontró tu Game ID. Toca 'Recargar' para generarlo.",
                        color = Color(0xFFFF6B6B),
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}