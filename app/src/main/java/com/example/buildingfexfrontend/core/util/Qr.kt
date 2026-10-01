package com.example.buildingfexfrontend.core.util

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

/** Generates black-on-white QR code bitmaps (zxing) for shareable invitations. */
object Qr {

    /** QR [Bitmap] for [text] at [sizePx], or null when [text] is blank. */
    fun bitmap(text: String, sizePx: Int = 640): Bitmap? {
        if (text.isBlank()) return null
        return try {
            val hints = mapOf(
                EncodeHintType.CHARACTER_SET to "UTF-8",
                EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,
                EncodeHintType.MARGIN to 1,
            )
            val matrix = QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, sizePx, sizePx, hints)
            val width = matrix.width
            val height = matrix.height
            val pixels = IntArray(width * height) { i ->
                if (matrix.get(i % width, i / width)) BLACK else WHITE
            }
            Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
                setPixels(pixels, 0, width, 0, 0, width, height)
            }
        } catch (e: Exception) {
            null
        }
    }

    private const val BLACK: Int = -0x1000000
    private const val WHITE: Int = -0x1
}
