package com.example.myapplication.repository

import android.net.Uri
import android.util.Log
import com.example.myapplication.data.models.Chat
import com.example.myapplication.model.ChatMessage
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.emptyList
import com.google.firebase.firestore.ListenerRegistration

class ChatRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    private val currentUserId: String?
        get() = auth.currentUser?.uid

    // ============================================================
    // CHATS (conversaciones)
    // ============================================================

    fun getChats(): Flow<List<Chat>> = callbackFlow {
        val userId = currentUserId
        if (userId == null) {
            close()
            return@callbackFlow
        }

        val listener = firestore.collection("chats")
            .whereArrayContains("participants", userId)
            .orderBy("lastMessageAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("ChatRepo", "Error getting chats: ${error.message}")
                    close(error)
                    return@addSnapshotListener
                }

                val chats = mutableListOf<Chat>()
                snapshot?.documents?.forEach { doc ->
                    try {
                        val type = doc.getString("type") ?: "direct"
                        val participants = doc.get("participants") as? List<*> ?: emptyList<String>()
                        val participantsList = participants.filterIsInstance<String>()
                        val lastMessage = doc.getString("lastMessage") ?: ""
                        val lastMessageAt = doc.getTimestamp("lastMessageAt") ?: Timestamp.now()
                        val sessionId = doc.getString("sessionId") ?: ""
                        val readOnly = doc.getBoolean("readOnly") ?: false
                        val deleteAt = doc.getTimestamp("deleteAt")
                        val createdAt = doc.getTimestamp("createdAt") ?: Timestamp.now()

                        val chat = Chat(
                            id = doc.id,
                            type = type,
                            participants = participantsList,
                            lastMessage = lastMessage,
                            lastMessageAt = lastMessageAt,
                            sessionId = sessionId,
                            readOnly = readOnly,
                            deleteAt = deleteAt,
                            createdAt = createdAt
                        )
                        chats.add(chat)
                    } catch (e: Exception) {
                        Log.e("ChatRepo", "Error parsing chat doc: ${e.message}")
                    }
                }

                val activeChats = chats.filter { chat ->
                    when {
                        chat.type == "direct" -> true
                        chat.deleteAt == null -> true
                        else -> chat.deleteAt > Timestamp.now()
                    }
                }
                trySend(activeChats)
            }

        awaitClose {
            listener.remove()
        }
    }

    fun listenToChats(onUpdate: (List<Chat>) -> Unit): ListenerRegistration {
        val userId = currentUserId
        if (userId == null) {
            // Si no hay usuario, devolver un listener vacío
            return object : ListenerRegistration {
                override fun remove() {}
            }
        }

        val listener = firestore.collection("chats")
            .orderBy("lastMessageAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("ChatRepo", "Error: ${error.message}")
                    return@addSnapshotListener
                }

                val chats = mutableListOf<Chat>()
                snapshot?.documents?.forEach { doc ->
                    try {
                        val participants = doc.get("participants") as? List<*> ?: emptyList<String>()
                        val participantsList = participants.filterIsInstance<String>()

                        // Solo incluir chats donde el usuario actual es participante
                        if (participantsList.contains(userId)) {
                            val chat = Chat(
                                id = doc.id,
                                type = doc.getString("type") ?: "direct",
                                participants = participantsList,
                                lastMessage = doc.getString("lastMessage") ?: "",
                                lastMessageAt = doc.getTimestamp("lastMessageAt") ?: Timestamp.now(),
                                sessionId = doc.getString("sessionId") ?: "",
                                readOnly = doc.getBoolean("readOnly") ?: false,
                                deleteAt = doc.getTimestamp("deleteAt"),
                                createdAt = doc.getTimestamp("createdAt") ?: Timestamp.now()
                            )
                            chats.add(chat)
                        }
                    } catch (e: Exception) {
                        Log.e("ChatRepo", "Error parsing chat: ${e.message}")
                    }
                }

                // Ordenar por último mensaje (más reciente primero)
                val sorted = chats.sortedByDescending { it.lastMessageAt.seconds }
                onUpdate(sorted)
            }

        return object : ListenerRegistration {
            override fun remove() {
                listener.remove()
            }
        }
    }
    fun listenToMessages(chatId: String, onUpdate: (List<ChatMessage>) -> Unit): ListenerRegistration {
        return firestore.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("sentAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("ChatRepo", "Error: ${error.message}")
                    return@addSnapshotListener
                }

                val messages = mutableListOf<ChatMessage>()
                snapshot?.documents?.forEach { doc ->
                    try {
                        val message = ChatMessage(
                            id = doc.getLong("id")?.toInt() ?: 0,
                            sender = doc.getString("sender") ?: "",
                            initial = doc.getString("initial") ?: "",
                            text = doc.getString("text"),
                            imageUri = doc.getString("imageUri")?.let { Uri.parse(it) },
                            senderId = doc.getString("senderId") ?: "",
                            photoURL = doc.getString("photoURL") ?: "",
                            time = doc.getString("time") ?: "",
                            isMe = doc.getString("senderId") == currentUserId,
                            isImageVerified = doc.getBoolean("isImageVerified") ?: false
                        )
                        messages.add(message)
                    } catch (e: Exception) {
                        Log.e("ChatRepo", "Error parsing message: ${e.message}")
                    }
                }
                onUpdate(messages)
            }
    }




    suspend fun getOrCreateDirectChat(otherUserId: String): Result<Chat> {
        val userId = currentUserId ?: return Result.failure(Exception("Usuario no autenticado"))

        return try {
            val querySnapshot = firestore.collection("chats")
                .whereArrayContains("participants", userId)
                .get()
                .await()

            var existingChat: Chat? = null
            for (doc in querySnapshot.documents) {
                val type = doc.getString("type")
                val participants = doc.get("participants") as? List<*> ?: emptyList<String>()
                val participantsList = participants.filterIsInstance<String>()
                if (type == "direct" && participantsList.contains(otherUserId)) {
                    existingChat = Chat(
                        id = doc.id,
                        type = type,
                        participants = participantsList,
                        lastMessage = doc.getString("lastMessage") ?: "",
                        lastMessageAt = doc.getTimestamp("lastMessageAt") ?: Timestamp.now(),
                        sessionId = doc.getString("sessionId") ?: "",
                        readOnly = doc.getBoolean("readOnly") ?: false,
                        deleteAt = doc.getTimestamp("deleteAt"),
                        createdAt = doc.getTimestamp("createdAt") ?: Timestamp.now()
                    )
                    break
                }
            }

            if (existingChat != null) {
                return Result.success(existingChat)
            }

            val chatId = if (userId < otherUserId) "${userId}_${otherUserId}" else "${otherUserId}_${userId}"
            val newChat = Chat(
                id = chatId,
                type = "direct",
                participants = listOf(userId, otherUserId),
                lastMessage = "",
                lastMessageAt = Timestamp.now(),
                sessionId = "",
                readOnly = false,
                deleteAt = null,
                createdAt = Timestamp.now()
            )

            firestore.collection("chats").document(chatId).set(newChat).await()
            Result.success(newChat)
        } catch (e: Exception) {
            Log.e("ChatRepo", "Error getting/creating chat: ${e.message}")
            Result.failure(e)
        }
    }

    // ============================================================
    // MENSAJES
    // ============================================================

    fun getMessages(chatId: String): Flow<List<ChatMessage>> = callbackFlow {
        val listener = firestore.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("sentAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("ChatRepo", "Error getting messages: ${error.message}")
                    close(error)
                    return@addSnapshotListener
                }

                val messages = mutableListOf<ChatMessage>()
                snapshot?.documents?.forEach { doc ->
                    try {
                        val id = doc.getLong("id")?.toInt() ?: 0
                        val sender = doc.getString("sender") ?: ""
                        val senderId = doc.getString("senderId") ?: ""
                        val photoURL = doc.getString("photoURL") ?: ""
                        val initial = doc.getString("initial") ?: ""
                        val text = doc.getString("text")
                        val imageUriString = doc.getString("imageUri")
                        val imageUri = imageUriString?.let { Uri.parse(it) }
                        val time = doc.getString("time") ?: ""
                        val isImageVerified = doc.getBoolean("isImageVerified") ?: false

                        val message = ChatMessage(
                            id = id,
                            sender = sender,
                            initial = initial,
                            text = text,
                            imageUri = imageUri,
                            senderId = senderId,
                            photoURL = photoURL,
                            time = time,
                            isMe = senderId == currentUserId,
                            isImageVerified = isImageVerified
                        )
                        messages.add(message)
                    } catch (e: Exception) {
                        Log.e("ChatRepo", "Error parsing message doc: ${e.message}")
                    }
                }
                trySend(messages)
            }

        awaitClose {
            listener.remove()
        }
    }

    suspend fun sendTextMessage(chatId: String, text: String): Result<Unit> {
        val userId = currentUserId ?: return Result.failure(Exception("Usuario no autenticado"))
        val userRepository = UserRepository()
        val userData = userRepository.getUser(userId)
        val now = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

        return try {
            val message = hashMapOf(
                "id" to System.currentTimeMillis().toInt(),
                "sender" to (userData?.displayName ?: auth.currentUser?.displayName ?: "Usuario"),
                "senderId" to userId,
                "photoURL" to (userData?.photoURL ?: auth.currentUser?.photoUrl?.toString() ?: ""),
                "initial" to (userData?.displayName?.take(2)?.uppercase() ?: "US"),
                "text" to text,
                "time" to now,
                "sentAt" to Timestamp.now(),
                "isImageVerified" to false
            )

            firestore.collection("chats")
                .document(chatId)
                .collection("messages")
                .document()
                .set(message)
                .await()

            firestore.collection("chats").document(chatId)
                .update(
                    "lastMessage", text.take(50),
                    "lastMessageAt", Timestamp.now()
                )
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("ChatRepo", "Error sending message: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun sendImageMessage(chatId: String, imageUri: Uri, caption: String? = null): Result<Unit> {
        val userId = currentUserId ?: return Result.failure(Exception("Usuario no autenticado"))
        val userRepository = UserRepository()
        val userData = userRepository.getUser(userId)
        val now = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

        return try {
            val imageRef = storage.reference.child("chat_images/${UUID.randomUUID()}.jpg")
            imageRef.putFile(imageUri).await()
            val imageUrl = imageRef.downloadUrl.await().toString()

            val message = hashMapOf(
                "id" to System.currentTimeMillis().toInt(),
                "sender" to (userData?.displayName ?: auth.currentUser?.displayName ?: "Usuario"),
                "senderId" to userId,
                "photoURL" to (userData?.photoURL ?: auth.currentUser?.photoUrl?.toString() ?: ""),
                "initial" to (userData?.displayName?.take(2)?.uppercase() ?: "US"),
                "text" to (caption ?: ""),
                "imageUri" to imageUrl,
                "time" to now,
                "sentAt" to Timestamp.now(),
                "isImageVerified" to false
            )

            firestore.collection("chats")
                .document(chatId)
                .collection("messages")
                .document()
                .set(message)
                .await()

            firestore.collection("chats").document(chatId)
                .update(
                    "lastMessage", if (caption != null) "📷 $caption" else "📷 Imagen",
                    "lastMessageAt", Timestamp.now()
                )
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("ChatRepo", "Error sending image: ${e.message}")
            Result.failure(e)
        }
    }

    // ============================================================
    // CHATS GRUPALES
    // ============================================================

    suspend fun createGroupChat(
        sessionId: String,
        raceName: String,
        participants: List<String>,
        createdBy: String
    ): Result<String> {
        return try {
            val chatId = "race_$sessionId"
            val newChat = Chat(
                id = chatId,
                type = "group",
                participants = participants,
                lastMessage = "Carrera iniciada: $raceName",
                lastMessageAt = Timestamp.now(),
                sessionId = sessionId,
                readOnly = false,
                deleteAt = null,
                createdAt = Timestamp.now()
            )

            firestore.collection("chats").document(chatId).set(newChat).await()
            Result.success(chatId)
        } catch (e: Exception) {
            Log.e("ChatRepo", "Error creating group chat: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun setGroupChatReadOnly(chatId: String) {
        try {
            val deleteAt = Timestamp.now().let { Timestamp(it.seconds + 600, it.nanoseconds) }
            firestore.collection("chats").document(chatId)
                .update(
                    "readOnly", true,
                    "deleteAt", deleteAt
                )
                .await()
            Log.d("ChatRepo", "Group chat $chatId set to read-only, will delete at $deleteAt")
        } catch (e: Exception) {
            Log.e("ChatRepo", "Error setting chat read-only: ${e.message}")
        }
    }

    /**
     * Obtiene un chat por su ID
     */
    suspend fun getChatById(chatId: String): Chat? {
        return try {
            val doc = firestore.collection("chats").document(chatId).get().await()
            doc.toObject(Chat::class.java)?.copy(id = doc.id)
        } catch (e: Exception) {
            Log.e("ChatRepo", "Error getting chat: ${e.message}")
            null
        }
    }
}