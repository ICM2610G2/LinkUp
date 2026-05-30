package com.example.myapplication.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.models.Checkpoint
import com.example.myapplication.repository.RaceRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

@Composable
fun CarreraActivaScreen(
    sessionId: String,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val raceRepository = remember { RaceRepository() }
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    var raceId by remember { mutableStateOf("") }
    var raceName by remember { mutableStateOf("Carrera activa") }
    var participantsCount by remember { mutableStateOf(0) }
    var myCheckpointsDone by remember { mutableStateOf<List<String>>(emptyList()) }
    var checkpoints by remember { mutableStateOf<List<Checkpoint>>(emptyList()) }

    DisposableEffect(sessionId) {
        val listener = FirebaseFirestore.getInstance()
            .collection("race_sessions")
            .document(sessionId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    raceId = snapshot.getString("raceId") ?: ""
                    raceName = snapshot.getString("raceName") ?: "Carrera activa"

                    val participants = snapshot.get("participants") as? Map<*, *>
                    participantsCount = participants?.size ?: 0

                    val myData = participants?.get(uid) as? Map<*, *>
                    myCheckpointsDone = myData?.get("checkpointsDone") as? List<String> ?: emptyList()
                }
            }

        onDispose { listener.remove() }
    }

    LaunchedEffect(raceId) {
        if (raceId.isNotBlank()) {
            checkpoints = raceRepository.getCheckpoints(raceId)
        }
    }

    val completedCount = myCheckpointsDone.size
    val total = checkpoints.size
    val progress = if (total == 0) 0f else completedCount.toFloat() / total.toFloat()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                }

                Text(
                    raceName,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.headlineSmall
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    InfoBadgeCarrera("${participantsCount} jugadores", Icons.Default.Groups)
                    InfoBadgeCarrera("$completedCount/$total puntos", Icons.Default.Flag)
                }

                Spacer(modifier = Modifier.height(16.dp))

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = Color(0xFFFF9800),
                    trackColor = Color(0xFF252525)
                )
            }

            items(checkpoints, key = { it.id }) { checkpoint ->
                val done = myCheckpointsDone.contains(checkpoint.id)

                CheckpointCarreraItem(
                    checkpoint = checkpoint,
                    done = done,
                    onValidate = {
                        scope.launch {
                            raceRepository.validateCheckpoint(sessionId, checkpoint.id)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun InfoBadgeCarrera(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier
            .background(Color(0xFF1A1A1A), RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = Color(0xFFFF9800), modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text, color = Color.White)
    }
}

@Composable
fun CheckpointCarreraItem(
    checkpoint: Checkpoint,
    done: Boolean,
    onValidate: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(
            1.dp,
            if (done) Color(0xFF22C55E) else Color(0x12FFFFFF)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(
                        if (done) Color(0xFF22C55E) else Color(0x26FF9800),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (done) Icons.Default.Check else Icons.Default.Place,
                    null,
                    tint = if (done) Color.White else Color(0xFFFF9800)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(checkpoint.name, color = Color.White, fontWeight = FontWeight.Bold)
                Text(checkpoint.description, color = Color.Gray, maxLines = 2)
            }

            Button(
                onClick = onValidate,
                enabled = !done,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF9800),
                    disabledContainerColor = Color(0xFF252525)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (done) "Listo" else "Validar")
            }
        }
    }
}