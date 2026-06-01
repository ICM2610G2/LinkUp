package com.example.myapplication.utils

import android.app.Activity
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter

class NFCManager(private val activity: Activity) {
    private val nfcAdapter: NfcAdapter? by lazy {
        NfcAdapter.getDefaultAdapter(activity)
    }

    fun isAvailable(): Boolean = nfcAdapter != null

    fun isEnabled(): Boolean = nfcAdapter?.isEnabled == true

    /**
     * Crea un payload NDEF que contiene el enlace de invitación.
     * Utiliza la fuente única de verdad del sistema QR/NFC independiente.
     */
    fun createPayload(uid: String): ByteArray {
        val link = QRGenerator.createQrContent(uid)
        val record = NdefRecord.createUri(link)
        return NdefMessage(arrayOf(record)).toByteArray()
    }

    /**
     * Extrae el UID de un payload NDEF (Uri Record)
     */
    fun parsePayload(data: ByteArray): String? {
        return try {
            val message = NdefMessage(data)
            val record = message.records.firstOrNull() ?: return null
            val uri = record.toUri()?.toString() ?: return null
            InviteUtils.extractUidFromLink(uri)
        } catch (e: Exception) {
            null
        }
    }
}