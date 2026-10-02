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
    val tipoVinilo: TipoVinilo,
    val inicio: Long,
    val fin: Long?,
    val distanciaKm: Double,
    val estado: EstadoViaje,
    val fotoInicioPath: String,
    val fotoFinPath: String?,
    val intentosFoto: Int
)

enum class EstadoViaje {
    EN_CURSO,

    /** El vinilo no se reconoció en la foto: el recorrido se registra pero no computa hasta que se apruebe. */
    EN_REVISION,
    PENDIENTE,
    SINCRONIZADO,
    NO_COMPUTABLE
}
