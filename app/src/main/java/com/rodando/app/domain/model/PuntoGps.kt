package com.rodando.app.domain.model

/** Una posición registrada durante un viaje en curso. */
data class PuntoGps(
    val latitud: Double,
    val longitud: Double,
    val timestamp: Long,
    val precisionMetros: Float
)
