package com.rodando.app.domain.model

/** Resultado del cálculo de ganancia del mes (RF04). */
data class ResumenGanancia(
    val kmComputables: Double,
    val kmPagados: Double,
    val diasConViajes: Int,
    val viajesComputables: Int,
    val viajesPendientes: Int,
    val fijoCobrado: Double,
    val totalEstimado: Double
)
