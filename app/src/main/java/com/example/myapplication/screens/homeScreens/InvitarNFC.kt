package com.example.myapplication.screens.homeScreens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.model.InvitarNFCViewModel
import com.example.myapplication.repository.FriendsRepository
import com.example.myapplication.utils.InviteUtils
import com.example.myapplication.utils.QRGenerator
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun InvitarNFC(
    onCerrar: () -> Unit,
    onEscanearQR: () -> Unit,
    viewModel: InvitarNFCViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val friendsRepository = remember { FriendsRepository() }
    val currentUser = FirebaseAuth.getInstance().currentUser

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCC000000)),
        contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {

                HeaderInvitar(onCerrar = onCerrar)

                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    BotonNFC(
                        escaneando = state.escaneando,
                        onClick = {
                            viewModel.updateEscaneando(!state.escaneando)
                        }
                    )

                    if (state.escaneando) {
                        Text(
                            "📲 NFC Receptor activo. Acerca un tag u otro teléfono.",
                            color = Color(0xFF2A9D8F),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    SeparadorO()

                    // Temporal
                    if (state.generatedCode == null) {
                        Button(
                            onClick = {
                                scope.launch {
                                    viewModel.updateIsGenerating(true)
                                    try {
                                        val code = friendsRepository.generateFriendInvite()
                                        viewModel.updateGeneratedCode(code)
                                    } catch (e: Exception) {
                                        Log.e("InvitarNFC", "Error generando código", e)
                                    }
                                    viewModel.updateIsGenerating(false)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !state.isGenerating
                        ) {
                            if (state.isGenerating) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                            } else {
                                Icon(Icons.Default.VpnKey, null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Generar código de amistad")
                            }
                        }
                    } else {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF252525)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth(),
                            border = BorderStroke(1.dp, Color(0xFFFF9800))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("TU CÓDIGO TEMPORAL", color = Color.Gray, fontSize = 10.sp)
                                Text(
                                    state.generatedCode!!,
                                    color = Color(0xFFFF9800),
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 2.sp
                                )
                                Text("Expira en 15 minutos", color = Color.Gray, fontSize = 11.sp)
                                
                                Row(
                                    modifier = Modifier.padding(top = 12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            val clip = ClipData.newPlainText("Friend Code", state.generatedCode)
                                            clipboard.setPrimaryClip(clip)
                                            Toast.makeText(context, "Código copiado", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0x33FFFFFF)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Copiar", fontSize = 12.sp)
                                    }

                                    Button(
                                        onClick = {
                                            val shareIntent = Intent().apply {
                                                action = Intent.ACTION_SEND
                                                putExtra(Intent.EXTRA_TEXT, "Mi código de amistad en LinkUp es: ${state.generatedCode}\n\nAbre LinkUp y escribe este código en la sección Agregar Amigo.\n\nEste código expira en 15 minutos.")
                                                type = "text/plain"
                                            }
                                            context.startActivity(Intent.createChooser(shareIntent, "Compartir código"))
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.Share, null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Compartir", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }

                    // QR
                    BotonOpcion(
                        icono = Icons.Default.QrCode,
                        iconoColor = Color(0xFFE9C46A),
                        fondoIcono = Color(0x33E9C46A),
                        titulo = "Ver mi QR",
                        descripcion = "Usa el QR permanente de tu perfil",
                        onClick = { viewModel.updateMostrarQR(!state.mostrarQR) }
                    )

                    if (state.mostrarQR && currentUser != null) {
                        val qrContent = remember(currentUser.uid) { QRGenerator.createQrContent(currentUser.uid) }
                        val qrBitmap = remember(qrContent) { QRGenerator.generate(qrContent) }
                        CodigoQR(qrBitmap)
                    }

                    BotonOpcion(
                        icono = Icons.Default.QrCodeScanner,
                        iconoColor = Color(0xFF3B82F6),
                        fondoIcono = Color(0x333B82F6),
                        titulo = "Escanear QR",
                        descripcion = "Escanear el código de un amigo",
                        onClick = onEscanearQR
                    )

                    InfoSeguridad()
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun CodigoQR(bitmap: Bitmap) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Código QR Personal",
                modifier = Modifier.size(200.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "SISTEMA QR INDEPENDIENTE\nEscanea este código para agregarme",
                color = Color.Black,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun HeaderInvitar(onCerrar: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF252525))
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Invitar amigos", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        IconButton(onClick = onCerrar) {
            Icon(Icons.Default.Close, null, tint = Color.White)
        }
    }
}

@Composable
fun BotonNFC(escaneando: Boolean, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(if (escaneando) 0xFF2A9D8F else 0xFF252525)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        border = if (escaneando) BorderStroke(2.dp, Color.White) else null
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(Icons.Default.Nfc, null, tint = Color.White, modifier = Modifier.size(32.dp))
            Column {
                Text(if (escaneando) "Buscando dispositivos..." else "Activar Receptor NFC", color = Color.White, fontWeight = FontWeight.Bold)
                Text("Listo para recibir invitaciones", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun BotonOpcion(
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    iconoColor: Color,
    fondoIcono: Color,
    titulo: String,
    descripcion: String,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF252525)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier.size(40.dp).background(fondoIcono, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icono, null, tint = iconoColor, modifier = Modifier.size(20.dp))
            }
            Column {
                Text(titulo, color = Color.White, fontWeight = FontWeight.SemiBold)
                Text(descripcion, color = Color.Gray, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun InfoSeguridad() {
    Text(
        "💡 Los códigos de amistad son temporales y expiran por seguridad.",
        color = Color.Gray,
        fontSize = 11.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
    )
}

@Composable
fun SeparadorO() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(modifier = Modifier.weight(1f).height(1.dp).background(Color(0x1AFFFFFF)))
        Text("o", color = Color.White.copy(alpha = 0.4f), fontSize = 12.sp)
        Box(modifier = Modifier.weight(1f).height(1.dp).background(Color(0x1AFFFFFF)))
    }
}