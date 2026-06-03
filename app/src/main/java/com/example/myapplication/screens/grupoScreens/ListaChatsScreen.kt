package com.example.myapplication.screens.grupoScreens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.myapplication.model.ListaChatsViewModel
import com.example.myapplication.repository.UserRepository
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ListaChatsScreen(
    onChatClick: (chatId: String, chatName: String, isGroup: Boolean, isReadOnly: Boolean) -> Unit,
    viewModel: ListaChatsViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val userRepository = remember { UserRepository() }

    // Mapa para almacenar los datos de los usuarios (nombre y foto)
    var usersMap by remember { mutableStateOf<Map<String, Pair<String, String>>>(emptyMap()) }
    var isLoadingUsers by remember { mutableStateOf(true) }

    // Cargar datos de los usuarios para los chats directos
    LaunchedEffect(state.chats) {
        isLoadingUsers = true
        val directChats = state.chats.filter { it.type == "direct" }
        val userIds = directChats.flatMap { it.participants }.distinct()
        val tempMap = mutableMapOf<String, Pair<String, String>>()

        for (userId in userIds) {
            if (userId != state.currentUserId) {
                val user = userRepository.getUser(userId)
                if (user != null) {
                    tempMap[userId] = Pair(user.displayName, user.photoURL)
                }
            }
        }
        usersMap = tempMap
        isLoadingUsers = false
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
            Text(
                "Chats",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
        }

        Divider(color = Color(0x22FFFFFF))

        when {
            state.isLoading || isLoadingUsers -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFFF9800))
                }
            }
            state.chats.isEmpty() -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.Chat, null, tint = Color.Gray, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No tienes conversaciones", color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Agrega amigos para empezar a chatear", color = Color.Gray.copy(alpha = 0.6f), fontSize = 12.sp)
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Chats grupales activos
                    val activeGroupChats = state.chats.filter {
                        it.type == "group" && it.readOnly == false
                    }

                    if (activeGroupChats.isNotEmpty()) {
                        item {
                            Text(
                                "CARRERA ACTIVA",
                                color = Color(0xFFFF9800),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                            )
                        }
                        items(activeGroupChats) { chat ->
                            ChatItemSimple(
                                chatName = "Carrera ${chat.sessionId.take(6)}",
                                photoURL = "",
                                lastMessage = chat.lastMessage,
                                time = chat.lastMessageAt,
                                isGroup = true,
                                onClick = {
                                    onChatClick(chat.id, "Carrera en curso", true, chat.readOnly)
                                }
                            )
                        }
                    }

                    // Chats 1-a-1
                    val directChats = state.chats.filter { it.type == "direct" }
                    if (directChats.isNotEmpty()) {
                        if (activeGroupChats.isNotEmpty()) {
                            item { Spacer(modifier = Modifier.height(8.dp)) }
                            item {
                                Text(
                                    "CONVERSACIONES",
                                    color = Color.Gray,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                                )
                            }
                        }
                        items(directChats) { chat ->
                            val otherUserId = chat.participants.firstOrNull { it != state.currentUserId }
                            val userData = otherUserId?.let { usersMap[it] }
                            val chatName = userData?.first ?: "Usuario"
                            val photoURL = userData?.second ?: ""

                            ChatItemSimple(
                                chatName = chatName,
                                photoURL = photoURL,
                                lastMessage = chat.lastMessage,
                                time = chat.lastMessageAt,
                                isGroup = false,
                                onClick = {
                                    onChatClick(chat.id, chatName, false, false)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatItemSimple(
    chatName: String,
    photoURL: String,
    lastMessage: String,
    time: com.google.firebase.Timestamp,
    isGroup: Boolean,
    onClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val timeStr = try {
        dateFormat.format(time.toDate())
    } catch (e: Exception) {
        "--:--"
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Avatar con foto real si existe
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (isGroup) Color(0xFFFF9800) else Color(0xFF2A9D8F)),
                contentAlignment = Alignment.Center
            ) {
                if (photoURL.isNotEmpty() && !isGroup) {
                    AsyncImage(
                        model = photoURL,
                        contentDescription = "Foto de perfil",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else if (isGroup) {
                    Icon(Icons.Default.Group, null, tint = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        chatName.take(2).uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            // Información del chat
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        chatName,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                    Text(
                        timeStr,
                        color = Color.Gray,
                        fontSize = 10.sp
                    )
                }
                Text(
                    lastMessage.ifEmpty { "Nuevo chat" },
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    maxLines = 1
                )
            }
        }
    }
}