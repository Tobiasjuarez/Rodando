package com.rodando.app.domain.model

/**
 * Avance hacia un objetivo del mes (400 km verificados o 12 días con viajes).
 * Lo usan las barras de progreso de Inicio y Ganancias.
 */
data class ObjetivoMensual(
    val actual: Double,
    val meta: Double
) {
    /** Fracción cumplida, entre 0 y 1. */
    val progreso: Double
        get() = if (meta <= 0.0) 1.0 else (actual / meta).coerceIn(0.0, 1.0)

    /** Cuánto falta para llegar a la meta (0 si ya se cumplió). */
    val faltante: Double
        get() = (meta - actual).coerceAtLeast(0.0)

    val cumplido: Boolean
        get() = actual >= meta
}
