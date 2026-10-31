package com.rodando.app.presentation.ganancias

import com.rodando.app.domain.model.EstadoViaje
import com.rodando.app.domain.model.TipoVinilo
import com.rodando.app.domain.model.Viaje
import com.rodando.app.domain.model.Vinilo
import com.rodando.app.presentation.common.Formato
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/** Los valores de ejemplo son los mismos que muestra el diseño en Figma. */
class GananciasUiTest {

    private val hoy = LocalDate.of(2026, 10, 20)
    private val parcial = Vinilo(TipoVinilo.PARCIAL, fijoMensual = 50_000.0, tarifaPorKm = 50.0, fechaInstalacion = 0L)

    private fun viaje(id: String, dia: LocalDate, km: Double, estado: EstadoViaje = EstadoViaje.SINCRONIZADO): Viaje {
        val inicio = dia.atTime(8, 30).atZone(Formato.zona).toInstant().toEpochMilli()
        return Viaje(id, "c", TipoVinilo.PARCIAL, inicio, inicio + 1, km, estado, "", "", 1)
    }

    @Test
    fun con_los_objetivos_en_curso_muestra_el_fijo_proporcional_y_lo_que_falta() {
        val viajes = (1..5).map { viaje("v$it", LocalDate.of(2026, 10, it), 37.2) } // 186 km en 5 días

        val ui = armarGanancias(viajes, parcial, hoy)

        assertEquals("OCTUBRE · ESTIMADO", ui.encabezado)
        assertEquals("$30.133", ui.total)
        assertEquals("$20.833 de fijo + 186,0 km × $50", ui.detalle)
        assertEquals("186,0 de 400 km", ui.barraKm.valor)
        assertEquals("Te faltan 214 km", ui.barraKm.ayuda)
        assertEquals("5 de 12 días", ui.barraDias.valor)
        assertEquals("Te faltan 7 días con viajes", ui.barraDias.ayuda)
        assertFalse(ui.barraKm.cumplido)
    }

    @Test
    fun con_los_objetivos_cumplidos_cobra_el_fijo_entero() {
        val viajes = (1..13).map { viaje("v$it", LocalDate.of(2026, 10, it), 45.9) } +
            viaje("v14", LocalDate.of(2026, 10, 14), 46.1) // 642,8 km en 14 días

        val ui = armarGanancias(viajes, parcial, hoy)

        assertEquals("$82.140", ui.total)
        assertEquals("$50.000 fijo + 642,8 km × $50", ui.detalle)
        assertEquals("Cumplido · te quedan 857 km pagos (tope 1.500)", ui.barraKm.ayuda)
        assertEquals("Cumplido", ui.barraDias.ayuda)
        assertTrue(ui.barraKm.cumplido)
        assertEquals(1f, ui.barraKm.progreso)
    }

    @Test
    fun solo_cuentan_los_viajes_del_mes_actual() {
        val viajes = listOf(
            viaje("anterior", LocalDate.of(2026, 9, 30), 100.0),
            viaje("este", LocalDate.of(2026, 10, 2), 20.0)
        )

        val ui = armarGanancias(viajes, parcial, hoy)

        assertEquals(1, ui.viajesComputables)
        assertEquals("20,0 de 400 km", ui.barraKm.valor)
    }

    @Test
    fun cuenta_pendientes_y_no_computables_del_mes() {
        val viajes = listOf(
            viaje("a", LocalDate.of(2026, 10, 2), 18.0, EstadoViaje.PENDIENTE),
            viaje("b", LocalDate.of(2026, 10, 3), 0.3, EstadoViaje.NO_COMPUTABLE)
        )

        val ui = armarGanancias(viajes, parcial, hoy)

        assertEquals(1, ui.pendientes)
        assertEquals(1, ui.noComputables)
        assertEquals("Te faltan 11 días con viajes", ui.barraDias.ayuda)
    }
}
