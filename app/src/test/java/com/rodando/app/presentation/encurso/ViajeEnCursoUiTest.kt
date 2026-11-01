package com.rodando.app.presentation.encurso

import com.rodando.app.domain.model.EstadoViaje
import com.rodando.app.domain.model.PuntoGps
import com.rodando.app.domain.model.TipoVinilo
import com.rodando.app.domain.model.Viaje
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ViajeEnCursoUiTest {

    private val inicio = 1_790_000_000_000L
    private val viaje = Viaje(
        id = "viaje-1",
        campaniaId = "campania-1",
        tipoVinilo = TipoVinilo.PARCIAL,
        inicio = inicio,
        fin = null,
        distanciaKm = 0.0,
        estado = EstadoViaje.EN_CURSO,
        fotoInicioPath = "foto.jpg",
        fotoFinPath = null,
        intentosFoto = 1
    )

    @Test
    fun muestra_el_tiempo_transcurrido_desde_el_inicio() {
        val ui = armarViajeEnCurso(viaje, emptyList(), ahora = inicio + (12 * 60 + 40) * 1000L)

        assertEquals("00:12:40", ui.tiempo)
        assertEquals("0,0 km", ui.distancia)
    }

    @Test
    fun la_distancia_sale_de_los_puntos_registrados() {
        // 0,01° de latitud son unos 1,11 km.
        val puntos = listOf(PuntoGps(-34.60, -58.38, inicio, 5f), PuntoGps(-34.61, -58.38, inicio + 120_000, 5f))

        val ui = armarViajeEnCurso(viaje, puntos, ahora = inicio + 180_000)

        assertEquals("1,1 km", ui.distancia)
        assertEquals(2, ui.puntos.size)
    }

    @Test
    fun avisa_si_despues_de_30_segundos_no_llego_ninguna_posicion() {
        val alArrancar = armarViajeEnCurso(viaje, emptyList(), ahora = inicio + 10_000)
        val sinSenal = armarViajeEnCurso(viaje, emptyList(), ahora = inicio + 30_000)
        val conPuntos = armarViajeEnCurso(viaje, listOf(PuntoGps(-34.60, -58.38, inicio, 5f)), ahora = inicio + 60_000)

        assertFalse(alArrancar.sinSenal)
        assertTrue(sinSenal.sinSenal)
        assertFalse(conPuntos.sinSenal)
    }
}
