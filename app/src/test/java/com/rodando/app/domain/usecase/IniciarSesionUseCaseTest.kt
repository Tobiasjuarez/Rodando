package com.rodando.app.domain.usecase

import com.rodando.app.domain.model.Campania
import com.rodando.app.domain.model.Sesion
import com.rodando.app.domain.repository.CampaniaRepository
import com.rodando.app.domain.repository.SesionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class IniciarSesionUseCaseTest {

    private class SesionesFalsas : SesionRepository {
        override val sesion = MutableStateFlow<Sesion?>(null)
        var emailRecibido: String? = null

        override suspend fun iniciarSesion(email: String, password: String): Sesion {
            emailRecibido = email
            if (password != "rodando123") error("Email o contraseña incorrectos.")
            return Sesion("token", "Lucía", email).also { sesion.value = it }
        }

        override suspend fun cerrarSesion() {
            sesion.value = null
        }
    }

    private class Campanias(private val falla: Boolean) : CampaniaRepository {
        var descargas = 0
        override fun observarCampaniaActiva(): Flow<Campania?> = flowOf(null)
        override suspend fun actualizarCampania() {
            descargas++
            if (falla) error("sin conexión")
        }
    }

    @Test
    fun inicia_sesion_y_descarga_la_campania() = runBlocking {
        val sesiones = SesionesFalsas()
        val campanias = Campanias(falla = false)

        val sesion = IniciarSesionUseCase(sesiones, campanias)("  lucia@rodando.com.ar ", "rodando123")

        assertEquals("Lucía", sesion.nombre)
        assertEquals("lucia@rodando.com.ar", sesiones.emailRecibido)
        assertEquals(1, campanias.descargas)
    }

    @Test
    fun si_la_campania_no_baja_la_sesion_queda_igual() = runBlocking {
        val sesiones = SesionesFalsas()

        IniciarSesionUseCase(sesiones, Campanias(falla = true))("lucia@rodando.com.ar", "rodando123")

        assertEquals("token", sesiones.sesion.value?.token)
    }

    @Test
    fun con_datos_vacios_o_contrasenia_incorrecta_falla_con_un_mensaje() = runBlocking {
        val usar = IniciarSesionUseCase(SesionesFalsas(), Campanias(falla = false))

        val vacio = runCatching { usar("", "") }
        val incorrecta = runCatching { usar("lucia@rodando.com.ar", "otra") }

        assertEquals("Completá el email y la contraseña.", vacio.exceptionOrNull()?.message)
        assertTrue(incorrecta.isFailure)
    }
}
