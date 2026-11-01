package com.rodando.app.domain.model

/**
 * Resultado del cálculo de ganancia del mes (RF04).
 *
 * - [confirmado]: solo viajes aprobados por el equipo. Es lo que se va a cobrar seguro.
 * - [estimado]: aprobados más los que esperan aprobación. Los viajes en revisión no suman
 *   hasta que se aprueben.
 * El resto de los campos describe el estimado.
 */
data class ResumenGanancia(
    val confirmado: Double,
    val estimado: Double,
    val fijo: Double,
    val montoKm: Double,
    val kmComputables: Double,
    val kmPagados: Double,
    val diasConViajes: Int,
    val viajesAprobados: Int,
    val viajesPorAprobar: Int,
    val viajesEnRevision: Int,
    /** Rechazados o demasiado cortos: quedan registrados pero no se pagan. */
    val viajesSinPago: Int,
    /** Finalizados que todavía no llegaron al servidor. */
    val viajesSinEnviar: Int,
    /** Avance hacia los días con viajes mínimos para cobrar el fijo entero. */
    val objetivoDias: ObjetivoMensual,
    /** Km que todavía se pagan este mes antes de llegar al tope. */
    val kmPagosRestantes: Double
)
