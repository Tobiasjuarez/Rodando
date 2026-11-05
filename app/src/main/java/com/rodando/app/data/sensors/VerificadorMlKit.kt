package com.rodando.app.data.sensors

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.rodando.app.domain.repository.VerificadorDeVinilo
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Busca el código QR del vinilo en la foto con ML Kit. Corre en el teléfono, sin conexión:
 * el modelo viene dentro de la app.
 */
class VerificadorMlKit(private val context: Context) : VerificadorDeVinilo {

    private val lector = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE).build()
    )

    override suspend fun reconoce(rutaFoto: String, codigoEsperado: String): Boolean {
        // fromFilePath lee la orientación EXIF, así que la foto llega derecha.
        val imagen = InputImage.fromFilePath(context, Uri.fromFile(File(rutaFoto)))
        val codigos = suspendCancellableCoroutine<List<Barcode>> { continuacion ->
            lector.process(imagen)
                .addOnSuccessListener { continuacion.resume(it) }
                .addOnFailureListener { continuacion.resumeWithException(it) }
        }
        return codigos.any { it.rawValue?.trim().equals(codigoEsperado.trim(), ignoreCase = true) }
    }
}
