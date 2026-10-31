package com.rodando.app.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Posición GPS de un viaje. Si se borra el viaje, se borran sus puntos. */
@Entity(
    tableName = "puntos_gps",
    foreignKeys = [
        ForeignKey(
            entity = ViajeEntity::class,
            parentColumns = ["id"],
            childColumns = ["viajeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("viajeId")]
)
data class PuntoGpsEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val viajeId: String,
    val latitud: Double,
    val longitud: Double,
    val timestamp: Long,
    val precisionMetros: Float
)
