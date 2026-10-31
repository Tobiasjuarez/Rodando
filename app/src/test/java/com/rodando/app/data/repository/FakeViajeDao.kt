package com.rodando.app.data.repository

import com.rodando.app.data.local.PuntoGpsEntity
import com.rodando.app.data.local.ViajeDao
import com.rodando.app.data.local.ViajeEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** DAO en memoria para probar el repositorio sin Android. */
class FakeViajeDao : ViajeDao {
    private val viajes = MutableStateFlow<List<ViajeEntity>>(emptyList())
    private val flujoPuntos = MutableStateFlow<List<PuntoGpsEntity>>(emptyList())
    val puntos: List<PuntoGpsEntity> get() = flujoPuntos.value

    override fun observarTodos(): Flow<List<ViajeEntity>> =
        viajes.map { lista -> lista.sortedByDescending { it.inicio } }

    override fun observarPorId(id: String): Flow<ViajeEntity?> =
        viajes.map { lista -> lista.find { it.id == id } }

    override fun observarEnCurso(): Flow<ViajeEntity?> =
        viajes.map { lista -> lista.find { it.fin == null } }

    override suspend fun obtenerPorId(id: String): ViajeEntity? = viajes.value.find { it.id == id }

    override suspend fun obtenerEnCurso(): ViajeEntity? =
        viajes.value.find { it.fin == null }

    override suspend fun contar(): Int = viajes.value.size

    override suspend fun guardar(viaje: ViajeEntity) {
        viajes.value = viajes.value.filterNot { it.id == viaje.id } + viaje
    }

    override suspend fun guardarPunto(punto: PuntoGpsEntity) {
        flujoPuntos.value = flujoPuntos.value + punto
    }

    override suspend fun guardarPuntos(puntos: List<PuntoGpsEntity>) {
        flujoPuntos.value = flujoPuntos.value + puntos
    }

    override fun observarPuntos(viajeId: String): Flow<List<PuntoGpsEntity>> =
        flujoPuntos.map { lista -> lista.filter { it.viajeId == viajeId }.sortedBy { it.timestamp } }

    override suspend fun puntosDeViaje(viajeId: String): List<PuntoGpsEntity> =
        puntos.filter { it.viajeId == viajeId }.sortedBy { it.timestamp }
}
