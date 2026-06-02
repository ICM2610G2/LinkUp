package com.example.myapplication.screens.perfilScreens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.util.Log
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.myapplication.data.models.User
import com.example.myapplication.repository.StorageRepository
import com.example.myapplication.repository.UserRepository
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Pantalla de edición de perfil
 * Permite al usuario cambiar su nombre público y foto de perfil
 * La foto se sube a Firebase Storage y la URL se guarda en Firestore
 * El nombre se actualiza tanto en Firestore como en Firebase Auth
 */
@Composable
fun EditProfileScreen(
    userData: User,
    onSave: (User) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val userRepository = remember { UserRepository() }
    val storageRepository = remember { StorageRepository() }
    val auth = FirebaseAuth.getInstance()

    // Estado local para los campos editables
    var displayName by remember { mutableStateOf(userData.displayName) }
    var photoURL by remember { mutableStateOf(userData.photoURL) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    // Launcher para seleccionar imagen de la galería
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
        Log.d("EditProfile", "Imagen seleccionada: $uri")
    }

    // ============================================================
    // FUNCIÓN PRINCIPAL: Guardar cambios
    // ============================================================
    // 1. Sube la nueva imagen a Firebase Storage (si se seleccionó una)
    // 2. Actualiza el nombre en Firestore
    // 3. Actualiza la foto en Firestore (si cambió)
    // 4. Actualiza el nombre y foto en Firebase Auth
    // 5. Retorna el usuario actualizado a la pantalla anterior
    // ============================================================
    suspend fun saveChanges() {
        isLoading = true
        errorMessage = null

        try {
            var newPhotoURL = photoURL

            // PASO 1: Subir imagen si hay una nueva seleccionada
            if (selectedImageUri != null) {
                Log.d("EditProfile", "Subiendo imagen a Storage...")
                val uploadResult = storageRepository.uploadProfilePicture(selectedImageUri!!)
                uploadResult.fold(
                    onSuccess = { url ->
                        newPhotoURL = url
                        Log.d("EditProfile", "✅ Imagen subida: $url")
                    },
                    onFailure = { e ->
                        errorMessage = "Error al subir imagen: ${e.message}"
                        isLoading = false
                        return
                    }
                )
            }

            // PASO 2: Actualizar nombre en Firestore
            if (displayName != userData.displayName) {
                Log.d("EditProfile", "Actualizando nombre en Firestore...")
                val updateNameResult = userRepository.updateDisplayName(userData.uid, displayName)
                if (updateNameResult.isFailure) {
                    errorMessage = "Error al actualizar nombre"
                    isLoading = false
                    return
                }
            }

            // PASO 3: Actualizar foto en Firestore (si cambió)
            if (newPhotoURL != photoURL) {
                Log.d("EditProfile", "Actualizando foto en Firestore...")
                val updatePhotoResult = userRepository.updatePhotoURL(userData.uid, newPhotoURL)
                if (updatePhotoResult.isFailure) {
                    errorMessage = "Error al actualizar foto"
                    isLoading = false
                    return
                }
            }

            // PASO 4: Actualizar perfil en Firebase Auth para persistencia
            val currentUser = auth.currentUser
            if (currentUser != null) {
                Log.d("EditProfile", "Actualizando perfil en Firebase Auth...")
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)
                    .setPhotoUri(if (newPhotoURL.isNotEmpty()) Uri.parse(newPhotoURL) else null)
                    .build()
                currentUser.updateProfile(profileUpdates).await()
                Log.d("EditProfile", "✅ Perfil de Auth actualizado")
            }

            // PASO 5: Crear usuario actualizado para devolver
            val updatedUser = userData.copy(
                displayName = displayName,
                photoURL = newPhotoURL
            )

            Log.d("EditProfile", "✅ Perfil guardado correctamente")
            onSave(updatedUser)

        } catch (e: Exception) {
            Log.e("EditProfile", "Error inesperado: ${e.message}", e)
            errorMessage = "Error inesperado: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    // UI de la pantalla de edición
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
                saveChanges()
            }
        },
        onCancelClick = onCancel,
        onImageClick = { galleryLauncher.launch("image/*") },
        onCopyGameId = {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Game ID", userData.gameId)
            clipboard.setPrimaryClip(clip)
            Log.d("EditProfile", "Game ID copiado: ${userData.gameId}")
        }
    )
}

/**
 * Componente UI de la pantalla de edición de perfil
 * Separado de la lógica para mejor organización y preview
 */
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
        // ============================================================
        // HEADER: Barra superior con botones Cancelar y Guardar
        // ============================================================
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

        // ============================================================
        // AVATAR: Foto de perfil clickeable
        // Muestra la imagen seleccionada, la existente o el icono por defecto
        // Al hacer clic, abre la galería para seleccionar nueva foto
        // ============================================================
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
                // Prioridad: 1. Imagen seleccionada, 2. Imagen existente, 3. Icono por defecto
                when {
                    selectedImageUri != null -> {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = "Foto de perfil (nueva)",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    photoURL.isNotEmpty() -> {
                        AsyncImage(
                            model = photoURL,
                            contentDescription = "Foto de perfil actual",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    else -> {
                        Icon(
                            Icons.Default.Person,
                            null,
                            tint = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
                // Ícono de cámara/crayón para indicar que es editable
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

        // ============================================================
        // CAMPO DE NOMBRE: Input para cambiar nombre público
        // ============================================================
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

        // ============================================================
        // GAME ID: Solo lectura, con botón para copiar
        // ============================================================
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
                IconButton(onClick = onCopyGameId) {
                    Icon(Icons.Default.ContentCopy, null, tint = Color(0xFFFF9800), modifier = Modifier.size(18.dp))
                }
            }
        }

        // ============================================================
        // MENSAJE DE ERROR: Se muestra si ocurre algún problema
        // ============================================================
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
                email = "juan@example.com",
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