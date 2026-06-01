package com.example.myapplication.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import coil.compose.AsyncImage
import com.example.myapplication.data.models.User
import com.example.myapplication.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import java.util.UUID
import kotlinx.coroutines.tasks.await
import androidx.compose.ui.tooling.preview.Preview
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.google.firebase.auth.UserProfileChangeRequest

@Composable
fun EditProfileScreen(
    userData: User,
    onSave: (User) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val userRepository = remember { UserRepository() }
    val auth = FirebaseAuth.getInstance()

    var displayName by remember { mutableStateOf(userData.displayName) }
    var photoURL by remember { mutableStateOf(userData.photoURL) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    // Launcher para seleccionar imagen de galería
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    // Función para subir imagen a Firebase Storage
    suspend fun uploadImage(uri: Uri): String? {
        return try {
            val storageRef = FirebaseStorage.getInstance().reference
            val imageRef = storageRef.child("profile_images/${auth.currentUser?.uid}/${UUID.randomUUID()}.jpg")
            imageRef.putFile(uri).await()
            val downloadUrl = imageRef.downloadUrl.await()
            downloadUrl.toString()
        } catch (e: Exception) {
            null
        }
    }

    EditProfileContent(
        userData = userData,
        displayName = displayName,
        onDisplayNameChange = { displayName = it },
        photoURL = photoURL,
        selectedImageUri = selectedImageUri,
        isLoading = isLoading,
        errorMessage = errorMessage,
        onSaveClick = {
            scope.launch {
                isLoading = true

                // Subir nueva foto si hay
                var newPhotoURL = photoURL
                if (selectedImageUri != null) {
                    val uploadedUrl = uploadImage(selectedImageUri!!)
                    if (uploadedUrl != null) {
                        newPhotoURL = uploadedUrl
                    }
                }

                // Actualizar en Firestore
                val updatedUser = userData.copy(
                    displayName = displayName.trim(),
                    photoURL = selectedImageUri?.toString() ?: newPhotoURL
                )

                onSave(updatedUser)

                val saveResult = userRepository.updateUser(updatedUser)

                if (saveResult.isSuccess) {
                    onSave(updatedUser)
                } else {
                    errorMessage = "No se pudo guardar el perfil"
                }
                val result = userRepository.updateUser(updatedUser)

                if (result.isSuccess) {
                    // Actualizar displayName en Firebase Auth
                    auth.currentUser?.updateProfile(
                        UserProfileChangeRequest.Builder()
                            .setDisplayName(displayName)
                            .setPhotoUri(if (newPhotoURL.isNotEmpty()) Uri.parse(newPhotoURL) else null)
                            .build()
                    )?.await()


                    onSave(updatedUser)
                } else {
                    errorMessage = "Error al guardar"
                }
                isLoading = false
            }
        },
        onCancelClick = onCancel,
        onImageClick = { galleryLauncher.launch("image/*") },
        onCopyGameId = {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Game ID", userData.gameId)
            clipboard.setPrimaryClip(clip)
        }
    )
}

@Composable
fun EditProfileContent(
    userData: User,
    displayName: String,
    onDisplayNameChange: (String) -> Unit,
    photoURL: String,
    selectedImageUri: Uri?,
    isLoading: Boolean,
    errorMessage: String?,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit,
    onImageClick: () -> Unit,
    onCopyGameId: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0B0B))
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1A1A1A))
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onCancelClick) {
                Text("Cancelar", color = Color.Gray)
            }
            Text("Editar perfil", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            TextButton(
                onClick = onSaveClick,
                enabled = displayName.isNotBlank() && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color(0xFFFF9800))
                } else {
                    Text("Guardar", color = Color(0xFFFF9800), fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Foto de perfil (clic para cambiar)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentSize(),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF252525))
                    .clickable { onImageClick() },
                contentAlignment = Alignment.Center
            ) {
                if (selectedImageUri != null) {
                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = "Foto de perfil",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else if (photoURL.isNotEmpty()) {
                    AsyncImage(
                        model = photoURL,
                        contentDescription = "Foto de perfil",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Default.Person,
                        null,
                        tint = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(48.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(32.dp)
                        .background(Color(0xFFFF9800), CircleShape)
                        .padding(6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Edit, null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Campo de nombre
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Nombre público", color = Color.Gray, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = displayName,
                    onValueChange = onDisplayNameChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Tu nombre") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF252525),
                        unfocusedContainerColor = Color(0xFF252525),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color(0xFFFF9800)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        // Game ID (solo lectura)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 12.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Game ID", color = Color.Gray, fontSize = 12.sp)
                    Text(userData.gameId, color = Color.White, fontWeight = FontWeight.Bold)
                }
                IconButton(
                    onClick = onCopyGameId
                ) {
                    Icon(Icons.Default.ContentCopy, null, tint = Color(0xFFFF9800), modifier = Modifier.size(18.dp))
                }
            }
        }

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                errorMessage,
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0B0B0B)
@Composable
fun EditProfilePreview() {
    MyApplicationTheme {
        EditProfileContent(
            userData = User(
                uid = "123",
                displayName = "Juan Pérez",
                gameId = "linkup#4821",
                photoURL = ""
            ),
            displayName = "Juan Pérez",
            onDisplayNameChange = {},
            photoURL = "",
            selectedImageUri = null,
            isLoading = false,
            errorMessage = null,
            onSaveClick = {},
            onCancelClick = {},
            onImageClick = {},
            onCopyGameId = {}
        )
    }
}
