package com.rodando.app.domain.repository

import kotlinx.coroutines.flow.Flow

/** Cuándo terminó bien la última sincronización con el servidor (null si nunca). */
interface RegistroDeSincronizacion {
    val ultima: Flow<Long?>
    suspend fun registrar(momento: Long)
}
