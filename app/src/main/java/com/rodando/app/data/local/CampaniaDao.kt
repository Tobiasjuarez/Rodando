package com.rodando.app.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface CampaniaDao {

    /** La campaña más reciente guardada en el teléfono. */
    @Query("SELECT * FROM campanias ORDER BY fechaInicio DESC LIMIT 1")
    fun observarActiva(): Flow<CampaniaEntity?>

    @Query("SELECT COUNT(*) FROM campanias")
    suspend fun contar(): Int

    @Upsert
    suspend fun guardar(campania: CampaniaEntity)
}
