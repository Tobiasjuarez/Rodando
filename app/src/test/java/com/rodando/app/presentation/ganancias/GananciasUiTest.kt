package com.rodando.app.presentation.ganancias

import com.rodando.app.domain.model.EstadoEnvio
import com.rodando.app.domain.model.EstadoVerificacion
import com.rodando.app.domain.model.TipoVinilo
import com.rodando.app.domain.model.Vinilo
import com.rodando.app.domain.model.viajeDePrueba
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

    private fun viaje(
        id: String,
        dia: LocalDate,
        km: Double,
        verificacion: EstadoVerificacion = EstadoVerificacion.APROBADO,
        envio: EstadoEnvio = EstadoEnvio.ENVIADO
    ) = dia.atTime(8, 30).atZone(Formato.zona).toInstant().toEpochMilli().let { inicio ->
        viajeDePrueba(id = id, inicio = inicio, fin = inicio + 1, km = km, verificacion = verificacion, envio = envio)
    }

    @Test
    fun con_los_objetivos_en_curso_muestra_el_fijo_proporcional_y_lo_que_falta() {
        val viajes = (1..5).map { viaje("v$it", LocalDate.of(2026, 10, it), 37.2) } // 186 km en 5 días

        val ui = armarGanancias(viajes, parcial, hoy)

        assertEquals("OCTUBRE · ESTIMADO", ui.encabezado)
        assertEquals("$30.133", ui.total)
        assertEquals("$20.833 de fijo + $9.300 por 186,0 km", ui.detalle)
        assertEquals("186,0 km", ui.kmPagos)
        assertEquals("5 de 12 días", ui.barraDias.valor)
        assertEquals("Te faltan 7 días con viajes", ui.barraDias.ayuda)
        assertFalse(ui.barraDias.cumplido)
    }

    @Test
    fun con_los_objetivos_cumplidos_cobra_el_fijo_entero() {
        val viajes = (1..13).map { viaje("v$it", LocalDate.of(2026, 10, it), 45.9) } +
            viaje("v14", LocalDate.of(2026, 10, 14), 46.1) // 642,8 km en 14 días

        val ui = armarGanancias(viajes, parcial, hoy)

        assertEquals("$82.140", ui.total)
        assertEquals("$50.000 fijo + $32.140 por 642,8 km", ui.detalle)
        assertEquals("Cumplido", ui.barraDias.ayuda)
        assertTrue(ui.barraDias.cumplido)
        assertEquals(1f, ui.barraDias.progreso)
        assertEquals("1.500", ui.kmTope)
    }

    @Test
    fun separa_lo_confirmado_de_lo_estimado() {
        val viajes = listOf(
            viaje("aprobado", LocalDate.of(2026, 10, 2), 20.0),
            viaje("por-aprobar", LocalDate.of(2026, 10, 3), 10.0, EstadoVerificacion.POR_APROBAR, EstadoEnvio.PENDIENTE)
        )

        val ui = armarGanancias(viajes, parcial.copy(fijoMensual = 0.0), hoy)

        assertEquals("$1.500", ui.total)
        assertEquals("$1.000", ui.confirmado)
        assertEquals(1, ui.aprobados)
        assertEquals(1, ui.porAprobar)
        assertEquals(1, ui.sinEnviar)
    }

    @Test
    fun solo_cuentan_los_viajes_del_mes_actual() {
        val viajes = listOf(
            viaje("anterior", LocalDate.of(2026, 9, 30), 100.0),
            viaje("este", LocalDate.of(2026, 10, 2), 20.0)
        )

        val ui = armarGanancias(viajes, parcial, hoy)

        assertEquals(1, ui.aprobados)
        assertEquals("20,0 km", ui.kmPagos)
    }

    @Test
    fun cuenta_en_revision_y_sin_pago_del_mes() {
        val viajes = listOf(
            viaje("a", LocalDate.of(2026, 10, 2), 18.0, EstadoVerificacion.EN_REVISION),
            viaje("b", LocalDate.of(2026, 10, 3), 0.3, EstadoVerificacion.NO_COMPUTABLE),
            viaje("c", LocalDate.of(2026, 10, 4), 12.0, EstadoVerificacion.RECHAZADO)
        )

        val ui = armarGanancias(viajes, parcial, hoy)

        assertEquals(1, ui.enRevision)
        assertEquals(2, ui.sinPago)
        assertEquals("Te faltan 12 días con viajes", ui.barraDias.ayuda)
    }
}
