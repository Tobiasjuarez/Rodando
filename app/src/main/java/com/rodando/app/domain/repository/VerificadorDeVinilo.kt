package com.rodando.app.domain.repository

/** Mira una foto y dice si aparece el código del vinilo. La implementación usa ML Kit en el teléfono. */
interface VerificadorDeVinilo {
    suspend fun reconoce(rutaFoto: String, codigoEsperado: String): Boolean
}
