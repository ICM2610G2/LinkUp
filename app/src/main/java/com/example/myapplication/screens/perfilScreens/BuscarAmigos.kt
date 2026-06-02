package com.example.myapplication.screens.perfilScreens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.myapplication.model.BuscarAmigosViewModel
import com.example.myapplication.repository.FriendsRepository
import com.example.myapplication.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
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

    // Timer para el código temporal
    LaunchedEffect(state.generatedCode) {
        if (state.generatedCode != null) {
            var seconds = 15 * 60
            while (seconds > 0) {
                val mins = seconds / 60
                val secs = seconds % 60
                viewModel.updateGeneratedCode(state.generatedCode, String.format("%02d:%02d", mins, secs))
                delay(1000)
                seconds--
            }
            viewModel.updateGeneratedCode(null, null)
        }
    }

    fun shareFriendCode(code: String) {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(
                Intent.EXTRA_TEXT,
                "Mi código de amistad en LinkUp es: $code\n\nAbre LinkUp y escribe este código en la sección Agregar Amigo.\n\nEste código expira en 15 minutos."
            )
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(shareIntent, "Compartir código de amistad"))
    }

    fun searchUser() {
        val query = state.searchQuery.trim().uppercase()
        if (query.isBlank()) return

        scope.launch {
            viewModel.updateIsSearching(true)
            viewModel.updateErrorMessage(null)
            viewModel.updateSearchResult(null)
            viewModel.updateFriendStatus(null)

            friendsRepository.getUserByInviteCode(query).fold(
                onSuccess = { user ->
                    if (user.uid == state.currentUser?.uid) {
                        viewModel.updateErrorMessage("No puedes agregarte a ti mismo")
                    } else {
                        viewModel.updateSearchResult(user)
                        viewModel.updateFriendStatus(friendsRepository.getFriendshipStatus(user.uid))
                    }
                },
                onFailure = { e ->
                    viewModel.updateErrorMessage(e.message ?: "Error al buscar código")
                }
            )
            viewModel.updateIsSearching(false)
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
                "Invitar Amigos",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECCIÓN 1: MI INVITACIÓN (CÓDIGO TEMPORAL)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "MI INVITACIÓN",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (state.generatedCode == null) {
                    Button(
                        onClick = {
                            scope.launch {
                                viewModel.updateIsGeneratingCode(true)
                                try {
                                    val code = friendsRepository.generateFriendInvite()
                                    viewModel.updateGeneratedCode(code, "15:00")
                                } catch (e: Exception) {
                                    viewModel.updateErrorMessage("Error al generar código")
                                }
                                viewModel.updateIsGeneratingCode(false)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !state.isGeneratingCode
                    ) {
                        if (state.isGeneratingCode) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                        } else {
                            Text("Generar Código Temporal")
                        }
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(
                                state.generatedCode!!,
                                color = Color(0xFFFF9800),
                                fontSize = 32.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 2.sp
                            )
                            Text(
                                "Expira en: ${state.remainingTime}",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }

                        Row {
                            IconButton(
                                onClick = {
                                    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Friend Code", state.generatedCode)
                                    clipboardManager.setPrimaryClip(clip)
                                    Toast.makeText(context, "Código copiado", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(Icons.Default.ContentCopy, "Copiar", tint = Color.White)
                            }
                            IconButton(
                                onClick = { shareFriendCode(state.generatedCode!!) }
                            ) {
                                Icon(Icons.Default.Share, "Compartir", tint = Color.White)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // SECCIÓN 2: AGREGAR AMIGO
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "AGREGAR AMIGO",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextField(
                        value = state.searchQuery,
                        onValueChange = {
                            if (it.length <= 8) {
                                viewModel.updateSearchQuery(it.uppercase())
                            }
                        },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Introduce el código", color = Color.Gray) },
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
                        enabled = !state.isSearching && state.searchQuery.length == 8,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (state.isSearching) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                        } else {
                            Text("Buscar")
                        }
                    }
                }
            }
        }

        // Mensaje de error
        if (state.errorMessage != null) {
            Text(
                state.errorMessage!!,
                color = Color(0xFFFF6B6B),
                modifier = Modifier.padding(16.dp),
                fontSize = 14.sp
            )
        }

        // Resultado de búsqueda
        state.searchResult?.let { user ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                border = BorderStroke(1.dp, Color(0x33FFFFFF))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Avatar con foto real o iniciales
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFF9800)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (user.photoURL.isNotEmpty()) {
                            AsyncImage(
                                model = user.photoURL,
                                contentDescription = "Foto de ${user.displayName}",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                user.displayName.take(2).uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(user.displayName, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        Text(user.gameId, color = Color.Gray, fontSize = 12.sp)
                    }

                    when (state.friendStatus) {
                        "accepted" -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Check, null, tint = Color(0xFF22C55E), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Amigos", color = Color(0xFF22C55E), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                        "pending" -> {
                            Text(
                                "Solicitud\nenviada",
                                color = Color(0xFFE9C46A),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                        "blocked" -> {
                            Text("Bloqueado", color = Color(0xFFEF4444), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                        else -> {
                            Button(
                                onClick = {
                                    scope.launch {
                                        viewModel.updateIsSending(true)
                                        viewModel.updateErrorMessage(null)
                                        friendsRepository.sendFriendRequest(user.uid).fold(
                                            onSuccess = {
                                                viewModel.updateFriendStatus("pending")
                                                Toast.makeText(context, "Solicitud enviada", Toast.LENGTH_SHORT).show()
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
                                Text("Agregar")
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
                        Icon(Icons.Default.Refresh, null, tint = Color(0xFFFF9800), modifier = Modifier.size(16.dp))
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
                                    Toast.makeText(context, "Game ID copiado", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(Icons.Default.ContentCopy, null, tint = Color(0xFFFF9800), modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
                Text("Comparte este código con tus amigos", color = Color.Gray, fontSize = 12.sp)
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
