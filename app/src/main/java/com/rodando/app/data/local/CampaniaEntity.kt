package com.rodando.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Campaña asignada al conductor. Viene de la API y se guarda para usarla sin conexión. */
@Entity(tableName = "campanias")
data class CampaniaEntity(
    @PrimaryKey val id: String,
    val marca: String,
    val fechaInicio: Long,
    val fechaFin: Long
)
