package com.example.myapplication.utils

import android.app.Activity
import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.util.Log

class NFCManager {

    companion object {
        private const val TAG = "NFCManager"
    }

    /**
     * Genera el payload NFC usando el mismo formato que el sistema QR.
     * linkup://addfriend?uid=UID
     */
    fun createPayload(uid: String): String =
        QRGenerator.createQrContent(uid)

    fun enableReaderMode(activity: Activity, onUidDetected: (String) -> Unit) {
        val adapter = NfcAdapter.getDefaultAdapter(activity)
        if (adapter == null) {
            Log.w(TAG, "NFC no disponible en este dispositivo")
            return
        }
        if (!adapter.isEnabled) {
            Log.w(TAG, "NFC desactivado por el usuario")
            return
        }

        Log.d(TAG, "ReaderMode activado")
        adapter.enableReaderMode(
            activity,
            { tag ->
                Log.d(TAG, "Tag detectado: ${tag.id.joinToString { "%02x".format(it) }}")
                val uid = parsePayload(tag)
                if (uid != null) {
                    Log.d(TAG, "UID extraído correctamente: $uid")
                    onUidDetected(uid)
                } else {
                    Log.w(TAG, "Tag detectado pero payload no parseable o formato incorrecto")
                }
            },
            // FIX #1: Se eliminó FLAG_READER_SKIP_NDEF_CHECK — ese flag impedía
            // que el sistema procesara el contenido NDEF, rompiendo parsePayload().
            NfcAdapter.FLAG_READER_NFC_A or
                    NfcAdapter.FLAG_READER_NFC_B or
                    NfcAdapter.FLAG_READER_NFC_F or
                    NfcAdapter.FLAG_READER_NFC_V,
            null
        )
    }

    fun disableReaderMode(activity: Activity) {
        Log.d(TAG, "ReaderMode desactivado")
        NfcAdapter.getDefaultAdapter(activity)?.disableReaderMode(activity)
    }

    private fun parsePayload(tag: Tag): String? {
        val ndef = Ndef.get(tag) ?: run {
            Log.w(TAG, "Tag no tiene formato NDEF — no es un tag de LinkUp")
            return null
        }
        return try {
            ndef.connect()

            // FIX #2: Se intenta cachedNdefMessage primero (no requiere que el tag
            // siga presente y es más rápido), con fallback a lectura activa.
            val message = ndef.cachedNdefMessage ?: ndef.ndefMessage

            try { ndef.close() } catch (_: Exception) {}

            val record = message?.records?.firstOrNull() ?: return null

            // FIX #3: Los records de tipo Text llevan un header de idioma al inicio
            // del payload (ej: \x02en) que rompe Uri.parse(). Se stripea correctamente.
            val content = record.toUri()?.toString()
                ?: run {
                    val rawPayload = record.payload
                    if (rawPayload.isNotEmpty()) {
                        val langLength = rawPayload[0].toInt() and 0x3F
                        val offset = 1 + langLength
                        if (offset < rawPayload.size) {
                            String(rawPayload, offset, rawPayload.size - offset, Charsets.UTF_8)
                        } else {
                            String(rawPayload, Charsets.UTF_8)
                        }
                    } else ""
                }

            Log.d(TAG, "Payload raw leído: $content")
            // InviteUtils valida que sea linkup://addfriend?uid=X
            InviteUtils.extractUidFromLink(content)
        } catch (e: Exception) {
            Log.e(TAG, "Error al leer payload NDEF", e)
            try { ndef.close() } catch (_: Exception) {}
            null
        }
    }

    fun parseFromIntent(intent: Intent): String? {
        @Suppress("DEPRECATION")
        val rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
            ?: return null
        if (rawMessages.isEmpty()) return null
        val message = rawMessages[0] as NdefMessage
        val record = message.records.firstOrNull() ?: return null

        // Mismo fix #3 aplicado al flujo por Intent
        val content = record.toUri()?.toString()
            ?: run {
                val rawPayload = record.payload
                if (rawPayload.isNotEmpty()) {
                    val langLength = rawPayload[0].toInt() and 0x3F
                    val offset = 1 + langLength
                    if (offset < rawPayload.size) {
                        String(rawPayload, offset, rawPayload.size - offset, Charsets.UTF_8)
                    } else {
                        String(rawPayload, Charsets.UTF_8)
                    }
                } else ""
            }

        Log.d(TAG, "parseFromIntent payload: $content")
        return InviteUtils.extractUidFromLink(content)
    }

    fun isAvailable(activity: Activity): Boolean =
        NfcAdapter.getDefaultAdapter(activity) != null

    fun isEnabled(activity: Activity): Boolean =
        NfcAdapter.getDefaultAdapter(activity)?.isEnabled == true
}