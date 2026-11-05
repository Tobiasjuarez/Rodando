package com.rodando.app.data.repository

import com.rodando.app.data.local.CampaniaDao
import com.rodando.app.data.local.CampaniaEntity
import com.rodando.app.data.local.ViniloDao
import com.rodando.app.data.local.ViniloEntity
import com.rodando.app.data.local.aDominio
import com.rodando.app.data.remote.RodandoApi
import com.rodando.app.domain.model.Campania
import com.rodando.app.domain.model.TipoVinilo
import com.rodando.app.domain.repository.CampaniaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CampaniaRepositoryImpl(
    private val dao: CampaniaDao,
    private val viniloDao: ViniloDao? = null,
    private val api: RodandoApi? = null
) : CampaniaRepository {

    override fun observarCampaniaActiva(): Flow<Campania?> = dao.observarActiva().map { it?.aDominio() }

    /** Baja la campaña y el vinilo vigentes y los guarda en Room: desde ahí los leen las pantallas. */
    override suspend fun actualizarCampania() {
        val api = api ?: return
        val respuesta = api.campania()
        val c = respuesta.campania
        dao.guardar(CampaniaEntity(id = c.id, marca = c.marca, fechaInicio = c.fechaInicio, fechaFin = c.fechaFin))
        val v = respuesta.vinilo
        viniloDao?.guardar(
            ViniloEntity(
                tipo = TipoVinilo.valueOf(v.tipo),
                fijoMensual = v.fijoMensual,
                tarifaPorKm = v.tarifaPorKm,
                fechaInstalacion = v.fechaInstalacion,
                diasMinimos = v.diasMinimos,
                kmTope = v.kmTope,
                codigo = v.codigo
            )
        )
    }
}
