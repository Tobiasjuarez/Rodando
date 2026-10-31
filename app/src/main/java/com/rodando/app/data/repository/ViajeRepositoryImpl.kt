package com.rodando.app.data.repository

import com.rodando.app.data.local.ViajeDao
import com.rodando.app.data.local.aDominio
import com.rodando.app.data.local.aEntidad
import com.rodando.app.domain.model.EstadoViaje
import com.rodando.app.domain.model.PuntoGps
import com.rodando.app.domain.model.TipoVinilo
import com.rodando.app.domain.model.Viaje
import com.rodando.app.domain.repository.ViajeRepository
import com.rodando.app.domain.usecase.CalcularDistanciaUseCase
import com.rodando.app.domain.usecase.CalcularGananciaUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

/**
 * Implementación Offline First: todo se escribe primero en Room.
 * El envío a la API (sincronizarPendientes) se suma cuando exista la API simulada.
 */
class ViajeRepositoryImpl(
    private val dao: ViajeDao,
    private val calcularDistancia: CalcularDistanciaUseCase = CalcularDistanciaUseCase(),
    private val ahora: () -> Long = System::currentTimeMillis,
    private val nuevoId: () -> String = { UUID.randomUUID().toString() }
) : ViajeRepository {

    override fun observarViajes(): Flow<List<Viaje>> =
        dao.observarTodos().map { lista -> lista.map { it.aDominio() } }

    override fun observarViaje(id: String): Flow<Viaje?> =
        dao.observarPorId(id).map { it?.aDominio() }

    override fun observarViajeEnCurso(): Flow<Viaje?> =
        dao.observarEnCurso().map { it?.aDominio() }

    override fun observarPuntos(viajeId: String): Flow<List<PuntoGps>> =
        dao.observarPuntos(viajeId).map { lista -> lista.map { it.aDominio() } }

    override suspend fun obtenerPuntos(viajeId: String): List<PuntoGps> =
        dao.puntosDeViaje(viajeId).map { it.aDominio() }

    override suspend fun iniciarViaje(
        campaniaId: String,
        tipoVinilo: TipoVinilo,
        fotoInicioPath: String,
        intentosFoto: Int,
        enRevision: Boolean
    ): Viaje {
        check(dao.obtenerEnCurso() == null) { "Ya hay un viaje en curso" }
        val viaje = Viaje(
            id = nuevoId(),
            campaniaId = campaniaId,
            tipoVinilo = tipoVinilo,
            inicio = ahora(),
            fin = null,
            distanciaKm = 0.0,
            estado = if (enRevision) EstadoViaje.EN_REVISION else EstadoViaje.EN_CURSO,
            fotoInicioPath = fotoInicioPath,
            fotoFinPath = null,
            intentosFoto = intentosFoto
        )
        dao.guardar(viaje.aEntidad())
        return viaje
    }

    override suspend fun registrarPunto(viajeId: String, punto: PuntoGps) {
        dao.guardarPunto(punto.aEntidad(viajeId))
    }

    override suspend fun finalizarViaje(viajeId: String, fotoFinPath: String): Viaje {
        val entidad = requireNotNull(dao.obtenerPorId(viajeId)) { "No existe el viaje $viajeId" }
        val distancia = calcularDistancia(obtenerPuntos(viajeId))
        val estado = when {
            // Un viaje en revisión sigue así hasta que el equipo apruebe las fotos.
            entidad.estado == EstadoViaje.EN_REVISION -> EstadoViaje.EN_REVISION
            distancia < CalcularGananciaUseCase.DISTANCIA_MINIMA_KM -> EstadoViaje.NO_COMPUTABLE
            else -> EstadoViaje.PENDIENTE
        }
        val finalizado = entidad.copy(
            fin = ahora(),
            distanciaKm = distancia,
            estado = estado,
            fotoFinPath = fotoFinPath
        )
        dao.guardar(finalizado)
        return finalizado.aDominio()
    }

    override suspend fun sincronizarPendientes() {
        // Se implementa en la etapa de sincronización (Retrofit + WorkManager).
    }
}
