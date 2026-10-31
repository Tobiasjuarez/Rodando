package com.rodando.app.domain.repository

import com.rodando.app.domain.model.PuntoGps
import com.rodando.app.domain.model.TipoVinilo
import com.rodando.app.domain.model.Viaje
import kotlinx.coroutines.flow.Flow

/**
 * Contrato que usa la capa de dominio. La implementación (capa de datos)
 * combina Room, archivos de fotos y la API; el dominio no conoce ninguno de ellos.
 *
 * Room es la fuente de verdad: los Flow emiten lo guardado en el teléfono,
 * con o sin conexión.
 */
interface ViajeRepository {
    fun observarViajes(): Flow<List<Viaje>>
    fun observarViaje(id: String): Flow<Viaje?>
    fun observarViajeEnCurso(): Flow<Viaje?>
    fun observarPuntos(viajeId: String): Flow<List<PuntoGps>>
    suspend fun obtenerPuntos(viajeId: String): List<PuntoGps>

    /**
     * Crea el viaje "En curso" (o "En revisión" si la foto no se pudo verificar).
     * Falla si ya hay otro viaje en curso.
     */
    suspend fun iniciarViaje(
        campaniaId: String,
        tipoVinilo: TipoVinilo,
        fotoInicioPath: String,
        intentosFoto: Int,
        enRevision: Boolean
    ): Viaje

    suspend fun registrarPunto(viajeId: String, punto: PuntoGps)

    /** Cierra el viaje, calcula la distancia y lo deja pendiente de envío (o no computable). */
    suspend fun finalizarViaje(viajeId: String, fotoFinPath: String): Viaje

    suspend fun sincronizarPendientes()
}
