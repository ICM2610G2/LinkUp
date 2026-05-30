package com.example.myapplication.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.repository.RaceRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

@Composable
fun LobbyCarreraScreen(
    sessionId: String,
    onBack: () -> Unit,
    onRaceStarted: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    val raceRepository = remember { RaceRepository() }
    val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    var raceName by remember { mutableStateOf("Cargando carrera...") }
    var status by remember { mutableStateOf("lobby") }
    var participantsCount by remember { mutableStateOf(0) }
    var createdBy by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    DisposableEffect(sessionId) {
        val listener = FirebaseFirestore.getInstance()
            .collection("race_sessions")
            .document(sessionId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    raceName = snapshot.getString("raceName") ?: "Carrera"
                    status = snapshot.getString("status") ?: "lobby"
                    createdBy = snapshot.getString("createdBy") ?: ""

                    val participants = snapshot.get("participants") as? Map<*, *>
                    participantsCount = participants?.size ?: 0

                    if (status == "active") {
                        onRaceStarted(sessionId)
                    }
                }
            }

        onDispose { listener.remove() }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                "Lobby de carrera",
                color = Color(0xFFFF9800),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                raceName,
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Groups,
                        null,
                        tint = Color(0xFFFF9800),
                        modifier = Modifier.size(34.dp)
                    )

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text("Participantes", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("$participantsCount jugador(es) en el lobby", color = Color.Gray, fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Estado: $status", color = Color.Gray)

            if (currentUid != createdBy) {
                Text("Esperando que el creador inicie la carrera", color = Color.Gray, fontSize = 12.sp)
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(errorMessage!!, color = Color(0xFFFF6B6B), fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    scope.launch {
                        errorMessage = null
                        val result = raceRepository.startRaceSession(sessionId)
                        result.onFailure { e -> errorMessage = e.message }
                    }
                },
                enabled = participantsCount >= 2 && currentUid == createdBy && status == "lobby",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF9800),
                    disabledContainerColor = Color(0xFFFF9800).copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.PlayArrow, null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Iniciar carrera", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}