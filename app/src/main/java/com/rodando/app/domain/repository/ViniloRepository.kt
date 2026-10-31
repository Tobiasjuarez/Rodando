package com.rodando.app.domain.repository

import com.rodando.app.domain.model.Vinilo
import kotlinx.coroutines.flow.Flow

interface ViniloRepository {
    /** Emite el vinilo vigente guardado en el teléfono (null si todavía no se instaló). */
    fun observarVinilo(): Flow<Vinilo?>
}
