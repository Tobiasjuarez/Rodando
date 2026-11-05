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
 * El fijo se cobra entero con al menos [diasMinimos] días con viajes en el mes; por debajo se paga
 * proporcional. No hay mínimo de km, para no incentivar recorridos extra: el vinilo tiene que
 * circular en los viajes de siempre. Se pagan como máximo [kmTope] km por mes.
 */
data class Vinilo(
    val tipo: TipoVinilo,
    val fijoMensual: Double,
    val tarifaPorKm: Double,
    val fechaInstalacion: Long?,
    val diasMinimos: Int = DIAS_MINIMOS,
    val kmTope: Double = KM_TOPE,
    /** Código QR impreso en el vinilo: la foto de inicio y de fin tiene que mostrarlo. */
    val codigo: String = ""
) {
    companion object {
        const val DIAS_MINIMOS = 12
        const val KM_TOPE = 1500.0
    }
}
