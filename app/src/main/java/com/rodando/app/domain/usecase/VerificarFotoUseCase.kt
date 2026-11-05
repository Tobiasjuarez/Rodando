package com.rodando.app.domain.usecase

import com.rodando.app.domain.repository.VerificadorDeVinilo
import com.rodando.app.domain.repository.ViniloRepository
import kotlinx.coroutines.flow.first

/**
 * RF01: busca en la foto el código del vinilo vigente y decide qué sigue según el intento:
 * aprobada, repetir la foto o, al tercer intento fallido, ofrecer seguir "en revisión".
 *
 * Límite (ver la respuesta a la devolución): reconocer el código prueba que el vinilo
 * estaba en ese momento, no que el auto circuló con él todo el viaje.
 */
class VerificarFotoUseCase(
    private val vinilos: ViniloRepository,
    private val verificador: VerificadorDeVinilo,
    private val evaluar: EvaluarFotoViniloUseCase = EvaluarFotoViniloUseCase()
) {

    suspend operator fun invoke(rutaFoto: String, intento: Int): ResultadoFoto {
        val codigo = vinilos.observarVinilo().first()?.codigo.orEmpty()
        // Si el lector falla (foto ilegible, error de ML Kit), cuenta como no reconocida.
        val reconocido = codigo.isNotBlank() &&
            runCatching { verificador.reconoce(rutaFoto, codigo) }.getOrDefault(false)
        return evaluar(reconocido, intento)
    }
}
