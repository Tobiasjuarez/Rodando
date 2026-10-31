package com.rodando.app.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ViniloDao {

    @Query("SELECT * FROM vinilo WHERE id = 1") // ViniloEntity.VIGENTE
    fun observarVigente(): Flow<ViniloEntity?>

    @Upsert
    suspend fun guardar(vinilo: ViniloEntity)
}
