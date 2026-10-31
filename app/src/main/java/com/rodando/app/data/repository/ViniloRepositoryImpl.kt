package com.rodando.app.data.repository

import com.rodando.app.data.local.ViniloDao
import com.rodando.app.data.local.aDominio
import com.rodando.app.domain.model.Vinilo
import com.rodando.app.domain.repository.ViniloRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ViniloRepositoryImpl(private val dao: ViniloDao) : ViniloRepository {

    override fun observarVinilo(): Flow<Vinilo?> = dao.observarVigente().map { it?.aDominio() }
}
