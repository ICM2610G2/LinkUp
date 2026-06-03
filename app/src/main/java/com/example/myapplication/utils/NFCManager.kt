package com.example.myapplication.utils

import android.app.Activity
import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.util.Log

/**
 * NFCManager (REFACCIÓN FINAL LIMPIA)
 * 
 * Utility pura responsable de la lógica NFC mediante ReaderMode.
 * Sigue las reglas estrictas: NO Beam, NO HCE, NO servicios.
 */
class NFCManager {

    /**
     * Fuente única de verdad para el payload NFC.
     * Reutiliza el formato exacto del sistema QR.
     */
    fun createPayload(uid: String): String {
        return QRGenerator.createQrContent(uid)
    }

    /**
     * Activa el modo lector (Receptor).
     * Usa flags específicos para detectar tags NDEF estándar.
     */
    fun enableReaderMode(activity: Activity, onUidDetected: (String) -> Unit) {
        val adapter = NfcAdapter.getDefaultAdapter(activity)
        if (adapter == null) {
            Log.w("NFCManager", "NFC no disponible en este dispositivo")
            return
        }

        adapter.enableReaderMode(
            activity,
            { tag ->
                parsePayload(tag)?.let { uid ->
                    onUidDetected(uid)
                }
            },
            NfcAdapter.FLAG_READER_NFC_A or
            NfcAdapter.FLAG_READER_NFC_B or
            NfcAdapter.FLAG_READER_NFC_F or
            NfcAdapter.FLAG_READER_NFC_V or
            NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
            null
        )
    }

    /**
     * Desactiva el ReaderMode para liberar el hardware.
     */
    fun disableReaderMode(activity: Activity) {
        NfcAdapter.getDefaultAdapter(activity)?.disableReaderMode(activity)
    }

    /**
     * Extrae el UID de un Tag detectado.
     * Utiliza InviteUtils.extractUidFromLink para mantener compatibilidad con QR.
     */
    private fun parsePayload(tag: Tag): String? {
        val ndef = Ndef.get(tag) ?: return null
        return try {
            ndef.connect()
            val message = ndef.ndefMessage
            ndef.close()
            
            val record = message?.records?.firstOrNull() ?: return null
            val content = record.toUri()?.toString() ?: String(record.payload)
            InviteUtils.extractUidFromLink(content)
        } catch (e: Exception) {
            Log.e("NFCManager", "Error al leer payload NDEF", e)
            null
        }
    }

    /**
     * Parsea el UID desde un Intent (fallback para onNewIntent).
     */
    fun parseFromIntent(intent: Intent): String? {
        @Suppress("DEPRECATION")
        val rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
        if (rawMessages != null && rawMessages.isNotEmpty()) {
            val message = rawMessages[0] as NdefMessage
            val record = message.records.firstOrNull() ?: return null
            val content = record.toUri()?.toString() ?: String(record.payload)
            return InviteUtils.extractUidFromLink(content)
        }
        return null
    }

    fun isAvailable(activity: Activity): Boolean {
        return NfcAdapter.getDefaultAdapter(activity) != null
    }

    fun isEnabled(activity: Activity): Boolean {
        return NfcAdapter.getDefaultAdapter(activity)?.isEnabled == true
    }
}
