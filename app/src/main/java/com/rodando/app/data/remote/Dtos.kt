package com.rodando.app.data.remote

// Lo que viaja por la red, tal cual lo escribe y lo lee la API. Gson los arma por nombre de campo.

data class LoginPedido(val email: String, val password: String)

data class LoginRespuesta(val token: String, val conductor: ConductorDto)

data class ConductorDto(val id: String, val nombre: String, val email: String)

data class CampaniaRespuesta(val campania: CampaniaDto, val vinilo: ViniloDto)

data class CampaniaDto(val id: String, val marca: String, val fechaInicio: Long, val fechaFin: Long)

data class ViniloDto(
    val tipo: String,
    val fijoMensual: Double,
    val tarifaPorKm: Double,
    val fechaInstalacion: Long?,
    val diasMinimos: Int,
    val kmTope: Double,
    val codigo: String
)

data class ViajeDto(
    val id: String,
    val campaniaId: String,
    val tipoVinilo: String,
    val tarifaPorKm: Double,
    val inicio: Long,
    val fin: Long?,
    val distanciaKm: Double,
    val verificacion: String,
    val intentosFoto: Int,
    val puntos: List<PuntoDto>,
    /** Fotos en JPEG achicado y codificado en base64; null si no hay archivo. */
    val fotoInicio: String?,
    val fotoFin: String?
)

data class PuntoDto(val latitud: Double, val longitud: Double, val timestamp: Long, val precisionMetros: Float)

data class EnvioRespuesta(val remoteId: String, val verificacion: String?, val duplicado: Boolean?)

data class EstadoDto(val id: String, val verificacion: String)
