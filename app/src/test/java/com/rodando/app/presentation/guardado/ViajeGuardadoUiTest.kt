package com.rodando.app.presentation.guardado

import com.rodando.app.domain.model.EstadoViaje
import com.rodando.app.domain.model.TipoVinilo
import com.rodando.app.domain.model.Viaje
import com.rodando.app.domain.model.Vinilo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ViajeGuardadoUiTest {

    private val vinilo = Vinilo(TipoVinilo.PARCIAL, 50_000.0, 50.0, fechaInstalacion = 0)

    private fun viaje(estado: EstadoViaje, km: Double) = Viaje(
        id = "viaje-1",
        campaniaId = "campania-1",
        tipoVinilo = TipoVinilo.PARCIAL,
        inicio = 0,
        fin = 42 * 60_000L,
        distanciaKm = km,
        estado = estado,
        fotoInicioPath = "inicio.jpg",
        fotoFinPath = "fin.jpg",
        intentosFoto = 1
    )

    @Test
    fun un_viaje_pendiente_muestra_lo_que_suma_por_km() {
        val ui = armarViajeGuardado(viaje(EstadoViaje.PENDIENTE, 18.4), vinilo)

        assertEquals("¡Viaje guardado!", ui.titulo)
        assertEquals("18,4 km", ui.distancia)
        assertEquals("42 min", ui.duracion)
        assertEquals("+$920", ui.ganancia)
    }

    @Test
    fun un_viaje_muy_corto_avisa_que_no_suma() {
        val ui = armarViajeGuardado(viaje(EstadoViaje.NO_COMPUTABLE, 0.3), vinilo)

        assertNull(ui.ganancia)
        assertTrue(ui.mensaje.contains("menos de 0,5 km"))
    }

    @Test
    fun un_viaje_en_revision_no_suma_hasta_que_se_apruebe() {
        val ui = armarViajeGuardado(viaje(EstadoViaje.EN_REVISION, 12.0), vinilo)

        assertNull(ui.ganancia)
        assertEquals("Viaje guardado en revisión", ui.titulo)
    }
}
