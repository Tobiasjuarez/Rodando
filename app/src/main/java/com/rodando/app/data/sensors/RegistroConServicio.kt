package com.rodando.app.data.sensors

import android.content.Context
import com.rodando.app.domain.repository.RegistroDeRecorrido

/**
 * Registro del recorrido a través de [ServicioViaje]. El servicio busca el viaje en curso
 * en Room, por eso no hace falta pasarle el id.
 */
class RegistroConServicio(
    private val context: Context,
    private val registrador: RegistradorDeViaje
) : RegistroDeRecorrido {

    override fun iniciar(viajeId: String) {
        ServicioViaje.iniciar(context)
    }

    override fun detener() {
        // Se corta acá, sin esperar a que el servicio se destruya, para que no entren
        // puntos después de la hora de fin.
        registrador.detener()
        ServicioViaje.detener(context)
    }
}
