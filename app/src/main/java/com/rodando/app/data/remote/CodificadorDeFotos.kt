package com.rodando.app.data.remote

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.util.Base64
import com.rodando.app.data.sensors.esFotoReal
import com.rodando.app.data.sensors.gradosSegunExif
import com.rodando.app.data.sensors.muestreoPara
import java.io.ByteArrayOutputStream
import java.io.File

/**
 * Prepara una foto del vinilo para enviarla: la achica (lado largo de unos 1.280 px),
 * la endereza según el EXIF y la comprime a JPEG. Queda en unos 150 KB en vez de 3 MB.
 */
object CodificadorDeFotos {

    fun enBase64(ruta: String?): String? {
        if (!esFotoReal(ruta) || !File(ruta!!).exists()) return null
        val medidas = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(ruta, medidas)
        if (medidas.outWidth <= 0) return null
        val bitmap = BitmapFactory.decodeFile(
            ruta,
            BitmapFactory.Options().apply { inSampleSize = muestreoPara(medidas.outWidth, medidas.outHeight, 1_280) }
        ) ?: return null
        val grados = gradosSegunExif(
            ExifInterface(ruta).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        )
        val derecha = if (grados == 0f) {
            bitmap
        } else {
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, Matrix().apply { postRotate(grados) }, true)
        }
        val salida = ByteArrayOutputStream()
        derecha.compress(Bitmap.CompressFormat.JPEG, 70, salida)
        return Base64.encodeToString(salida.toByteArray(), Base64.NO_WRAP)
    }
}
