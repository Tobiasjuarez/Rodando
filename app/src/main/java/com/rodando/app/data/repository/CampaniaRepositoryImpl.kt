package com.rodando.app.data.repository

import com.rodando.app.data.local.CampaniaDao
import com.rodando.app.data.local.aDominio
import com.rodando.app.domain.model.Campania
import com.rodando.app.domain.repository.CampaniaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CampaniaRepositoryImpl(private val dao: CampaniaDao) : CampaniaRepository {

    override fun observarCampaniaActiva(): Flow<Campania?> = dao.observarActiva().map { it?.aDominio() }

    override suspend fun actualizarCampania() {
        // Se implementa con la API simulada (etapa de sincronización).
    }
}
