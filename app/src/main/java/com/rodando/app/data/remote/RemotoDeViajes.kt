package com.rodando.app.data.remote

import com.rodando.app.domain.model.EstadoVerificacion
import com.rodando.app.domain.model.PuntoGps
import com.rodando.app.domain.model.Viaje

/**
 * Lo que el repositorio de viajes necesita del servidor. Separado de Retrofit para poder
 * probar la sincronización (envíos parciales, duplicados) sin red.
 */
interface RemotoDeViajes {
    /** Envía el viaje y devuelve el id que le dio el servidor. Lanza una excepción si falla. */
    suspend fun enviar(viaje: Viaje, puntos: List<PuntoGps>): String

    /** Estado de verificación de cada viaje que tiene el servidor, por id. */
    suspend fun estados(): Map<String, EstadoVerificacion>
}

class RemotoDeViajesApi(
    private val api: RodandoApi,
    private val codificarFoto: (String?) -> String? = CodificadorDeFotos::enBase64
) : RemotoDeViajes {

    override suspend fun enviar(viaje: Viaje, puntos: List<PuntoGps>): String =
        api.enviarViaje(viaje.aDto(puntos, codificarFoto(viaje.fotoInicioPath), codificarFoto(viaje.fotoFinPath))).remoteId

    override suspend fun estados(): Map<String, EstadoVerificacion> =
        api.estados().mapNotNull { dto -> verificacionDe(dto.verificacion)?.let { dto.id to it } }.toMap()
}

fun Viaje.aDto(puntos: List<PuntoGps>, fotoInicio: String?, fotoFin: String?) = ViajeDto(
    id = id,
    campaniaId = campaniaId,
    tipoVinilo = tipoVinilo.name,
    tarifaPorKm = tarifaPorKm,
    inicio = inicio,
    fin = fin,
    distanciaKm = distanciaKm,
    verificacion = verificacion.name,
    intentosFoto = intentosFoto,
    puntos = puntos.map { PuntoDto(it.latitud, it.longitud, it.timestamp, it.precisionMetros) },
    fotoInicio = fotoInicio,
    fotoFin = fotoFin
)

/** Un estado que la app no conoce (una versión más nueva del servidor) se ignora. */
fun verificacionDe(texto: String?): EstadoVerificacion? =
    EstadoVerificacion.entries.firstOrNull { it.name == texto }
