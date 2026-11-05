package com.rodando.app.presentation.inicio

import com.rodando.app.domain.model.Campania
import com.rodando.app.domain.model.EstadoEnvio
import com.rodando.app.domain.model.EstadoVerificacion
import com.rodando.app.domain.model.TipoVinilo
import com.rodando.app.domain.model.Vinilo
import com.rodando.app.domain.model.viajeDePrueba
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

    private fun viaje(enCurso: Boolean = false, envio: EstadoEnvio = EstadoEnvio.ENVIADO) = viajeDePrueba(
        inicio = millis(hoy),
        fin = if (enCurso) null else millis(hoy) + 1,
        km = 10.0,
        verificacion = EstadoVerificacion.POR_APROBAR,
        envio = envio
    )

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
        val ui = armarInicio(campania, parcial, listOf(viaje(enCurso = true)), hoy)

        assertFalse(ui.puedeIniciarViaje)
        assertTrue(ui.hayViajeEnCurso)
    }

    @Test
    fun cuenta_los_viajes_pendientes_de_envio() {
        val viajes = listOf(
            viaje(envio = EstadoEnvio.PENDIENTE),
            viaje(envio = EstadoEnvio.PENDIENTE),
            viaje(envio = EstadoEnvio.ENVIADO),
            viaje(enCurso = true, envio = EstadoEnvio.PENDIENTE) // el viaje en curso todavía no se envía
        )

        val ui = armarInicio(campania, parcial, viajes, hoy)

        assertEquals(2, ui.pendientes)
    }

    @Test
    fun muestra_el_estimado_y_lo_confirmado_del_mes() {
        val aprobado = viaje().copy(verificacion = EstadoVerificacion.APROBADO)

        val ui = armarInicio(campania, parcial.copy(fijoMensual = 0.0), listOf(aprobado, viaje()), hoy)

        assertEquals("$1.000", ui.totalMes)
        assertEquals("$500", ui.confirmadoMes)
        assertEquals(2, ui.viajesDelMes)
    }
}
