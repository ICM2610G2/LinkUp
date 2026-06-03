package com.example.myapplication.repository

import android.net.Uri
import android.util.Log
import com.example.myapplication.data.models.Checkpoint
import com.example.myapplication.data.models.Race
import com.example.myapplication.data.models.RaceSession
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID
import kotlin.math.*
import com.google.firebase.database.FirebaseDatabase

class RaceRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) {

    suspend fun createRace(
        name: String,
        description: String,
        isPublic: Boolean,
        checkpoints: List<Checkpoint>,
        imageUri: Uri? = null
    ): Result<String> {
        return try {
            val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Usuario no autenticado"))

            if (name.isBlank()) return Result.failure(Exception("El nombre es obligatorio"))
            if (checkpoints.size < 2) return Result.failure(Exception("Debes seleccionar al menos 2 checkpoints"))

            var photoUrl = ""
            if (imageUri != null) {
                val fileName = "races/${UUID.randomUUID()}.jpg"
                val imageRef = storage.reference.child(fileName)
                imageRef.putFile(imageUri).await()
                photoUrl = imageRef.downloadUrl.await().toString()
            }

            val distanceKm = calculateTotalDistanceKm(checkpoints)

            val difficulty = when {
                distanceKm < 3.0 -> "facil"
                distanceKm <= 8.0 -> "media"
                else -> "dificil"
            }

            val raceRef = firestore.collection("races").document()

            val race = Race(
                id = raceRef.id,
                name = name.trim(),
                description = description.trim(),
                createdBy = uid,
                difficulty = difficulty,
                estimatedDistanceKm = distanceKm,
                checkpointCount = checkpoints.size,
                isPublic = isPublic,
                createdAt = Timestamp.now(),
                photoUrl = photoUrl
            )

            raceRef.set(race).await()

            checkpoints.forEachIndexed { index, checkpoint ->
                raceRef.collection("checkpoints")
                    .document()
                    .set(checkpoint.copy(order = index + 1))
                    .await()
            }

            Result.success(raceRef.id)
        } catch (e: Exception) {
            Log.e("RaceRepository", "Error creating race", e)
            Result.failure(e)
        }
    }

