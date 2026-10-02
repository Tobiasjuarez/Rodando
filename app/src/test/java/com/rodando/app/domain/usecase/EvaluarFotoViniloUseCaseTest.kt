package com.rodando.app.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Test

class EvaluarFotoViniloUseCaseTest {

    private val evaluar = EvaluarFotoViniloUseCase()

    @Test
    fun si_se_reconoce_el_vinilo_la_foto_se_aprueba() {
        assertEquals(ResultadoFoto.Aprobada, evaluar(reconocido = true, intento = 1))
    }

    @Test
    fun si_no_se_reconoce_pide_reintentar_mientras_queden_intentos() {
        assertEquals(ResultadoFoto.Reintentar(intentosRestantes = 2), evaluar(reconocido = false, intento = 1))
        assertEquals(ResultadoFoto.Reintentar(intentosRestantes = 1), evaluar(reconocido = false, intento = 2))
    }

    @Test
    fun al_tercer_intento_fallido_ofrece_revision() {
        assertEquals(ResultadoFoto.OfrecerRevision, evaluar(reconocido = false, intento = 3))
    }
}
