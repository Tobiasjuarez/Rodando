package com.rodando.app.domain.model

/** Tipos de vinilo que se colocan en el local. A más cobertura, mayor tarifa por km. */
enum class TipoVinilo {
    COMPLETO,
    PARCIAL,
    LIVIANO
}

/** Vinilo vigente del conductor. La tarifa viene de la API. */
data class Vinilo(
    val tipo: TipoVinilo,
    val tarifaPorKm: Double,
    val fechaInstalacion: Long?
)
