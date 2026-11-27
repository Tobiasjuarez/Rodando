package com.rodando.app.domain.repository

/**
 * RF01: si el vinilo no se puede verificar porque está dañado (roto, despegado, QR ilegible),
 * el conductor lo reporta para que el equipo coordine el cambio.
 */
interface ReportesDeVinilo {
    /**
     * Envía el reporte con la última foto. [id] identifica el reporte: si se reintenta con el
     * mismo id, el servidor no lo duplica. Lanza una excepción si no se pudo enviar.
     */
    suspend fun reportarDanio(id: String, rutaFoto: String?, momento: String, viajeId: String?)
}
