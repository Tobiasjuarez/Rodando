package com.rodando.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.rodando.app.domain.model.TipoVinilo

/**
 * Vinilo vigente del conductor y sus reglas de pago (vienen de la API).
 * Hay una sola fila: el conductor lleva un vinilo a la vez.
 */
@Entity(tableName = "vinilo")
data class ViniloEntity(
    @PrimaryKey val id: Int = VIGENTE,
    val tipo: TipoVinilo,
    val fijoMensual: Double,
    val tarifaPorKm: Double,
    val fechaInstalacion: Long?,
    val kmMinimo: Double,
    val diasMinimos: Int,
    val kmTope: Double
) {
    companion object {
        const val VIGENTE = 1
    }
}
