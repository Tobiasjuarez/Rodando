package com.rodando.app.domain.repository

import com.rodando.app.domain.model.EstadoDeEnvio
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Pide que los viajes pendientes se envíen en segundo plano. Quien lo implementa decide
 * cuándo (apenas haya conexión) y cómo reintentar si falla.
 */
interface ProgramadorDeEnvios {
    /** Envía lo pendiente y trae las aprobaciones apenas haya conexión. */
    fun enviarAhora()

    /** Repite el envío y la consulta de aprobaciones cada tanto, aunque la app esté cerrada. */
    fun programarPeriodico()

    /** Estado del envío pedido con [enviarAhora], para mostrarlo en pantalla. */
    val estado: Flow<EstadoDeEnvio> get() = flowOf(EstadoDeEnvio.QUIETO)
}
