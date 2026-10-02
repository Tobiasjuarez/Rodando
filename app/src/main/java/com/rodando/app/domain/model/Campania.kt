package com.rodando.app.domain.model

/**
 * Campaña publicitaria asignada al conductor. Viene de la API y se guarda localmente.
 * La tarifa por km depende del tipo de vinilo (ver [Vinilo]).
 */
data class Campania(
    val id: String,
    val marca: String,
    val fechaInicio: Long,
    val fechaFin: Long
)
