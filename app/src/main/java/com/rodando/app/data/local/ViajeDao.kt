package com.rodando.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ViajeDao {

    /** Todos los viajes, del más nuevo al más viejo. Emite de nuevo cada vez que cambia la tabla. */
    @Query("SELECT * FROM viajes ORDER BY inicio DESC")
    fun observarTodos(): Flow<List<ViajeEntity>>

    @Query("SELECT * FROM viajes WHERE id = :id")
    fun observarPorId(id: String): Flow<ViajeEntity?>

    @Query("SELECT * FROM viajes WHERE estado = 'EN_CURSO' LIMIT 1")
    fun observarEnCurso(): Flow<ViajeEntity?>

    @Query("SELECT * FROM viajes WHERE id = :id")
    suspend fun obtenerPorId(id: String): ViajeEntity?

    @Query("SELECT * FROM viajes WHERE estado = 'EN_CURSO' LIMIT 1")
    suspend fun obtenerEnCurso(): ViajeEntity?

    @Query("SELECT COUNT(*) FROM viajes")
    suspend fun contar(): Int

    /** Inserta o actualiza sin borrar la fila, así no se pierden los puntos GPS del viaje. */
    @Upsert
    suspend fun guardar(viaje: ViajeEntity)

    @Insert
    suspend fun guardarPunto(punto: PuntoGpsEntity)

    @Insert
    suspend fun guardarPuntos(puntos: List<PuntoGpsEntity>)

    @Query("SELECT * FROM puntos_gps WHERE viajeId = :viajeId ORDER BY timestamp")
    fun observarPuntos(viajeId: String): Flow<List<PuntoGpsEntity>>

    @Query("SELECT * FROM puntos_gps WHERE viajeId = :viajeId ORDER BY timestamp")
    suspend fun puntosDeViaje(viajeId: String): List<PuntoGpsEntity>
}
