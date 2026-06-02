package com.example.myapplication.screens.perfilScreens

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
import com.example.myapplication.data.models.User
import com.example.myapplication.model.ListaAmigosViewModel
import com.example.myapplication.screens.SolicitudCard

@Composable
fun ListaAmigos(
    onCerrar: () -> Unit,
    onBuscarAmigos: () -> Unit = {},
    onVerSolicitudes: () -> Unit = {},
    viewModel: ListaAmigosViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

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
                    Text("Amigos", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(onClick = onBuscarAmigos) {
                            Icon(Icons.Default.PersonAdd, null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Box {
                            IconButton(onClick = onVerSolicitudes) {
                                Icon(Icons.Default.Notifications, null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                            if (state.solicitudes.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .background(Color(0xFFFF9800), CircleShape)
                                        .align(Alignment.TopEnd),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        state.solicitudes.size.toString(),
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
                        label = "Amigos (${state.amigos.size})",
                        activa = state.tabActiva == "todos",
                        color = Color(0xFF2A9D8F),
                        textColor = Color.White,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.updateTabActiva("todos") }
                    )
                    TabAmigos(
                        label = "Solicitudes (${state.solicitudes.size})",
                        activa = state.tabActiva == "solicitudes",
                        color = Color(0xFFE9C46A),
                        textColor = Color.Black,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.updateTabActiva("solicitudes") }
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color(0x1AFFFFFF))
                )

                if (state.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFFFF9800))
                    }
                } else {
                    when (state.tabActiva) {
                        "todos" -> {
                            if (state.amigos.isEmpty()) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Default.PersonOutline, null, tint = Color.Gray, modifier = Modifier.size(64.dp))
                                    Icon(
                                        Icons.Default.Group,
                                        null,
                                        tint = Color.Gray,
                                        modifier = Modifier.size(64.dp)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text("No tienes amigos aún", color = Color.Gray)
                                    Text("Busca amigos por Game ID", color = Color.Gray.copy(alpha = 0.6f), fontSize = 12.sp)
                                    Text(
                                        "Añade amigos con un código",
                                        color = Color.Gray.copy(alpha = 0.6f),
                                        fontSize = 12.sp
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = onBuscarAmigos,
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Default.Add, null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Añadir amigos")
                                    }
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(state.amigos, key = { it.uid }) { amigo ->
                                        AmigoItem(
                                            amigo = amigo,
                                            onRemove = { viewModel.removeAmigo(amigo.uid) },
                                            onBlock = { viewModel.blockAmigo(amigo.uid) }
                                        )
                                    }
                                }
                            }
                        }
                        "solicitudes" -> {
                            if (state.solicitudes.isEmpty()) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Default.PersonAdd, null, tint = Color.Gray, modifier = Modifier.size(64.dp))
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text("No tienes solicitudes pendientes", color = Color.Gray)
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(state.solicitudes, key = { it.friendship.id }) { item ->
                                        SolicitudCard(
                                            item = item,
                                            onAccept = { viewModel.acceptRequest(item.friendship.id) },
                                            onReject = { viewModel.rejectRequest(item.friendship.id) }
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
            // Avatar con foto real o iniciales
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFF9800)),
                contentAlignment = Alignment.Center
            ) {
                if (amigo.photoURL.isNotEmpty()) {
                    AsyncImage(
                        model = amigo.photoURL,
                        contentDescription = "Foto de ${amigo.displayName}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        amigo.displayName.take(2).uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    amigo.displayName,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "${amigo.totalPoints} pts · ${amigo.gameId}",
                    color = Color(0xFFFF9800).copy(alpha = 0.8f),
                    fontSize = 11.sp
                )
                Text(amigo.displayName, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text(amigo.gameId, color = Color.Gray, fontSize = 12.sp)
                Text("${amigo.totalPoints} pts", color = Color(0xFFFF9800).copy(alpha = 0.8f), fontSize = 11.sp)
            }

            Box {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0x0DFFFFFF), RoundedCornerShape(8.dp))
                        .clickable { mostrarMenu = !mostrarMenu },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.MoreVert, null, tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                }
                DropdownMenu(
                    expanded = mostrarMenu,
                    onDismissRequest = { mostrarMenu = false },
                    modifier = Modifier.background(Color(0xFF1A1A1A))
                ) {
                    DropdownMenuItem(
                        text = { Text("Eliminar amigo", color = Color(0xFFEF4444), fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.PersonRemove, null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp)) },
                        onClick = {
                            mostrarMenu = false
                            onRemove()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Bloquear", color = Color(0xFFEF4444), fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Block, null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp)) },
                        onClick = {
                            mostrarMenu = false
                            onBlock()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Reportar", color = Color(0xFFE9C46A), fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Flag, null, tint = Color(0xFFE9C46A), modifier = Modifier.size(16.dp)) },
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
            .background(if (activa) color else Color(0xFF252525), RoundedCornerShape(10.dp))
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