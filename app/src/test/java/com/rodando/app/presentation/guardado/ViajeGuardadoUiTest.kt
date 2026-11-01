package com.rodando.app.presentation.guardado

import com.rodando.app.domain.model.EstadoEnvio
import com.rodando.app.domain.model.EstadoVerificacion
import com.rodando.app.domain.model.viajeDePrueba
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ViajeGuardadoUiTest {

    private fun viaje(verificacion: EstadoVerificacion, km: Double, tarifa: Double = 50.0) = viajeDePrueba(
        id = "viaje-1",
        inicio = 0,
        fin = 42 * 60_000L,
        km = km,
        verificacion = verificacion,
        envio = EstadoEnvio.PENDIENTE,
        tarifaPorKm = tarifa
    )

    @Test
    fun un_viaje_por_aprobar_muestra_lo_que_suma_por_km_con_su_tarifa() {
        val ui = armarViajeGuardado(viaje(EstadoVerificacion.POR_APROBAR, 18.4))

        assertEquals("¡Viaje guardado!", ui.titulo)
        assertEquals("18,4 km", ui.distancia)
        assertEquals("42 min", ui.duracion)
        assertEquals("+$920", ui.ganancia)
        assertTrue(ui.mensaje.contains("cuando el equipo lo apruebe"))
        assertTrue(ui.mensaje.contains("cuando haya conexión"))
    }

    @Test
    fun la_ganancia_usa_la_tarifa_del_viaje() {
        val ui = armarViajeGuardado(viaje(EstadoVerificacion.POR_APROBAR, 10.0, tarifa = 60.0))

        assertEquals("+$600", ui.ganancia)
    }

    @Test
    fun un_viaje_muy_corto_avisa_que_no_suma() {
        val ui = armarViajeGuardado(viaje(EstadoVerificacion.NO_COMPUTABLE, 0.3))

        assertNull(ui.ganancia)
        assertTrue(ui.mensaje.contains("menos de 0,5 km"))
    }

    @Test
    fun un_viaje_en_revision_no_suma_hasta_que_se_apruebe() {
        val ui = armarViajeGuardado(viaje(EstadoVerificacion.EN_REVISION, 12.0))

        assertNull(ui.ganancia)
        assertEquals("Viaje guardado en revisión", ui.titulo)
    }
}
