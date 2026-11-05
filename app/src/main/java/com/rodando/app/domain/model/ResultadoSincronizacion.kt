package com.rodando.app.domain.model

/**
 * Cómo salió un intento de sincronizar: cuántos viajes se enviaron, cuántos fallaron
 * (se reintentan después) y si se pudieron traer las aprobaciones del equipo.
 */
data class ResultadoSincronizacion(
    val enviados: Int,
    val fallidos: Int,
    val estadosActualizados: Boolean
) {
    /** Hay que reintentar más tarde si quedó algo sin enviar o no se pudo consultar el servidor. */
    val completo: Boolean get() = fallidos == 0 && estadosActualizados
}
