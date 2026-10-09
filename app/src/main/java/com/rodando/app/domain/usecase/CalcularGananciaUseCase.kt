package com.rodando.app.domain.usecase

import com.rodando.app.domain.model.EstadoViaje
import com.rodando.app.domain.model.ObjetivoMensual
import com.rodando.app.domain.model.ResumenGanancia
import com.rodando.app.domain.model.Viaje
import com.rodando.app.domain.model.Vinilo
import java.time.Instant
import java.time.ZoneId

/**
 * Regla de negocio de RF04: ganancia estimada del mes.
 *
 * ganancia = fijo x min(km / kmMinimo, días / diasMinimos, 1) + tarifa por km x min(km, kmTope)
 *
 * Un viaje es computable si ya finalizó (pendiente de envío o sincronizado)
 * y recorrió al menos [DISTANCIA_MINIMA_KM]. Un día cuenta si tuvo al menos un viaje computable.
 * Recibe los viajes de un solo mes.
 */
class CalcularGananciaUseCase(
    private val zona: ZoneId = ZoneId.of("America/Argentina/Buenos_Aires")
) {

    operator fun invoke(viajes: List<Viaje>, vinilo: Vinilo): ResumenGanancia {
        val computables = viajes.filter { esComputable(it) }
        val km = computables.sumOf { it.distanciaKm }
        val dias = computables.map { Instant.ofEpochMilli(it.inicio).atZone(zona).toLocalDate() }.distinct().size

        val proporcionFijo = minOf(km / vinilo.kmMinimo, dias.toDouble() / vinilo.diasMinimos, 1.0)
        val fijo = vinilo.fijoMensual * proporcionFijo
        val kmPagados = minOf(km, vinilo.kmTope)

        return ResumenGanancia(
            kmComputables = km,
            kmPagados = kmPagados,
            diasConViajes = dias,
            viajesComputables = computables.size,
            viajesPendientes = computables.count { it.estado == EstadoViaje.PENDIENTE },
            fijoCobrado = fijo,
            totalEstimado = fijo + kmPagados * vinilo.tarifaPorKm,
            objetivoKm = ObjetivoMensual(actual = km, meta = vinilo.kmMinimo),
            objetivoDias = ObjetivoMensual(actual = dias.toDouble(), meta = vinilo.diasMinimos.toDouble()),
            kmPagosRestantes = (vinilo.kmTope - km).coerceAtLeast(0.0)
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
