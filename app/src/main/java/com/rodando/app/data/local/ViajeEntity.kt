package com.rodando.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.rodando.app.domain.model.EstadoViaje
import com.rodando.app.domain.model.TipoVinilo

/**
 * Viaje guardado en el teléfono. Room es la fuente de verdad: la UI lo lee de acá,
 * haya o no conexión.
 *
 * [remoteId] queda en null hasta que la API confirma el envío.
 */
@Entity(tableName = "viajes")
data class ViajeEntity(
    @PrimaryKey val id: String,
    val campaniaId: String,
    val tipoVinilo: TipoVinilo,
    val inicio: Long,
    val fin: Long?,
    val distanciaKm: Double,
    val estado: EstadoViaje,
    val fotoInicioPath: String,
    val fotoFinPath: String?,
    val intentosFoto: Int,
    val remoteId: String? = null
)
