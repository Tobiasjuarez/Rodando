package com.rodando.app.domain.model

/** Resultado del cálculo de ganancia del mes (RF04). */
data class ResumenGanancia(
    val kmComputables: Double,
    val viajesComputables: Int,
    val viajesPendientes: Int,
    val totalEstimado: Double
)
