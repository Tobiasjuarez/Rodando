package com.rodando.app.domain.usecase

/**
 * Regla del camino alternativo de RF01: qué hacer después de verificar la foto del vinilo.
 *
 * - Si se reconoció el vinilo, el viaje continúa.
 * - Si no, se pide repetir la foto hasta [MAX_INTENTOS].
 * - Al agotar los intentos, se ofrece iniciar el viaje "En revisión" o reportar el vinilo dañado.
 */
class EvaluarFotoViniloUseCase {

    operator fun invoke(reconocido: Boolean, intento: Int): ResultadoFoto = when {
        reconocido -> ResultadoFoto.Aprobada
        intento < MAX_INTENTOS -> ResultadoFoto.Reintentar(intentosRestantes = MAX_INTENTOS - intento)
        else -> ResultadoFoto.OfrecerRevision
    }

    companion object {
        const val MAX_INTENTOS = 3
    }
}

sealed interface ResultadoFoto {
    data object Aprobada : ResultadoFoto
    data class Reintentar(val intentosRestantes: Int) : ResultadoFoto
    data object OfrecerRevision : ResultadoFoto
}
