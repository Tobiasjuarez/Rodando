package com.rodando.app.data.repository

import com.rodando.app.data.local.ViajeDao
import com.rodando.app.data.local.aDominio
import com.rodando.app.data.local.aEntidad
import com.rodando.app.data.remote.RemotoDeViajes
import com.rodando.app.domain.model.EstadoEnvio
import com.rodando.app.domain.model.EstadoVerificacion
import com.rodando.app.domain.model.PuntoGps
import com.rodando.app.domain.model.ResultadoSincronizacion
import com.rodando.app.domain.model.TipoVinilo
import com.rodando.app.domain.model.Viaje
import com.rodando.app.domain.repository.ViajeRepository
import com.rodando.app.domain.usecase.CalcularGananciaUseCase
import com.rodando.app.domain.usecase.LimpiarRecorridoUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

/**
 * Implementación Offline First: todo se escribe primero en Room y la UI lee de ahí.
 * El envío al servidor ([remoto]) pasa después, cuando hay conexión.
 */
class ViajeRepositoryImpl(
    private val dao: ViajeDao,
    private val remoto: RemotoDeViajes? = null,
    private val limpiarRecorrido: LimpiarRecorridoUseCase = LimpiarRecorridoUseCase(),
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
        tarifaPorKm: Double,
        fotoInicioPath: String,
        intentosFoto: Int,
        enRevision: Boolean
    ): Viaje {
        check(dao.obtenerEnCurso() == null) { "Ya hay un viaje en curso" }
        val viaje = Viaje(
            id = nuevoId(),
            campaniaId = campaniaId,
            tipoVinilo = tipoVinilo,
            tarifaPorKm = tarifaPorKm,
            inicio = ahora(),
            fin = null,
            distanciaKm = 0.0,
            envio = EstadoEnvio.PENDIENTE,
            verificacion = if (enRevision) EstadoVerificacion.EN_REVISION else EstadoVerificacion.POR_APROBAR,
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

    override suspend fun finalizarViaje(viajeId: String, fotoFinPath: String, fotoVerificada: Boolean): Viaje {
        val entidad = requireNotNull(dao.obtenerPorId(viajeId)) { "No existe el viaje $viajeId" }
        // Los saltos imposibles del GPS no suman km.
        val distancia = limpiarRecorrido.distanciaKm(obtenerPuntos(viajeId))
        val verificacion = when {
            // Un viaje en revisión sigue así hasta que el equipo apruebe las fotos.
            entidad.verificacion == EstadoVerificacion.EN_REVISION || !fotoVerificada -> EstadoVerificacion.EN_REVISION
            distancia < CalcularGananciaUseCase.DISTANCIA_MINIMA_KM -> EstadoVerificacion.NO_COMPUTABLE
            else -> EstadoVerificacion.POR_APROBAR
        }
        val finalizado = entidad.copy(
            fin = ahora(),
            distanciaKm = distancia,
            envio = EstadoEnvio.PENDIENTE,
            verificacion = verificacion,
            fotoFinPath = fotoFinPath
        )
        dao.guardar(finalizado)
        return finalizado.aDominio()
    }

    override suspend fun sincronizarPendientes(): ResultadoSincronizacion {
        val remoto = remoto ?: return ResultadoSincronizacion(0, 0, estadosActualizados = true)

        var enviados = 0
        var fallidos = 0
        for (pendiente in dao.pendientesDeEnvio()) {
            try {
                val remoteId = remoto.enviar(pendiente.aDominio(), obtenerPuntos(pendiente.id))
                // Se relee por si cambió mientras se enviaba; solo se marca el envío.
                val actual = dao.obtenerPorId(pendiente.id) ?: continue
                dao.guardar(actual.copy(envio = EstadoEnvio.ENVIADO, remoteId = remoteId))
                enviados++
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                fallidos++
            }
        }

        // Aprobaciones y rechazos del equipo.
        val estadosActualizados = try {
            remoto.estados().forEach { (id, verificacion) ->
                val viaje = dao.obtenerPorId(id)
                if (viaje != null && viaje.verificacion != verificacion) dao.guardar(viaje.copy(verificacion = verificacion))
            }
            true
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            false
        }
        return ResultadoSincronizacion(enviados, fallidos, estadosActualizados)
    }
}
