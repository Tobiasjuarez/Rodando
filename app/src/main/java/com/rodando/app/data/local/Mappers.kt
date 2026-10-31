package com.rodando.app.data.local

import com.rodando.app.domain.model.PuntoGps
import com.rodando.app.domain.model.Viaje

// Conversión entre las entidades de Room y los modelos del dominio.

fun ViajeEntity.aDominio(): Viaje = Viaje(
    id = id,
    campaniaId = campaniaId,
    tipoVinilo = tipoVinilo,
    inicio = inicio,
    fin = fin,
    distanciaKm = distanciaKm,
    estado = estado,
    fotoInicioPath = fotoInicioPath,
    fotoFinPath = fotoFinPath,
    intentosFoto = intentosFoto
)

fun Viaje.aEntidad(remoteId: String? = null): ViajeEntity = ViajeEntity(
    id = id,
    campaniaId = campaniaId,
    tipoVinilo = tipoVinilo,
    inicio = inicio,
    fin = fin,
    distanciaKm = distanciaKm,
    estado = estado,
    fotoInicioPath = fotoInicioPath,
    fotoFinPath = fotoFinPath,
    intentosFoto = intentosFoto,
    remoteId = remoteId
)

fun PuntoGpsEntity.aDominio(): PuntoGps = PuntoGps(
    latitud = latitud,
    longitud = longitud,
    timestamp = timestamp,
    precisionMetros = precisionMetros
)

fun PuntoGps.aEntidad(viajeId: String): PuntoGpsEntity = PuntoGpsEntity(
    viajeId = viajeId,
    latitud = latitud,
    longitud = longitud,
    timestamp = timestamp,
    precisionMetros = precisionMetros
)
