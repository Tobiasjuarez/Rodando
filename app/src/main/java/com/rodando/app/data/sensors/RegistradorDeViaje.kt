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
 * y las viejas: al encender el GPS, Android puede entregar primero la última ubicación conocida,
 * que puede ser de horas antes y de otro lugar.
 */
class RegistradorDeViaje(
    private val repositorio: ViajeRepository,
    private val fuente: FuenteUbicacion,
    private val alcance: CoroutineScope
) : RegistroDeRecorrido {
    private var trabajo: Job? = null
    private var viajeActual: String? = null

    val registrando: Boolean get() = trabajo?.isActive == true

    override fun iniciar(viajeId: String) = iniciar(viajeId, desde = Long.MIN_VALUE)

    /** Registra solo las posiciones tomadas desde [desde] (la hora de inicio del viaje). */
    fun iniciar(viajeId: String, desde: Long) {
        if (registrando && viajeActual == viajeId) return
        detener()
        viajeActual = viajeId
        val minimo = if (desde == Long.MIN_VALUE) desde else desde - TOLERANCIA_MS
        trabajo = alcance.launch {
            fuente.ubicaciones()
                .filter { it.precisionMetros <= PRECISION_MAXIMA_METROS && it.timestamp >= minimo }
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

        /** Margen para aceptar una posición tomada apenas antes de tocar "Iniciar". */
        const val TOLERANCIA_MS = 30_000L
    }
}
