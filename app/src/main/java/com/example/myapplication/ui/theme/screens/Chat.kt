package com.example.myapplication.ui.theme.screens

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.model.ChatMessage
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun Chat() {
    val context = LocalContext.current
    val messages = remember {
        mutableStateListOf(
            ChatMessage(
                id = 1,
                sender = "Camila R.",
                initial = "CR",
                text = "¡Ya llegué al Museo del Oro! 🏆",
                time = "14:23",
                isMe = false
            ),
            ChatMessage(
                id = 2,
                sender = "Camila R.",
                initial = "CR",
                imageRes = context.resources.getIdentifier("museum_sample", "drawable", context.packageName)/*tryGetDrawable(context, "museum_sample")*/,
                text = null,
                time = "14:23",
                isMe = false,
                isImageVerified = true
            ),
            ChatMessage(
                id = 3,
                sender = "Andrés M.",
                initial = "AM",
                text = "Voy para allá, estoy a 200m ✈️",
                time = "14:25",
                isMe = false
            ),
            ChatMessage(
                id = 4,
                sender = "Tu",
                initial = "TÚ",
                text = "Jaja ya casi llego, no me ganen",
                time = "14:28",
                isMe = true
            ),
            ChatMessage(
                id = 5,
                sender = "Laura G.",
                initial = "LG",
                text = "Felicidades!!!",
                time = "14:30",
                isMe = false
            )
        )
    }

    var inputText by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF0A0A0A))) {
        TopBar()
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color(0x0DFFFFFF))
        )
        Box(modifier = Modifier.weight(1f)) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                reverseLayout = true,                                   // LazyColumn invertida para que los mensajes nuevos aparescan debajo
                verticalArrangement = Arrangement.Top,
                contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
            ) {
                itemsIndexed(items = messages.asReversed()) { index, message ->         // mostrar los mensajes en orden al reves
                    ChatRow(message = message)
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        // Area de texto
        ChatInputArea(
            inputText = inputText,
            onInputChange = { inputText = it },
            onSend = {
                if (inputText.isNotBlank()) {
                    val now = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                    messages.add(
                        ChatMessage(
                            id = messages.size + 1,
                            sender = "Tú",
                            initial = "TÚ",
                            text = inputText,
                            time = now,
                            isMe = true
                        )
                    )
                    Log.i("MyApp", "Mensaje enviado: $inputText")
                    inputText = ""
                }
            }
        )
    }
}

@Composable
fun TopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1A1A1A))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color(0xFFFF9800)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text("Ruta Centro Histórico",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp)
            Text("4 participantes · En curso",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 12.sp)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            listOf("CR", "AM", "LP").forEach { iniciales ->
                Box(
                    modifier = Modifier
                        .background(Color(0xFF252525), RoundedCornerShape(50))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        iniciales,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun ChatRow(message: ChatMessage) {
    val bubbleColorMe = Color(0xFFFF9800)
    val bubbleColorOther = Color(0x1FFFFFFF)
    val textColorMe = Color.White
    val textColorOther = Color(0xE6FFFFFF)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = if (message.isMe) Arrangement.End else Arrangement.Start
    ) {
        if (message.isMe) {
            Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f)) {       // A la derecha
                Bubble(message = message)
                Spacer(modifier = Modifier.height(4.dp))
                Text(message.time, color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
            AvatarInitials(initial = "TÚ", isMe = true)
        } else {
            AvatarInitials(initial = message.initial, isMe = false)
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(message.sender,
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 10.sp
                )     // Nombre remitente
                Spacer(modifier = Modifier.height(4.dp))
                Bubble(message = message)
                Spacer(modifier = Modifier.height(4.dp))
                Text(message.time,
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 10.sp)
            }
        }
    }
}

@Composable
fun Bubble(message: ChatMessage) {
    if (message.text != null) {
        Box(
            modifier = Modifier
                .background(
                    if (message.isMe) Color(0xFFFF9800) else Color(0xFF1A1A1A),
                    RoundedCornerShape(
                        topStart = if (message.isMe) 16.dp else 0.dp,
                        topEnd = if (message.isMe) 0.dp else 16.dp,
                        bottomStart = 16.dp,
                        bottomEnd = 16.dp
                    )
                )
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Text(message.text, color = Color.White, fontSize = 14.sp)
        }
    } else {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.width(220.dp)
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(Color(0xFF1B1B1C)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Photo,
                        null,
                        tint = Color.White.copy(alpha = 0.4f),
                        modifier = Modifier.size(44.dp)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF252525))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(Color(0xFFEF4444), CircleShape)
                    )
                    Text(
                        "Museo del Oro",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 10.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        "Foto verificada",
                        color = Color(0xFF22C55E),
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
fun AvatarInitials(initial: String, isMe: Boolean) {
    val bg = if (isMe) Color(0xFFFF9800) else Color(0xFF252525)
    val text = if (initial == "Tu") "TU"
                else initial.split(" ").getOrNull(0)?.firstOrNull()?.toString() ?: "?"
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(bg),
        contentAlignment = Alignment.Center
    ) {
        Text(initial,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ChatInputArea(
    inputText: String,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1A1A1A))
            .padding(horizontal = 16.dp, vertical = 12.dp)
            /*.padding(bottom = 80.dp)*/,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        IconButton(
            onClick = { Log.i("MyApp", "Adjuntar imagen clicked") },
            modifier = Modifier
                .size(44.dp)
                .background(Color(0xFF252525), CircleShape)
        ) {
            Icon(Icons.Default.Photo, null, tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(20.dp))
        }
        TextField(
            value = inputText,
            onValueChange = onInputChange,
            modifier = Modifier.weight(1f).height(54.dp),
            placeholder = { Text("Escribe un mensaje...", color = Color.White.copy(alpha = 0.4f), fontSize = 14.sp) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF252525),
                unfocusedContainerColor = Color(0xFF252525),
                cursorColor = Color(0xFFFF9800),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(50),
            singleLine = true
        )
        IconButton(
            onClick = onSend,
            modifier = Modifier
                .size(44.dp)
                .background(Color(0xFFFF9800), CircleShape)
        ) {
            Icon(Icons.Default.Send, null, tint = Color.White, modifier = Modifier.size(20.dp))
        }
    }
}

@Preview
@Composable
fun SimpleComposablePreview() {
    Chat()
}

