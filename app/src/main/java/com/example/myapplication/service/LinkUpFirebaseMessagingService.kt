package com.example.myapplication.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.myapplication.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class LinkUpFirebaseMessagingService : FirebaseMessagingService() {

    // Se llama cuando el token FCM se renueva
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.i("FCM", "Nuevo token: $token")

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            // LinkUp usa Firestore (no Realtime Database como en el taller)
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUser.uid)
                .update("fcmToken", token)
        }
    }

    // Se llama cuando llega una notificación push con la app en primer plano
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val title = message.data["title"] ?: "LinkUp"
        val body = message.data["body"] ?: "Tienes una nueva notificación"

        showNotification(title, body)
    }

    private fun showNotification(title: String, body: String) {
        val channelId = "linkup_channel"
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Crear el canal (requerido en Android 8+)
        val channel = NotificationChannel(
            channelId,
            "Notificaciones LinkUp",
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(channel)

        // Intent para abrir la app al tocar la notificación
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // cámbialo por el ícono de LinkUp
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}