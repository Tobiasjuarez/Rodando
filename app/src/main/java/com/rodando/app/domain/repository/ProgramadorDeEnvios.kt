package com.rodando.app.domain.repository

/**
 * Pide que los viajes pendientes se envíen en segundo plano. Quien lo implementa decide
 * cuándo (apenas haya conexión) y cómo reintentar si falla.
 */
interface ProgramadorDeEnvios {
    /** Envía lo pendiente y trae las aprobaciones apenas haya conexión. */
    fun enviarAhora()

    /** Repite el envío y la consulta de aprobaciones cada tanto, aunque la app esté cerrada. */
    fun programarPeriodico()
}
