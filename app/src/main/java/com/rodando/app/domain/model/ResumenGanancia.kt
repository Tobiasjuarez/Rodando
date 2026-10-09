package com.rodando.app.domain.model

/** Resultado del cálculo de ganancia del mes (RF04). */
data class ResumenGanancia(
    val kmComputables: Double,
    val kmPagados: Double,
    val diasConViajes: Int,
    val viajesComputables: Int,
    val viajesPendientes: Int,
    val fijoCobrado: Double,
    val totalEstimado: Double,
    /** Avance hacia los km mínimos para cobrar el fijo entero. */
    val objetivoKm: ObjetivoMensual,
    /** Avance hacia los días con viajes mínimos para cobrar el fijo entero. */
    val objetivoDias: ObjetivoMensual,
    /** Km que todavía se pagan este mes antes de llegar al tope. */
    val kmPagosRestantes: Double
)
