package com.rodando.app.domain.model

/**
 * Un viaje registrado por el conductor (RF01, RF02).
 *
 * El id es un UUID generado en el teléfono: si un envío a la API se reintenta,
 * el backend reconoce el mismo viaje y no lo duplica.
 */
data class Viaje(
    val id: String,
    val campaniaId: String,
    val inicio: Long,
    val fin: Long?,
    val distanciaKm: Double,
    val estado: EstadoViaje,
    val fotoInicioPath: String,
    val fotoFinPath: String?
)

enum class EstadoViaje {
    EN_CURSO,
    PENDIENTE,
    SINCRONIZADO,
    NO_COMPUTABLE
}
