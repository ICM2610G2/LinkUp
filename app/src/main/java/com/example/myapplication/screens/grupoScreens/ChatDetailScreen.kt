package com.example.myapplication.screens.grupoScreens

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.myapplication.model.ChatDetailViewModel
import com.example.myapplication.repository.UserRepository
import java.io.File

@Composable
fun ChatDetailScreen(
    chatId: String,
    chatName: String,
    isGroup: Boolean,
    isReadOnly: Boolean,
    onBack: () -> Unit,
    onAbrirCarrera: (raceId: String) -> Unit = {},
    viewModel: ChatDetailViewModel = viewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    val listState = rememberLazyListState()
    val userRepository = remember { UserRepository() }

    var otherUserPhotoURL by remember { mutableStateOf("") }
    var otherUserName by remember { mutableStateOf(chatName) }
    var isLoadingOtherUser by remember { mutableStateOf(true) }

    LaunchedEffect(chatId, isGroup) {
        if (!isGroup) {
            isLoadingOtherUser = true
            try {
                val participants = viewModel.getChatParticipants(chatId)
                val currentUserId = viewModel.getCurrentUserId()
                val otherId = participants.firstOrNull { it != currentUserId }
                if (otherId != null) {
                    val user = userRepository.getUser(otherId)
                    otherUserName = user?.displayName ?: chatName
                    otherUserPhotoURL = user?.photoURL ?: ""
                }
            } catch (e: Exception) {
                otherUserPhotoURL = ""
            }
            isLoadingOtherUser = false
        }
    }

    LaunchedEffect(chatId) {
        viewModel.initChat(chatId, isReadOnly)
        viewModel.loadMessages(chatId)
    }

    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size - 1)
        }
    }

    val imageFile = remember {
        File(context.filesDir, "camera_${System.currentTimeMillis()}.jpg")
    }
    val cameraUri = remember {
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            imageFile
        )
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) viewModel.sendImageMessage(chatId, cameraUri)
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) cameraLauncher.launch(cameraUri)
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.sendImageMessage(chatId, it) }
    }

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

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (isGroup) Color(0xFFFF9800) else Color(0xFF2A9D8F)),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isGroup -> {
                        Icon(Icons.Default.Group, null, tint = Color.White, modifier = Modifier.size(22.dp))
                    }
                    otherUserPhotoURL.isNotEmpty() && !isLoadingOtherUser -> {
                        AsyncImage(
                            model = otherUserPhotoURL,
                            contentDescription = "Foto de perfil",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    else -> {
                        Text(
                            otherUserName.take(2).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    if (isGroup) "Carrera en curso" else otherUserName,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                if (isReadOnly) {
                    Text("Solo lectura", color = Color.Gray, fontSize = 10.sp)
                } else {
                    Text("En línea", color = Color.Gray, fontSize = 10.sp)
                }
            }
        }

        HorizontalDivider(color = Color(0x22FFFFFF))

        Box(modifier = Modifier.weight(1f)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = listState,
                reverseLayout = false,
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.messages) { message ->
                    MessageBubble(
                        text = message.text,
                        imageUri = message.imageUri,
                        senderName = message.sender,
                        senderPhotoURL = message.photoURL,
                        time = message.time,
                        isMe = message.isMe,
                        isGroup = isGroup,
                        onAbrirCarrera = onAbrirCarrera
                    )
                }
            }
        }

        if (!isReadOnly) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1A1A1A))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { galleryLauncher.launch("image/*") },
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color(0xFF252525), CircleShape)
                ) {
                    Icon(Icons.Default.Photo, null, tint = Color.White)
                }

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) },
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color(0xFF252525), CircleShape)
                ) {
                    Icon(Icons.Default.CameraAlt, null, tint = Color.White)
                }

                Spacer(modifier = Modifier.width(8.dp))

                TextField(
                    value = state.inputText,
                    onValueChange = { viewModel.updateInputText(it) },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Mensaje...") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF252525),
                        unfocusedContainerColor = Color(0xFF252525),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = { viewModel.sendTextMessage() },
                    enabled = state.inputText.isNotBlank(),
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color(0xFFFF9800), CircleShape)
                ) {
                    Icon(Icons.Default.Send, null, tint = Color.White)
                }
            }
        }
    }
}

@Composable
fun MessageBubble(
    text: String?,
    imageUri: Uri?,
    senderName: String,
    senderPhotoURL: String,
    time: String,
    isMe: Boolean,
    isGroup: Boolean,
    onAbrirCarrera: (raceId: String) -> Unit = {}
) {
    val esInvitacion = text != null &&
            text.startsWith("🏃 Te han invitado a una carrera") &&
            text.contains("ID: ")

    val raceId = if (esInvitacion) {
        text!!.lines()
            .firstOrNull { it.startsWith("ID: ") }
            ?.removePrefix("ID: ")
            ?.trim() ?: ""
    } else ""

    val raceName = if (esInvitacion) {
        text!!.lines()
            .firstOrNull { it.startsWith("Nombre: ") }
            ?.removePrefix("Nombre: ")
            ?.trim() ?: ""
    } else ""

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        if (!isMe && isGroup) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFF9800)),
                contentAlignment = Alignment.Center
            ) {
                if (senderPhotoURL.isNotEmpty()) {
                    AsyncImage(
                        model = senderPhotoURL,
                        contentDescription = "Avatar",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        senderName.take(2).uppercase(),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(horizontalAlignment = if (isMe) Alignment.End else Alignment.Start) {
            if (!isMe && isGroup) {
                Text(senderName, color = Color(0xFFFF9800), fontSize = 10.sp)
            }

            if (esInvitacion) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2E)),
                    modifier = Modifier.widthIn(max = 260.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            "🏃 Invitación a carrera",
                            color = Color(0xFFFF9800),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            raceName,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { if (raceId.isNotEmpty()) onAbrirCarrera(raceId) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Ver carrera", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .background(
                            if (isMe) Color(0xFFFF9800) else Color(0xFF1A1A1A),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(10.dp)
                ) {
                    Column {
                        if (imageUri != null) {
                            AsyncImage(
                                model = imageUri,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(180.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                            if (!text.isNullOrEmpty()) {
                                Spacer(modifier = Modifier.height(6.dp))
                            }
                        }
                        if (!text.isNullOrEmpty()) {
                            Text(text, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}