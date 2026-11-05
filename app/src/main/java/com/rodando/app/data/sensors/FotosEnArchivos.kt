package com.rodando.app.data.sensors

import android.content.Context
import com.rodando.app.domain.repository.AlmacenDeFotos
import java.io.File
import java.util.UUID

/**
 * Guarda las fotos en la carpeta privada de la app: no hace falta permiso de almacenamiento
 * y se borran si se desinstala la app. Quedan en el teléfono hasta que se envíen (etapa 5).
 */
class FotosEnArchivos(context: Context) : AlmacenDeFotos {

    private val carpeta = File(context.filesDir, "fotos").apply { mkdirs() }

    override fun nuevaRuta(): String = File(carpeta, "vinilo-${UUID.randomUUID()}.jpg").absolutePath

    override fun borrar(ruta: String) {
        val archivo = File(ruta)
        // Solo se borra lo que está dentro de la carpeta de fotos de la app.
        if (archivo.parentFile?.absolutePath == carpeta.absolutePath) archivo.delete()
    }
}
