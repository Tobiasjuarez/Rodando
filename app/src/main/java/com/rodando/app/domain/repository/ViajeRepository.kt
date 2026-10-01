package com.rodando.app.domain.repository

import com.rodando.app.domain.model.PuntoGps
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
    fun observarViajeEnCurso(): Flow<Viaje?>
    suspend fun iniciarViaje(campaniaId: String, fotoInicioPath: String): Viaje
    suspend fun registrarPunto(viajeId: String, punto: PuntoGps)
    suspend fun finalizarViaje(viajeId: String, fotoFinPath: String): Viaje
    suspend fun sincronizarPendientes()
}
