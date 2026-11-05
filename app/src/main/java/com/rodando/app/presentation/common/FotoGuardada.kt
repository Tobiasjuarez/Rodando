package com.rodando.app.presentation.common

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.rodando.app.data.sensors.esFotoReal
import com.rodando.app.data.sensors.gradosSegunExif
import com.rodando.app.data.sensors.muestreoPara
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Lee una foto del vinilo guardada en el teléfono, achicada y derecha, sin bloquear la pantalla.
 * Devuelve null mientras carga, si no hay archivo o si la ruta no es de una foto real.
 */
@Composable
fun rememberFoto(ruta: String?, maximo: Int = 1024): ImageBitmap? {
    val foto by produceState<ImageBitmap?>(initialValue = null, ruta, maximo) {
        value = if (ruta != null && esFotoReal(ruta)) {
            withContext(Dispatchers.IO) { runCatching { leerFoto(ruta, maximo) }.getOrNull() }
        } else {
            null
        }
    }
    return foto
}

private fun leerFoto(ruta: String, maximo: Int): ImageBitmap? {
    if (!File(ruta).exists()) return null
    val medidas = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(ruta, medidas)
    if (medidas.outWidth <= 0 || medidas.outHeight <= 0) return null

    val opciones = BitmapFactory.Options().apply {
        inSampleSize = muestreoPara(medidas.outWidth, medidas.outHeight, maximo)
    }
    val bitmap = BitmapFactory.decodeFile(ruta, opciones) ?: return null

    // La cámara guarda la foto "acostada" y anota en el EXIF cuánto hay que girarla.
    val grados = gradosSegunExif(
        ExifInterface(ruta).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
    )
    val derecha = if (grados == 0f) {
        bitmap
    } else {
        Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, Matrix().apply { postRotate(grados) }, true)
    }
    return derecha.asImageBitmap()
}
