package com.example.myapplication.screens

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
import com.example.myapplication.data.models.User
import com.example.myapplication.repository.FriendsRepository
import com.example.myapplication.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

@Composable
fun BuscarAmigosScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val friendsRepository = remember { FriendsRepository() }
    val userRepository = remember { UserRepository() }
    val firestore = remember { FirebaseFirestore.getInstance() }

    var searchQuery by remember { mutableStateOf("") }
    var searchResult by remember { mutableStateOf<User?>(null) }
    var isLoadingUser by remember { mutableStateOf(true) }
    var isSearching by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var friendStatus by remember { mutableStateOf<String?>(null) }
    var isSending by remember { mutableStateOf(false) }
    var currentUser by remember { mutableStateOf<User?>(null) }

    // Cargar usuario actual
    LaunchedEffect(Unit) {
        isLoadingUser = true
        currentUser = userRepository.getCurrentUser()
        isLoadingUser = false
    }

    fun searchUser() {
        val query = searchQuery.trim().lowercase()
        if (query.isBlank()) return

        if (isLoadingUser) {
            errorMessage = "Espera un momento..."
            return
        }

        scope.launch {
            isSearching = true
            errorMessage = null
            searchResult = null
            friendStatus = null

            val user = friendsRepository.searchUserByGameId(query)

            when {
                user == null -> {
                    errorMessage = "No se encontró ningún usuario con Game ID: $query"
                }
                user.uid == currentUser?.uid -> {
                    errorMessage = "Ese es tu propio Game ID 😅"
                }
                else -> {
                    searchResult = user
                    friendStatus = friendsRepository.getFriendshipStatus(user.uid)
                }
            }
            isSearching = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
    ) {
        // Header
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

        // Buscador
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
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            if (searchResult != null || errorMessage != null) {
                                searchResult = null
                                errorMessage = null
                                friendStatus = null
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
                        enabled = !isSearching && searchQuery.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isSearching) {
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

        // Error
        if (errorMessage != null) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0x33FF4444)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    errorMessage!!,
                    color = Color(0xFFFF6B6B),
                    modifier = Modifier.padding(12.dp),
                    fontSize = 14.sp
                )
            }
        }

        // Resultado
        searchResult?.let { user ->
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

                    when (friendStatus) {
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
                                        isSending = true
                                        errorMessage = null
                                        friendsRepository.sendFriendRequest(user.uid).fold(
                                            onSuccess = {
                                                friendStatus = "pending"
                                            },
                                            onFailure = { e ->
                                                errorMessage = e.message
                                            }
                                        )
                                        isSending = false
                                    }
                                },
                                enabled = !isSending,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                if (isSending) {
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

        // Tu Game ID
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

                    // Botón para forzar recarga - CORREGIDO
                    TextButton(
                        onClick = {
                            scope.launch {
                                isLoadingUser = true
                                currentUser = userRepository.getCurrentUser()
                                isLoadingUser = false
                                if (currentUser?.gameId.isNullOrEmpty()) {
                                    errorMessage = "⚠️ Game ID no encontrado. Reintentando..."
                                    val firebaseUser = FirebaseAuth.getInstance().currentUser
                                    if (firebaseUser != null && currentUser != null) {
                                        val newGameId = "linkup#${(1000..9999).random()}"
                                        val updatedUser = currentUser!!.copy(gameId = newGameId)
                                        userRepository.updateUser(updatedUser)
                                        currentUser = updatedUser
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

                if (isLoadingUser) {
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
                            currentUser?.gameId?.takeIf { it.isNotEmpty() } ?: "No disponible",
                            color = if (currentUser?.gameId.isNullOrEmpty()) Color.Red else Color(0xFFFF9800),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )

                        if (!currentUser?.gameId.isNullOrEmpty()) {
                            val clipboardManager = LocalContext.current.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            IconButton(
                                onClick = {
                                    val clip = ClipData.newPlainText("Game ID", currentUser!!.gameId)
                                    clipboardManager.setPrimaryClip(clip)
                                    // ✅ CORREGIDO: Usar Snackbar o Toast de manera correcta
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

                if (!isLoadingUser && currentUser?.gameId.isNullOrEmpty()) {
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