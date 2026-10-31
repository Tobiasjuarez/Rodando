package com.rodando.app.data.sensors

import com.rodando.app.domain.repository.FuenteUbicacion
import com.rodando.app.domain.repository.RegistroDeRecorrido
import com.rodando.app.domain.repository.ViajeRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

/**
 * Mientras hay un viaje en curso, guarda en Room cada posición que entrega la fuente.
 * Vive en el alcance de la app, no de una pantalla: el recorrido se sigue registrando
 * aunque el conductor cambie de pantalla.
 *
 * Descarta las posiciones imprecisas (por ejemplo, dentro de un túnel o entre edificios altos)
 * para que no sumen km falsos.
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
            fuente.ubicaciones()
                .filter { it.precisionMetros <= PRECISION_MAXIMA_METROS }
                .collect { punto -> repositorio.registrarPunto(viajeId, punto) }
        }
    }

    override fun detener() {
        trabajo?.cancel()
        trabajo = null
        viajeActual = null
    }

    companion object {
        /** Posiciones con un margen de error mayor no se guardan. */
        const val PRECISION_MAXIMA_METROS = 50f
    }
}
