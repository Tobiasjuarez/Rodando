package com.rodando.app.domain.repository

import com.rodando.app.domain.model.Campania
import kotlinx.coroutines.flow.Flow

interface CampaniaRepository {
    /** Emite la campaña guardada localmente (null si el conductor no tiene una activa). */
    fun observarCampaniaActiva(): Flow<Campania?>

    /** Descarga la campaña desde la API y la guarda en Room. */
    suspend fun actualizarCampania()
}
