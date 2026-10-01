package com.rodando.app.domain.usecase

import com.rodando.app.domain.model.EstadoViaje
import com.rodando.app.domain.model.ResumenGanancia
import com.rodando.app.domain.model.Viaje

/**
 * Regla de negocio de RF04: ganancia estimada = km computables x tarifa de la campaña.
 *
 * Un viaje es computable si ya finalizó (pendiente de envío o sincronizado)
 * y recorrió al menos [DISTANCIA_MINIMA_KM].
 */
class CalcularGananciaUseCase {

    operator fun invoke(viajes: List<Viaje>, tarifaPorKm: Double): ResumenGanancia {
        val computables = viajes.filter { esComputable(it) }
        val km = computables.sumOf { it.distanciaKm }
        return ResumenGanancia(
            kmComputables = km,
            viajesComputables = computables.size,
            viajesPendientes = computables.count { it.estado == EstadoViaje.PENDIENTE },
            totalEstimado = km * tarifaPorKm
        )
    }

    fun esComputable(viaje: Viaje): Boolean =
        viaje.estado in ESTADOS_FINALIZADOS && viaje.distanciaKm >= DISTANCIA_MINIMA_KM

    companion object {
        /** Umbral a confirmar por el equipo (ver preentrega, RF02). */
        const val DISTANCIA_MINIMA_KM = 0.5

        private val ESTADOS_FINALIZADOS = setOf(EstadoViaje.PENDIENTE, EstadoViaje.SINCRONIZADO)
    }
}
