package com.rodando.app.domain.model

/** Tipos de vinilo que se colocan en el local. A más cobertura, mayor fijo mensual y tarifa por km. */
enum class TipoVinilo {
    COMPLETO,
    PARCIAL,
    LIVIANO
}

/**
 * Vinilo vigente del conductor. Los montos y las reglas vienen de la API.
 *
 * El conductor cobra [fijoMensual] por llevar el vinilo más [tarifaPorKm] por cada km computable.
 * El fijo se cobra entero con al menos [kmMinimo] km y [diasMinimos] días con viajes en el mes;
 * por debajo se paga proporcional. Se pagan como máximo [kmTope] km por mes.
 */
data class Vinilo(
    val tipo: TipoVinilo,
    val fijoMensual: Double,
    val tarifaPorKm: Double,
    val fechaInstalacion: Long?,
    val kmMinimo: Double = KM_MINIMO,
    val diasMinimos: Int = DIAS_MINIMOS,
    val kmTope: Double = KM_TOPE
) {
    companion object {
        const val KM_MINIMO = 400.0
        const val DIAS_MINIMOS = 12
        const val KM_TOPE = 1500.0
    }
}
