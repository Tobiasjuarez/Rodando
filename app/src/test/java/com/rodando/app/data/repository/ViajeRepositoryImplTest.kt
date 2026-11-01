package com.rodando.app.data.repository

import com.rodando.app.domain.model.EstadoViaje
import com.rodando.app.domain.model.PuntoGps
import com.rodando.app.domain.model.TipoVinilo
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ViajeRepositoryImplTest {

    private val dao = FakeViajeDao()
    private var reloj = 1_000L
    private val repositorio = ViajeRepositoryImpl(dao, ahora = { reloj }, nuevoId = { "viaje-1" })

    private fun punto(lat: Double, t: Long) = PuntoGps(lat, -58.38, t, 5f)

    private suspend fun iniciar(enRevision: Boolean = false) =
        repositorio.iniciarViaje("campania", TipoVinilo.PARCIAL, "inicio.jpg", 1, enRevision)

    @Test
    fun iniciar_deja_el_viaje_en_curso_y_guardado() = runBlocking {
        iniciar()

        val enCurso = repositorio.observarViajeEnCurso().first()
        assertNotNull(enCurso)
        assertEquals(EstadoViaje.EN_CURSO, enCurso!!.estado)
        assertEquals(1, repositorio.observarViajes().first().size)
    }

    @Test
    fun no_se_puede_iniciar_otro_viaje_con_uno_en_curso() = runBlocking {
        iniciar()

        val resultado = runCatching { iniciar() }

        assertTrue(resultado.isFailure)
    }

    @Test
    fun al_finalizar_calcula_la_distancia_y_queda_pendiente_de_envio() = runBlocking {
        iniciar()
        repositorio.registrarPunto("viaje-1", punto(-34.60, 60_000))
        repositorio.registrarPunto("viaje-1", punto(-34.61, 180_000)) // unos 1,1 km en 2 minutos
        reloj = 2_000L

        val viaje = repositorio.finalizarViaje("viaje-1", "fin.jpg")

        assertEquals(EstadoViaje.PENDIENTE, viaje.estado)
        assertEquals(1.11, viaje.distanciaKm, 0.01)
        assertEquals(2_000L, viaje.fin)
        assertEquals("fin.jpg", viaje.fotoFinPath)
    }

    @Test
    fun un_viaje_de_menos_de_medio_km_queda_no_computable() = runBlocking {
        iniciar()
        repositorio.registrarPunto("viaje-1", punto(-34.600, 60_000))
        repositorio.registrarPunto("viaje-1", punto(-34.601, 90_000)) // unos 110 m

        val viaje = repositorio.finalizarViaje("viaje-1", "fin.jpg")

        assertEquals(EstadoViaje.NO_COMPUTABLE, viaje.estado)
    }

    @Test
    fun un_viaje_en_revision_sigue_en_revision_al_finalizar() = runBlocking {
        iniciar(enRevision = true)
        repositorio.registrarPunto("viaje-1", punto(-34.60, 60_000))
        repositorio.registrarPunto("viaje-1", punto(-34.61, 180_000))

        val viaje = repositorio.finalizarViaje("viaje-1", "fin.jpg")

        assertEquals(EstadoViaje.EN_REVISION, viaje.estado)
    }
}
