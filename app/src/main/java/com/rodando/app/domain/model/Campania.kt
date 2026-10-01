package com.rodando.app.domain.model

/** Campaña publicitaria asignada al conductor. Viene de la API y se guarda localmente. */
data class Campania(
    val id: String,
    val marca: String,
    val fechaInicio: Long,
    val fechaFin: Long,
    val tarifaPorKm: Double
)
