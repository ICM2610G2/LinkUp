package com.example.myapplication.utils

import android.net.Uri
import android.util.Log

/**
 * UTILIDADES DE INVITACIÓN.
 * 
 * ATENCIÓN: El sistema QR es INDEPENDIENTE. 
 * Las funciones marcadas para QR NO deben ser modificadas por cambios en el sistema de códigos temporales.
 */
object InviteUtils {
    private const val TAG = "InviteUtils"
    private const val QR_SCHEME = "linkup"
    private const val QR_HOST = "addfriend"

    /**
     * EXCLUSIVO PARA QR PERSONAL.
     * Genera el contenido del QR basado en el UID permanente del usuario.
     * NO usar para invitaciones por código temporal.
     */
    fun createQrLink(uid: String): String {
        return "$QR_SCHEME://$QR_HOST?uid=$uid"
    }

    /**
     * Extrae el UID de un enlace QR.
     * Sistema independiente de los códigos de amistad temporales.
     */
    fun extractUidFromLink(link: String?): String? {
        if (link == null) return null
        return try {
            val uri = Uri.parse(link)
            val uid = uri.getQueryParameter("uid")
            if (uri.scheme == QR_SCHEME && uri.host == QR_HOST && !uid.isNullOrEmpty()) {
                uid
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parseando link QR", e)
            null
        }
    }
}