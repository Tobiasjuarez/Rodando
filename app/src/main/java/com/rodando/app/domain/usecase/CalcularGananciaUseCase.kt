package com.rodando.app.domain.usecase

import com.rodando.app.domain.model.EstadoEnvio
import com.rodando.app.domain.model.EstadoVerificacion
import com.rodando.app.domain.model.ObjetivoMensual
import com.rodando.app.domain.model.ResumenGanancia
import com.rodando.app.domain.model.Viaje
import com.rodando.app.domain.model.Vinilo
import java.time.Instant
import java.time.ZoneId

/**
 * Regla de negocio de RF04: ganancia del mes.
 *
 * ganancia = fijo x min(días / diasMinimos, 1) + suma de (km pagados de cada viaje x su tarifa)
 *
 * - El fijo depende solo de la constancia (días con viajes), no de los km: así no conviene
 *   salir a dar vueltas para llegar a un mínimo.
 * - Cada viaje se paga con la tarifa que tenía al iniciarse.
 * - Se pagan como máximo kmTope km por mes, en orden cronológico.
 * - Se calcula dos veces: con los viajes aprobados (confirmado) y con los aprobados más los
 *   que esperan aprobación (estimado). Recibe los viajes de un solo mes.
 */
class CalcularGananciaUseCase(
    private val zona: ZoneId = ZoneId.of("America/Argentina/Buenos_Aires")
) {

    operator fun invoke(viajes: List<Viaje>, vinilo: Vinilo): ResumenGanancia {
        val finalizados = viajes.filterNot { it.enCurso }
        val confirmado = calcularMonto(finalizados.filter { esAprobado(it) }, vinilo)
        val estimado = calcularMonto(finalizados.filter { cuentaParaEstimado(it) }, vinilo)

        return ResumenGanancia(
            confirmado = confirmado.total,
            estimado = estimado.total,
            fijo = estimado.fijo,
            montoKm = estimado.montoKm,
            kmComputables = estimado.km,
            kmPagados = estimado.kmPagados,
            diasConViajes = estimado.dias,
            viajesAprobados = finalizados.count { esAprobado(it) },
            viajesPorAprobar = finalizados.count { cuentaParaEstimado(it) && !esAprobado(it) },
            viajesEnRevision = finalizados.count { it.verificacion == EstadoVerificacion.EN_REVISION },
            viajesSinPago = finalizados.count {
                it.verificacion == EstadoVerificacion.RECHAZADO || it.verificacion == EstadoVerificacion.NO_COMPUTABLE
            },
            viajesSinEnviar = finalizados.count { it.envio == EstadoEnvio.PENDIENTE },
            objetivoDias = ObjetivoMensual(actual = estimado.dias.toDouble(), meta = vinilo.diasMinimos.toDouble()),
            kmPagosRestantes = (vinilo.kmTope - estimado.kmPagados).coerceAtLeast(0.0)
        )
    }

    /** Finalizado, aprobado o esperando aprobación, y con la distancia mínima. */
    fun cuentaParaEstimado(viaje: Viaje): Boolean =
        !viaje.enCurso &&
            viaje.verificacion in ESTADOS_QUE_SUMAN &&
            viaje.distanciaKm >= DISTANCIA_MINIMA_KM

    /**
     * Lo que suma un viaje por sus km, con su propia tarifa (null si no suma). El fijo es mensual
     * y no se reparte por viaje; tampoco se descuenta el tope, que se aplica al total del mes.
     */
    fun gananciaPorKm(viaje: Viaje): Double? =
        if (cuentaParaEstimado(viaje)) viaje.distanciaKm * viaje.tarifaPorKm else null

    private fun esAprobado(viaje: Viaje) =
        cuentaParaEstimado(viaje) && viaje.verificacion == EstadoVerificacion.APROBADO

    private class Monto(val fijo: Double, val montoKm: Double, val km: Double, val kmPagados: Double, val dias: Int) {
        val total: Double get() = fijo + montoKm
    }

    private fun calcularMonto(viajes: List<Viaje>, vinilo: Vinilo): Monto {
        val dias = viajes.map { Instant.ofEpochMilli(it.inicio).atZone(zona).toLocalDate() }.distinct().size
        val fijo = vinilo.fijoMensual * minOf(dias.toDouble() / vinilo.diasMinimos, 1.0)

        var kmPagados = 0.0
        var montoKm = 0.0
        viajes.sortedBy { it.inicio }.forEach { viaje ->
            val pagables = minOf(viaje.distanciaKm, (vinilo.kmTope - kmPagados).coerceAtLeast(0.0))
            montoKm += pagables * viaje.tarifaPorKm
            kmPagados += pagables
        }
        return Monto(fijo, montoKm, km = viajes.sumOf { it.distanciaKm }, kmPagados = kmPagados, dias = dias)
    }

    companion object {
        /** Umbral a confirmar por el equipo (ver preentrega, RF02). */
        const val DISTANCIA_MINIMA_KM = 0.5

        private val ESTADOS_QUE_SUMAN = setOf(EstadoVerificacion.POR_APROBAR, EstadoVerificacion.APROBADO)
    }
}
