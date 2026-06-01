package com.example.myapplication.screens

import android.Manifest
import android.net.Uri
import androidx.compose.runtime.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.myapplication.model.ChatMessage
import java.text.SimpleDateFormat
import java.util.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.model.ChatViewModel
import java.io.File

@Composable
fun Chat(viewModel: ChatViewModel = viewModel()) {

    val context = LocalContext.current
    val state by viewModel.chatState.collectAsState()

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
        if (success) viewModel.updatePreviewImage(cameraUri)
    }

    // 🛡️ Camera permission
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            cameraLauncher.launch(cameraUri)
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        viewModel.updatePreviewImage(uri)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
    ) {

        // 🔝 TOP BAR
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1A1A1A))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFF9800)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.LocationOn, null, tint = Color.White)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text("Chat", color = Color.White, fontSize = 16.sp)
                Text("En línea", color = Color.Gray, fontSize = 12.sp)
            }
        }

        Divider(color = Color(0x22FFFFFF))

        // 💬 MESSAGES
        Box(modifier = Modifier.weight(1f)) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                reverseLayout = true,
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                itemsIndexed(state.messages.asReversed()) { _, message ->

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (message.isMe) Arrangement.End else Arrangement.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    if (message.isMe) Color(0xFFFF9800)
                                    else Color(0xFF1A1A1A),
                                    RoundedCornerShape(16.dp)
                                )
                                .padding(10.dp)
                        ) {

                            Column {

                                // 🖼 IMAGE
                                if (message.imageUri != null) {
                                    AsyncImage(
                                        model = message.imageUri,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(200.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable {
                                                viewModel.updateFullScreenImage(message.imageUri)
                                            },
                                        contentScale = ContentScale.Crop
                                    )

                                    if (message.text != null) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                    }
                                }

                                // 💬 TEXT
                                if (message.text != null) {
                                    Text(message.text, color = Color.White)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }

        if (state.previewImage != null) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black)
                    .padding(12.dp)
            ) {
                Box {
                    AsyncImage(
                        model = state.previewImage,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                    IconButton(
                        onClick = {
                            viewModel.updatePreviewImage(null)
                            viewModel.updateInputText("")
                        },
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Icon(Icons.Default.Close, null, tint = Color.White)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                TextField(
                    value = state.inputText,
                    onValueChange = {viewModel.updateInputText(it)},
                    placeholder = { Text("Agregar mensaje...") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF252525),
                        unfocusedContainerColor = Color(0xFF252525),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                IconButton(
                    onClick = {
                        val now = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                        viewModel.addMessage(
                            ChatMessage(
                                id = state.messages.size + 1,
                                sender = "Tú",
                                initial = "TÚ",
                                text = if (state.inputText.isBlank()) null else state.inputText,
                                imageUri = state.previewImage,
                                time = now,
                                isMe = true
                            )
                        )
                        viewModel.updatePreviewImage(null)
                        viewModel.updateInputText("")
                    },
                    modifier = Modifier
                        .align(Alignment.End)
                        .size(48.dp)
                        .background(Color(0xFFFF9800), CircleShape)
                ) {
                    Icon(Icons.Default.Send, null, tint = Color.White)
                }
            }

        } else {

            // 🧾 INPUT
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
                    onClick = {
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    },
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
                    onClick = {
                        if (state.inputText.isNotBlank()) {
                            val now = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                            viewModel.addMessage(
                                ChatMessage(
                                    id = state.messages.size + 1,
                                    sender = "Tú",
                                    initial = "TÚ",
                                    text = state.inputText,
                                    time = now,
                                    isMe = true
                                )
                            )
                            viewModel.updateInputText("")
                        }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color(0xFFFF9800), CircleShape)
                ) {
                    Icon(Icons.Default.Send, null, tint = Color.White)
                }
            }
        }
    }

    if (state.fullScreenImage != null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .clickable { viewModel.updateFullScreenImage(null) },
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = state.fullScreenImage,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )

            IconButton(
                onClick = { viewModel.updateFullScreenImage(null) },
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(Icons.Default.Close, null, tint = Color.White)
            }
        }
    }
}