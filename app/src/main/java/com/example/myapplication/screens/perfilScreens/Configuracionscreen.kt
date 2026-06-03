package com.example.myapplication.screens.perfilScreens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.models.User
import com.example.myapplication.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfiguracionScreen(
    userData: User?,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val userRepository = remember { UserRepository() }
    val accent = Color(0xFFFF9800)

    // Estado local de privacidad de ubicación
    var shareLocationMode by remember {
        mutableStateOf(userData?.shareLocationMode ?: "in_race")
    }
    var isSaving by remember { mutableStateOf(false) }
    var saveSuccess by remember { mutableStateOf(false) }

    val locationOptions = listOf(
        Triple("always", "Siempre visible", Icons.Default.LocationOn),
        Triple("in_race", "Solo en carreras", Icons.Default.DirectionsRun),
        Triple("never", "Nunca", Icons.Default.LocationOff)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Configuración",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── Sección: Privacidad de ubicación ──────────────────
            SectionHeader(
                icon = Icons.Default.PrivacyTip,
                title = "Privacidad de ubicación",
                accent = accent
            )

            Text(
                text = "Controla quién puede ver tu ubicación en el mapa.",
                color = Color.Gray,
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            locationOptions.forEach { (mode, label, icon) ->
                LocationOptionCard(
                    icon = icon,
                    label = label,
                    selected = shareLocationMode == mode,
                    accent = accent,
                    onClick = { shareLocationMode = mode }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── Sección: Cuenta ───────────────────────────────────
            SectionHeader(
                icon = Icons.Default.ManageAccounts,
                title = "Cuenta",
                accent = accent
            )

            InfoCard(
                icon = Icons.Default.Email,
                label = "Correo electrónico",
                value = userData?.email ?: FirebaseAuth.getInstance().currentUser?.email ?: "—",
                accent = accent
            )

            InfoCard(
                icon = Icons.Default.Tag,
                label = "Game ID",
                value = userData?.gameId ?: "—",
                accent = accent
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ── Botón guardar ─────────────────────────────────────
            Button(
                onClick = {
                    scope.launch {
                        isSaving = true
                        saveSuccess = false
                        try {
                            val uid = FirebaseAuth.getInstance().currentUser?.uid
                            if (uid != null) {
                                userRepository.updateShareLocationMode(uid, shareLocationMode)
                                saveSuccess = true
                            }
                        } catch (_: Exception) {}
                        isSaving = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = accent),
                shape = RoundedCornerShape(14.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.Save, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Guardar cambios", fontWeight = FontWeight.Bold)
                }
            }

            if (saveSuccess) {
                Text(
                    text = "✓ Cambios guardados",
                    color = Color(0xFF4CAF50),
                    fontSize = 14.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionHeader(icon: ImageVector, title: String, accent: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(top = 8.dp)
    ) {
        Icon(icon, null, tint = accent, modifier = Modifier.size(20.dp))
        Text(
            text = title,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun LocationOptionCard(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    accent: Color,
    onClick: () -> Unit
) {
    val borderColor = if (selected) accent else Color(0xFF2A2A2A)
    val bgColor = if (selected) accent.copy(alpha = 0.12f) else Color(0xFF1A1A1A)

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = CardDefaults.outlinedCardBorder().copy(
            // workaround: usamos modifier directamente
        ),
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                icon,
                null,
                tint = if (selected) accent else Color.Gray,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = label,
                color = if (selected) Color.White else Color.Gray,
                fontSize = 15.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                modifier = Modifier.weight(1f)
            )
            if (selected) {
                Icon(
                    Icons.Default.CheckCircle,
                    null,
                    tint = accent,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun InfoCard(
    icon: ImageVector,
    label: String,
    value: String,
    accent: Color
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(icon, null, tint = accent, modifier = Modifier.size(20.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(label, color = Color.Gray, fontSize = 11.sp)
                Text(value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}