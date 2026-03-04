package com.example.myapplication.ui.theme.screens

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
                text = "¡Ya llegué al Museo del Oro! 🏆",
                time = "14:23",
                isMe = false
            ),
            ChatMessage(
                id = 2,
                sender = "Camila R.",
                imageRes = context.resources.getIdentifier("museum_sample", "drawable", context.packageName)/*tryGetDrawable(context, "museum_sample")*/,
                text = null,
                time = "14:23",
                isMe = false,
                isImageVerified = true
            ),
            ChatMessage(
                id = 3,
                sender = "Andrés M.",
                text = "Voy para allá, estoy a 200m ✈️",
                time = "14:25",
                isMe = false
            ),
            ChatMessage(
                id = 4,
                sender = "Tu",
                text = "Jaja ya casi llego, no me ganen",
                time = "14:28",
                isMe = true
            ),
            ChatMessage(
                id = 5,
                sender = "Laura G.",
                text = "Felicidades!!!",
                time = "14:30",
                isMe = false
            )
        )
    }

    var inputText by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        TopBar()
        Divider(color = Color(0x1AFFFFFF))
        Box(modifier = Modifier.weight(1f)) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                reverseLayout = true,                                   // LazyColumn invertida para que los mensajes nuevos aparescan debajo
                verticalArrangement = Arrangement.Top,
                contentPadding = PaddingValues(top = 12.dp, bottom = 88.dp)
            ) {
                itemsIndexed(items = messages.asReversed()) { index, message ->         // mostrar los mensajes en orden al reves
                    ChatRow(message = message)
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            // Area de texto
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.Transparent)
                    .padding(12.dp).padding(bottom = 80.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp),
                        placeholder = { Text("Escribe un mensaje...", color = Color(0x80FFFFFF)) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF121214),
                            unfocusedContainerColor = Color(0xFF121214),
                            cursorColor = Color(0xFFFFA000),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.AttachFile,
                                contentDescription = "Adjuntar",
                                tint = Color(0x80FFFFFF),
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { /* galleria */ }) {
                                Icon(
                                    imageVector = Icons.Default.Photo,
                                    contentDescription = "Foto",
                                    tint = Color(0x80FFFFFF)
                                )
                            }
                        },
                        shape = RoundedCornerShape(28.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Boton de enviar
                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                // Agrega a la lista de chats
                                val now = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                                messages.add(
                                    ChatMessage(
                                        id = messages.size + 1,
                                        sender = "Tu",
                                        text = inputText,
                                        time = now,
                                        isMe = true
                                    )
                                )
                                inputText = ""
                            }
                        },
                        modifier = Modifier
                            .size(54.dp)
                            .background(color = Color(0xFFFF9800), shape = CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Enviar",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TopBar() {
    val orange = Color(0xFFFF9800)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF121214))
            .padding(horizontal = 12.dp, vertical = 12.dp).padding(top = 30.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(orange),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text("Ruta Centro Histórico", color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Text("4 participantes · En curso", color = Color(0xB3FFFFFF), fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.weight(1f))

        IconButton(onClick = { }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Más",
                tint = Color(0xB3FFFFFF)
            )
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
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        if (message.isMe) {
            Spacer(modifier = Modifier.weight(1f))
            Column(horizontalAlignment = Alignment.End) {       // A la derecha
                Bubble(isMe = true, bubbleColor = bubbleColorMe) {
                    if (message.text != null) {
                        Text(
                            text = message.text,
                            color = textColorMe,
                            fontSize = 15.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    } else if (message.imageRes != null) {
                        ImageMessageCard(
                            caption = null,
                            drawableRes = message.imageRes,
                            verified = message.isImageVerified
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(message.time, color = Color(0x80FFFFFF), fontSize = 11.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
            AvatarInitials(initial = "Tú", isMe = true)
        } else {
            AvatarInitials(initial = message.sender, isMe = false)
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(message.sender, color = Color(0xB3FFFFFF), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)     // Nombre remitente
                Spacer(modifier = Modifier.height(4.dp))
                Bubble(isMe = false, bubbleColor = bubbleColorOther) {
                    if (message.text != null) {
                        Text(
                            text = message.text,
                            color = textColorOther,
                            fontSize = 15.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    } else if (message.imageRes != null) {
                        ImageMessageCard(
                            caption = "Museo del Oro",
                            drawableRes = message.imageRes,
                            verified = message.isImageVerified
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(message.time, color = Color(0x80FFFFFF), fontSize = 11.sp)
            }
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun Bubble(isMe: Boolean, bubbleColor: Color, content: @Composable () -> Unit) {
    val shape = if (isMe) {
        RoundedCornerShape(16.dp, 0.dp, 16.dp, 16.dp)
    } else {
        RoundedCornerShape(0.dp, 16.dp, 16.dp, 16.dp)
    }

    Surface(
        color = bubbleColor,
        shape = shape,
        tonalElevation = 2.dp,
        modifier = Modifier.defaultMinSize(minWidth = 50.dp)
    ) {
        content()
    }
}

@Composable
fun AvatarInitials(initial: String, isMe: Boolean) {
    val bg = if (isMe) Color(0xFFFF9800) else Color(0xFF2B2B2B)
    val text = if (initial == "Tu") "TU" else initial.split(" ").getOrNull(0)?.firstOrNull()?.toString()
        ?: "CR"
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(bg),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ImageMessageCard(caption: String?, @DrawableRes drawableRes: Int?, verified: Boolean) {
    val cardWidth = 210.dp
    Column(
        modifier = Modifier
            .width(cardWidth)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF111113))
            .border(1.dp, Color(0x1FFFFFFF), RoundedCornerShape(12.dp))
    ) {
        if (drawableRes != null) {
            Image(
                painter = painterResource(id = drawableRes),
                contentDescription = caption ?: "image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .height(120.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
            )
        } else {
            // placeholder
            Box(
                modifier = Modifier
                    .height(120.dp)
                    .fillMaxWidth()
                    .background(Color(0xFF1B1B1C)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Photo,
                    contentDescription = "placeholder",
                    tint = Color(0x66FFFFFF),
                    modifier = Modifier.size(44.dp)
                )
            }
        }

        // titulo + tag de verificado
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                if (!caption.isNullOrEmpty()) {
                    Text(
                        text = caption,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    Text(
                        text = "Foto",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Foto verificada", color = Color(0xFF2ED47A), fontSize = 12.sp)
            }

            if (verified) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "verified",
                    tint = Color(0xFF2ED47A),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Preview
@Composable
fun SimpleComposablePreview() {
    Chat()
}

