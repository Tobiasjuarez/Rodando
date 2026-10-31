package com.rodando.app.data.sensors

import com.rodando.app.domain.repository.FuenteUbicacion
import com.rodando.app.domain.repository.RegistroDeRecorrido
import com.rodando.app.domain.repository.ViajeRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Mientras hay un viaje en curso, guarda en Room cada posición que entrega la fuente.
 * Vive en el alcance de la app, no de una pantalla: el recorrido se sigue registrando
 * aunque el conductor cambie de pantalla.
 */
class RegistradorDeViaje(
    private val repositorio: ViajeRepository,
    private val fuente: FuenteUbicacion,
    private val alcance: CoroutineScope
) : RegistroDeRecorrido {
    private var trabajo: Job? = null
    private var viajeActual: String? = null

    val registrando: Boolean get() = trabajo?.isActive == true

    override fun iniciar(viajeId: String) {
        if (registrando && viajeActual == viajeId) return
        detener()
        viajeActual = viajeId
        trabajo = alcance.launch {
            fuente.ubicaciones().collect { punto -> repositorio.registrarPunto(viajeId, punto) }
        }
    }

    override fun detener() {
        trabajo?.cancel()
        trabajo = null
        viajeActual = null
    }
}
