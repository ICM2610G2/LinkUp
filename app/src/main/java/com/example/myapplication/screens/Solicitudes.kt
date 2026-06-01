package com.example.myapplication.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.myapplication.model.SolicitudConUsuario
import com.example.myapplication.model.SolicitudesViewModel
import androidx.compose.foundation.BorderStroke

@Composable
fun Solicitudes(
    onBack: () -> Unit,
    viewModel: SolicitudesViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

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
                "Solicitudes de amistad",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            if (state.solicitudes.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(Color(0xFFFF9800), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        state.solicitudes.size.toString(),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        when {
            state.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFFF9800))
                }
            }
            state.solicitudes.isEmpty() -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.PersonAdd, null, tint = Color.Gray, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No tienes solicitudes pendientes", color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Cuando alguien te agregue, aparecerá aquí",
                        color = Color.Gray.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = state.solicitudes,
                        key = { it.friendship.id }
                    ) { item ->
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

@Composable
fun SolicitudCard(
    item: SolicitudConUsuario,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    val user = item.solicitante

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, Color(0x4DE9C46A))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Avatar con foto real o iniciales
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE9C46A)),
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
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(user.displayName, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Text(user.gameId, color = Color.Gray, fontSize = 12.sp)
                    Text(
                        "${user.totalPoints} pts · ${user.totalPlacesVisited} lugares visitados",
                        color = Color.Gray.copy(alpha = 0.6f),
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onAccept,
                    modifier = Modifier.weight(1f).height(44.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A9D8F)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Aceptar", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
                Button(
                    onClick = onReject,
                    modifier = Modifier.weight(1f).height(44.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0x33FFFFFF))
                ) {
                    Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Rechazar", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}