package com.example.myapplication.screens.perfilScreens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.myapplication.data.models.RaceSession
import com.example.myapplication.repository.RaceRepository
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialCarrerasScreen(onBack: () -> Unit) {
    val raceRepository = remember { RaceRepository() }
    val accent = Color(0xFFFF9800)

    var sessions by remember { mutableStateOf<List<RaceSession>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            // Reutilizamos getUserActiveSessions + traemos finalizadas
            sessions = raceRepository.getAllUserSessions()
        } catch (e: Exception) {
            errorMsg = e.message
        }
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Historial de carreras",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1A1A1A)
                )
            )
        },
        containerColor = Color(0xFF0B0B0B)
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        color = accent,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                errorMsg != null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.ErrorOutline,
                            null,
                            tint = Color.Gray,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Error al cargar historial", color = Color.Gray)
                    }
                }

                sessions.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.EmojiEvents,
                            null,
                            tint = Color(0xFF333333),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Aún no has participado\nen ninguna carrera",
                            color = Color.Gray,
                            fontSize = 16.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "¡Inscríbete a una desde la pestaña Carreras!",
                            color = Color(0xFF555555),
                            fontSize = 13.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(sessions.sortedByDescending { it.createdAt }) { session ->
                            SessionCard(session = session, accent = accent)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SessionCard(session: RaceSession, accent: Color) {
    val statusColor = when (session.status) {
        "finished" -> Color(0xFF4CAF50)
        "active"   -> accent
        "lobby"    -> Color(0xFF2A9D8F)
        else       -> Color.Gray
    }
    val statusLabel = when (session.status) {
        "finished" -> "Finalizada"
        "active"   -> "En curso"
        "lobby"    -> "En lobby"
        else       -> session.status
    }

    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }
    val dateStr = try {
        session.createdAt?.toDate()?.let { dateFormatter.format(it) } ?: "—"
    } catch (_: Exception) { "—" }

    val participantCount = session.participantIds.size

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.DirectionsRun,
                        null,
                        tint = accent,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = session.raceName.ifBlank { "Carrera sin nombre" },
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                // Badge de estado
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusColor.copy(alpha = 0.18f)
                ) {
                    Text(
                        text = statusLabel,
                        color = statusColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFF2A2A2A))
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SessionStat(
                    icon = Icons.Default.CalendarToday,
                    label = "Fecha",
                    value = dateStr,
                    accent = accent
                )
                SessionStat(
                    icon = Icons.Default.Group,
                    label = "Participantes",
                    value = participantCount.toString(),
                    accent = accent
                )
            }
        }
    }
}

@Composable
private fun SessionStat(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    accent: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(icon, null, tint = accent, modifier = Modifier.size(14.dp))
        Column {
            Text(label, color = Color.Gray, fontSize = 10.sp)
            Text(value, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}