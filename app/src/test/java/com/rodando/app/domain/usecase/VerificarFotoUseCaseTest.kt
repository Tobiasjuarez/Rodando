package com.rodando.app.domain.usecase

import com.rodando.app.domain.model.TipoVinilo
import com.rodando.app.domain.model.Vinilo
import com.rodando.app.domain.repository.VerificadorDeVinilo
import com.rodando.app.domain.repository.ViniloRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class VerificarFotoUseCaseTest {

    private val vinilo = Vinilo(TipoVinilo.PARCIAL, 50_000.0, 50.0, fechaInstalacion = 0, codigo = "RODANDO-VINILO-0001")

    private class Vinilos(private val v: Vinilo?) : ViniloRepository {
        override fun observarVinilo(): Flow<Vinilo?> = flowOf(v)
    }

    /** Simula ML Kit: "lee" el código que diga [leido]. */
    private class Lector(private val leido: String?, private val falla: Boolean = false) : VerificadorDeVinilo {
        override suspend fun reconoce(rutaFoto: String, codigoEsperado: String): Boolean {
            if (falla) error("ML Kit no pudo leer la imagen")
            return leido == codigoEsperado
        }
    }

    private fun verificar(leido: String?, v: Vinilo? = vinilo, falla: Boolean = false) =
        VerificarFotoUseCase(Vinilos(v), Lector(leido, falla))

    @Test
    fun con_el_codigo_del_vinilo_la_foto_se_aprueba() = runBlocking {
        assertEquals(ResultadoFoto.Aprobada, verificar("RODANDO-VINILO-0001")("foto.jpg", intento = 1))
    }

    @Test
    fun con_otro_codigo_o_sin_codigo_se_pide_repetir() = runBlocking {
        assertEquals(ResultadoFoto.Reintentar(2), verificar("OTRO-VINILO")("foto.jpg", intento = 1))
        assertEquals(ResultadoFoto.Reintentar(1), verificar(null)("foto.jpg", intento = 2))
    }

    @Test
    fun al_tercer_intento_fallido_se_ofrece_seguir_en_revision() = runBlocking {
        assertEquals(ResultadoFoto.OfrecerRevision, verificar(null)("foto.jpg", intento = 3))
    }

    @Test
    fun si_el_lector_falla_cuenta_como_no_reconocida() = runBlocking {
        assertEquals(ResultadoFoto.Reintentar(2), verificar("RODANDO-VINILO-0001", falla = true)("foto.jpg", intento = 1))
    }

    @Test
    fun sin_vinilo_cargado_no_se_puede_aprobar() = runBlocking {
        assertEquals(ResultadoFoto.Reintentar(2), verificar("RODANDO-VINILO-0001", v = null)("foto.jpg", intento = 1))
    }
}
