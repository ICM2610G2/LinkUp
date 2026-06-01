package com.example.myapplication.utils

import android.net.Uri
import android.util.Log

object InviteUtils {
    private const val TAG = "InviteUtils"
    private const val QR_SCHEME = "linkup"
    private const val QR_HOST = "addfriend"
    private const val SHARE_HOST = "linkup.app"

    private const val CUSTOM_SCHEME_URL = "linkup://addfriend"
    private const val HTTPS_BASE_URL = "https://linkup.app/addfriend"

    /**
     * Genera un enlace con esquema personalizado (linkup://addfriend?uid=XXXX)
     * Recomendado para Códigos QR y NFC para apertura directa e instantánea.
     */
    fun createQrLink(uid: String): String {
        return "$QR_SCHEME://$QR_HOST?uid=$uid"
    }

    /**
     * Alias de createQrLink para compatibilidad con NFCManager y otros componentes.
     */
    fun createFriendLink(uid: String): String {
        return createQrLink(uid)
    }

    /**
     * Genera un enlace HTTPS estándar (https://linkup.app/addfriend?uid=XXXX)
     * Recomendado para compartir por aplicaciones de mensajería (WhatsApp, Telegram)
     * para que sea reconocido como un enlace clickable.
     */
    fun createShareableLink(uid: String): String {
        return "https://$SHARE_HOST/addfriend?uid=$uid"
    }

    /**
     * Alias de createShareableLink para compatibilidad.
     */
    fun createHttpsFriendLink(uid: String): String {
        return createShareableLink(uid)
    }

    /**
     * Extrae el UID de un enlace, soportando tanto linkup:// como https://
     * Es flexible con los parámetros y el path.
     */
    fun extractUidFromLink(link: String?): String? {
        if (link == null) return null
        Log.d(TAG, "Intentando extraer UID de: $link")
        
        return try {
            val uri = Uri.parse(link)
            val uid = uri.getQueryParameter("uid")
            
            val isLinkupScheme = uri.scheme == QR_SCHEME && uri.host == QR_HOST
            val isHttpsScheme = uri.scheme == "https" && uri.host == SHARE_HOST && uri.path?.contains("addfriend") == true
            
            if ((isLinkupScheme || isHttpsScheme) && !uid.isNullOrEmpty()) {
                Log.d(TAG, "UID extraído exitosamente: $uid")
                uid
            } else {
                Log.w(TAG, "Enlace no reconocido como invitación válida. Scheme: ${uri.scheme}, Host: ${uri.host}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error crítico al parsear el enlace: $link", e)
            null
        }
    }
}