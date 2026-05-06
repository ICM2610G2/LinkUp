package com.example.myapplication.ui.theme.screens

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.models.User
import com.example.myapplication.repository.FriendsRepository
import com.example.myapplication.repository.UserRepository
import kotlinx.coroutines.launch

@Composable
fun ListaAmigos(
    onCerrar: () -> Unit,
    onBuscarAmigos: () -> Unit = {},
    onVerSolicitudes: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val friendsRepository = remember { FriendsRepository() }
    val userRepository = remember { UserRepository() }

    var amigos by remember { mutableStateOf<List<User>>(emptyList()) }
    var solicitudes by remember { mutableStateOf<List<SolicitudConUsuario>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var tabActiva by remember { mutableStateOf("todos") }

    suspend fun loadAll() {
        isLoading = true

        amigos = friendsRepository.getAcceptedFriends()

        val pending = friendsRepository.getPendingRequests()
        solicitudes = pending.mapNotNull { friendship ->
            val user = userRepository.getUser(friendship.userA)
            if (user != null) SolicitudConUsuario(friendship, user) else null
        }

        isLoading = false
    }

    LaunchedEffect(Unit) {
        loadAll()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCC000000)),
        contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF252525))
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Amigos",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(onClick = onBuscarAmigos) {
                            Icon(Icons.Default.Search, null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        // Badge en solicitudes
                        Box {
                            IconButton(onClick = onVerSolicitudes) {
                                Icon(Icons.Default.PersonAdd, null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                            if (solicitudes.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .background(Color(0xFFFF9800), CircleShape)
                                        .align(Alignment.TopEnd),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        solicitudes.size.toString(),
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        IconButton(onClick = onCerrar) {
                            Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                // Tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TabAmigos(
                        label = "Amigos (${amigos.size})",
                        activa = tabActiva == "todos",
                        color = Color(0xFF2A9D8F),
                        textColor = Color.White,
                        modifier = Modifier.weight(1f),
                        onClick = { tabActiva = "todos" }
                    )
                    TabAmigos(
                        label = "Solicitudes (${solicitudes.size})",
                        activa = tabActiva == "solicitudes",
                        color = Color(0xFFE9C46A),
                        textColor = Color.Black,
                        modifier = Modifier.weight(1f),
                        onClick = { tabActiva = "solicitudes" }
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color(0x1AFFFFFF))
                )

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFFFF9800))
                    }
                } else {
                    when (tabActiva) {
                        "todos" -> {
                            if (amigos.isEmpty()) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        Icons.Default.PersonOutline,
                                        null,
                                        tint = Color.Gray,
                                        modifier = Modifier.size(64.dp)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text("No tienes amigos aún", color = Color.Gray)
                                    Text(
                                        "Busca amigos por Game ID",
                                        color = Color.Gray.copy(alpha = 0.6f),
                                        fontSize = 12.sp
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = onBuscarAmigos,
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Default.Search, null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Buscar amigos")
                                    }
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(amigos, key = { it.uid }) { amigo ->
                                        AmigoItem(
                                            amigo = amigo,
                                            onRemove = {
                                                scope.launch {
                                                    friendsRepository.removeFriend(amigo.uid)
                                                    amigos = friendsRepository.getAcceptedFriends()
                                                }
                                            },
                                            onBlock = {
                                                scope.launch {
                                                    friendsRepository.blockUser(amigo.uid)
                                                    amigos = friendsRepository.getAcceptedFriends()
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        "solicitudes" -> {
                            if (solicitudes.isEmpty()) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        Icons.Default.PersonAdd,
                                        null,
                                        tint = Color.Gray,
                                        modifier = Modifier.size(64.dp)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text("No tienes solicitudes pendientes", color = Color.Gray)
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(solicitudes, key = { it.friendship.id }) { item ->
                                        SolicitudCard(
                                            item = item,
                                            onAccept = {
                                                scope.launch {
                                                    friendsRepository.acceptRequest(item.friendship.id)
                                                    loadAll()
                                                }
                                            },
                                            onReject = {
                                                scope.launch {
                                                    friendsRepository.rejectRequest(item.friendship.id)
                                                    loadAll()
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AmigoItem(
    amigo: User,
    onRemove: () -> Unit,
    onBlock: () -> Unit
) {
    var mostrarMenu by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF252525)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFFFF9800), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    amigo.displayName.take(2).uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    amigo.displayName,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(amigo.gameId, color = Color.Gray, fontSize = 12.sp)
                Text(
                    "${amigo.totalPoints} pts",
                    color = Color(0xFFFF9800).copy(alpha = 0.8f),
                    fontSize = 11.sp
                )
            }

            // Menú de opciones
            Box {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0x0DFFFFFF), RoundedCornerShape(8.dp))
                        .clickable { mostrarMenu = !mostrarMenu },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.MoreVert,
                        null,
                        tint = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }

                DropdownMenu(
                    expanded = mostrarMenu,
                    onDismissRequest = { mostrarMenu = false },
                    modifier = Modifier.background(Color(0xFF1A1A1A))
                ) {
                    DropdownMenuItem(
                        text = { Text("Eliminar amigo", color = Color(0xFFEF4444), fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.PersonRemove, null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                        },
                        onClick = {
                            mostrarMenu = false
                            onRemove()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Bloquear", color = Color(0xFFEF4444), fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Block, null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                        },
                        onClick = {
                            mostrarMenu = false
                            onBlock()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Reportar", color = Color(0xFFE9C46A), fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Flag, null, tint = Color(0xFFE9C46A), modifier = Modifier.size(16.dp))
                        },
                        onClick = { mostrarMenu = false }
                    )
                }
            }
        }
    }
}

@Composable
fun TabAmigos(
    label: String,
    activa: Boolean,
    color: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .background(
                if (activa) color else Color(0xFF252525),
                RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            color = if (activa) textColor else Color.White.copy(alpha = 0.6f),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}