    suspend fun getPublicRaces(): List<Race> {
        return try {
            val snapshot = firestore.collection("races")
                .whereEqualTo("public", true)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                try {
                    doc.toObject(Race::class.java)?.copy(id = doc.id)
                } catch (e: Exception) {
                    Log.e("RaceRepository", "Error mapping Race document ${doc.id}", e)
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("RaceRepository", "Error getting public races", e)
            emptyList()
        }
    }

    suspend fun getRaceById(raceId: String): Race? {
        return try {
            firestore.collection("races").document(raceId).get().await().toObject(Race::class.java)?.copy(id = raceId)
        } catch (e: Exception) {
            Log.e("RaceRepository", "Error getting race by id $raceId", e)
            null
        }
    }

    suspend fun getUserActiveSessions(): List<RaceSession> {
        return try {
            val uid = auth.currentUser?.uid ?: return emptyList()

            val snapshot = firestore.collection("race_sessions")
                .whereArrayContains("participantIds", uid)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                try {
                    val session = doc.toObject(RaceSession::class.java)
                    if (session != null && session.status != "finished") {
                        session.id = doc.id
                        session
                    } else null
                } catch (e: Exception) {
                    Log.e("RaceRepository", "Error mapping RaceSession ${doc.id}", e)
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("RaceRepository", "Error getting user active sessions", e)
            emptyList()
        }
    }

    /**
     * Devuelve TODAS las sesiones del usuario (activas, lobby y finalizadas).
     * Usado por HistorialCarrerasScreen.
     */
    suspend fun getAllUserSessions(): List<RaceSession> {
        return try {
            val uid = auth.currentUser?.uid ?: return emptyList()

            val snapshot = firestore.collection("race_sessions")
                .whereArrayContains("participantIds", uid)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                try {
                    doc.toObject(RaceSession::class.java)?.also { it.id = doc.id }
                } catch (e: Exception) {
                    Log.e("RaceRepository", "Error mapping RaceSession ${doc.id}", e)
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("RaceRepository", "Error getting all user sessions", e)
            emptyList()
        }
    }

    suspend fun joinRaceSession(sessionId: String): Result<Unit> {
        return try {
            val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Usuario no autenticado"))

            val activeSessions = getUserActiveSessions()
            if (activeSessions.any { it.id != sessionId }) {
                return Result.failure(Exception("No puedes unirte. Ya participas en otra carrera activa."))
            }

            firestore.collection("race_sessions")
                .document(sessionId)
                .update(
                    "participants.$uid", mapOf(
                        "joinedAt" to Timestamp.now(),
                        "completedAt" to null,
                        "position" to null,
                        "checkpointsDone" to emptyList<String>()
                    ),
                    "participantIds", FieldValue.arrayUnion(uid)
                )
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("RaceRepository", "Error joining race session $sessionId", e)
            Result.failure(e)
        }
    }

    suspend fun findOrJoinLobby(race: Race): Result<String> {
        return try {
            val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Usuario no autenticado"))

            val existingLobby = firestore.collection("race_sessions")
                .whereEqualTo("raceId", race.id)
                .whereEqualTo("status", "lobby")
                .limit(1)
                .get()
                .await()
                .documents
                .firstOrNull()

            if (existingLobby != null) {
                joinRaceSession(existingLobby.id).fold(
                    onSuccess = { Result.success(existingLobby.id) },
                    onFailure = { Result.failure(it) }
                )
            } else {
                createLobby(race)
            }
        } catch (e: Exception) {
            Log.e("RaceRepository", "Error finding or joining lobby", e)
            Result.failure(e)
        }
    }

    suspend fun createLobby(race: Race): Result<String> {
        return try {
            val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Usuario no autenticado"))

            val activeSessions = getUserActiveSessions()
            if (activeSessions.isNotEmpty()) {
                return Result.failure(Exception("No puedes crear una sesión si ya participas en otra carrera activa."))
            }

            val sessionRef = firestore.collection("race_sessions").document()

            val sessionData = hashMapOf(
                "raceId" to race.id,
                "raceName" to race.name,
                "status" to "lobby",
                "createdBy" to uid,
                "participantIds" to listOf(uid),
                "participants" to mapOf(
                    uid to mapOf(
                        "joinedAt" to Timestamp.now(),
                        "completedAt" to null,
                        "position" to null,
                        "checkpointsDone" to emptyList<String>()
                    )
                ),
                "startedAt" to null,
                "endedAt" to null,
                "winnerUid" to "",
                "groupChatId" to "",
                "createdAt" to Timestamp.now()
            )

            sessionRef.set(sessionData).await()

            Result.success(sessionRef.id)
        } catch (e: Exception) {
            Log.e("RaceRepository", "Error creating lobby", e)
            Result.failure(e)
        }
    }

    suspend fun startRaceSession(sessionId: String): Result<Unit> {
        return try {
            val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Usuario no autenticado"))

            val ref = firestore.collection("race_sessions").document(sessionId)
            val snap = ref.get().await()

            val createdBy = snap.getString("createdBy") ?: ""
            val status = snap.getString("status") ?: ""
            val participants = snap.get("participants") as? Map<*, *>
            val participantsCount = participants?.size ?: 0

            if (uid != createdBy) return Result.failure(Exception("Solo el creador puede iniciar"))
            if (status != "lobby") return Result.failure(Exception("La carrera ya no está en lobby"))
            if (participantsCount < 2) return Result.failure(Exception("Se necesitan mínimo 2 participantes"))

            ref.update(
                mapOf(
                    "status" to "active",
                    "startedAt" to Timestamp.now()
                )
            ).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("RaceRepository", "Error starting race session", e)
            Result.failure(e)
        }
    }

    suspend fun finishRaceSession(sessionId: String): Result<Unit> {
        return try {
            val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Usuario no autenticado"))

            val ref = firestore.collection("race_sessions").document(sessionId)
            val snap = ref.get().await()

            val createdBy = snap.getString("createdBy") ?: ""

            if (uid != createdBy) return Result.failure(Exception("Solo el creador puede finalizar la carrera"))

            ref.update(
                mapOf(
                    "status" to "finished",
                    "endedAt" to Timestamp.now()
                )
            ).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("RaceRepository", "Error finishing race session", e)
            Result.failure(e)
        }
    }

    suspend fun getCheckpoints(raceId: String): List<Checkpoint> {
        return try {
            firestore.collection("races")
                .document(raceId)
                .collection("checkpoints")
                .get()
                .await()
                .documents
                .mapNotNull { it.toObject(Checkpoint::class.java)?.copy(id = it.id) }
                .sortedBy { it.order }
        } catch (e: Exception) {
            Log.e("RaceRepository", "Error getting checkpoints for race $raceId", e)
            emptyList()
        }
    }

    suspend fun validateCheckpoint(
        sessionId: String,
        checkpointId: String
    ): Result<Unit> {
        return try {
            val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Usuario no autenticado"))

            firestore.collection("race_sessions")
                .document(sessionId)
                .update(
                    "participants.$uid.checkpointsDone", FieldValue.arrayUnion(checkpointId),
                    "participants.$uid.lastCheckpointAt", FieldValue.serverTimestamp()
                )
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("RaceRepository", "Error validating checkpoint", e)
            Result.failure(e)
        }
    }

    suspend fun uploadCheckpointPhoto(
        sessionId: String,
        checkpointId: String,
        imageUri: Uri
    ): Result<String> {
        return try {
            val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Usuario no autenticado"))
            val fileName = "sessions/$sessionId/checkpoints/${checkpointId}_$uid.jpg"
            val ref = storage.reference.child(fileName)

            ref.putFile(imageUri).await()
            val downloadUrl = ref.downloadUrl.await().toString()

            firestore.collection("race_sessions")
                .document(sessionId)
                .update(
                    "participants.$uid.checkpointsDone", FieldValue.arrayUnion(checkpointId),
                    "participants.$uid.lastCheckpointAt", FieldValue.serverTimestamp()
                )
                .await()

            Result.success(downloadUrl)
        } catch (e: Exception) {
            Log.e("RaceRepository", "Error uploading checkpoint photo", e)
            Result.failure(e)
        }
    }

    private fun calculateTotalDistanceKm(checkpoints: List<Checkpoint>): Double {
        if (checkpoints.size < 2) return 0.0
        var total = 0.0

        for (i in 0 until checkpoints.size - 1) {
            val a = checkpoints[i].coordinates
            val b = checkpoints[i + 1].coordinates
            total += haversineKm(a.latitude, a.longitude, b.latitude, b.longitude)
        }

        return total
    }

    private fun haversineKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a =
            sin(dLat / 2).pow(2.0) +
                    cos(Math.toRadians(lat1)) *
                    cos(Math.toRadians(lat2)) *
                    sin(dLon / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    suspend fun leaveRaceSession(sessionId: String): Result<Unit> {
        return try {
            val uid = auth.currentUser?.uid
                ?: return Result.failure(Exception("Usuario no autenticado"))

            firestore.collection("race_sessions")
                .document(sessionId)
                .update(
                    mapOf(
                        "participants.$uid" to FieldValue.delete(),
                        "participantIds" to FieldValue.arrayRemove(uid)
                    )
                )
                .await()

            FirebaseDatabase.getInstance(
                "https://linkup-99296-default-rtdb.firebaseio.com/"
            ).reference
                .child("live_positions")
                .child(sessionId)
                .child(uid)
                .removeValue()
                .await()

            Result.success(Unit)

        } catch (e: Exception) {
            Log.e("RaceRepository", "Error leaving race session", e)
            Result.failure(e)
        }
    }
}
