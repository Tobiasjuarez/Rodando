package com.rodando.app.presentation.inicio

import com.rodando.app.domain.model.Campania
import com.rodando.app.domain.model.EstadoViaje
import com.rodando.app.domain.model.TipoVinilo
import com.rodando.app.domain.model.Viaje
import com.rodando.app.domain.model.Vinilo
import com.rodando.app.presentation.common.Formato
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class InicioUiTest {

    private val hoy = LocalDate.of(2026, 10, 20)
    private fun millis(dia: LocalDate) = dia.atStartOfDay(Formato.zona).toInstant().toEpochMilli()

    private val campania = Campania("c", "Pampa Bebidas", millis(LocalDate.of(2026, 10, 1)), millis(LocalDate.of(2026, 10, 31)))
    private val parcial = Vinilo(TipoVinilo.PARCIAL, 50_000.0, 50.0, fechaInstalacion = millis(LocalDate.of(2026, 10, 1)))

    private fun viaje(estado: EstadoViaje) =
        Viaje("v", "c", TipoVinilo.PARCIAL, millis(hoy), if (estado == EstadoViaje.EN_CURSO) null else millis(hoy) + 1, 0.0, estado, "", null, 1)

    @Test
    fun muestra_la_campania_el_vinilo_y_la_tarifa() {
        val ui = armarInicio(campania, parcial, emptyList(), hoy)

        assertEquals("Pampa Bebidas", ui.marca)
        assertEquals("1 al 31 de octubre · Vinilo Parcial", ui.periodo)
        assertEquals("$50.000/mes + $50/km", ui.tarifa)
        assertTrue(ui.puedeIniciarViaje)
    }

    @Test
    fun sin_vinilo_instalado_no_hay_resumen_ni_se_puede_iniciar() {
        val sinInstalar = parcial.copy(fechaInstalacion = null)

        val ui = armarInicio(campania, sinInstalar, emptyList(), hoy)

        assertFalse(ui.puedeIniciarViaje)
        assertNull(armarInicio(campania, null, emptyList(), hoy).totalMes)
    }

    @Test
    fun con_un_viaje_en_curso_no_se_puede_iniciar_otro() {
        val ui = armarInicio(campania, parcial, listOf(viaje(EstadoViaje.EN_CURSO)), hoy)

        assertFalse(ui.puedeIniciarViaje)
        assertTrue(ui.hayViajeEnCurso)
    }

    @Test
    fun cuenta_los_viajes_pendientes_de_envio() {
        val ui = armarInicio(campania, parcial, listOf(viaje(EstadoViaje.PENDIENTE), viaje(EstadoViaje.PENDIENTE)), hoy)

        assertEquals(2, ui.pendientes)
    }
}
