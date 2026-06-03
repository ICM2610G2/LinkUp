package com.example.myapplication.utils

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix

/**
 * SISTEMA QR INDEPENDIENTE.
 * NO reutilizar esta lógica para códigos temporales de amistad.
 * El sistema QR es independiente y se basa en datos permanentes (UID).
 * 
 * Este sistema está desacoplado de InviteUtils y de los códigos temporales.
 */
object QRGenerator {
    private const val QR_SCHEME = "linkup"
    private const val QR_HOST = "addfriend"

    /**
     * Genera el contenido del QR basado en el UID permanente.
     */
    fun createQrContent(uid: String): String {
        return "$QR_SCHEME://$QR_HOST?uid=$uid"
    }

    fun generate(content: String, size: Int = 512): Bitmap {
        val bitMatrix: BitMatrix = MultiFormatWriter().encode(
            content,
            BarcodeFormat.QR_CODE,
            size,
            size
        )
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
            }
        }
        return bitmap
    }
}