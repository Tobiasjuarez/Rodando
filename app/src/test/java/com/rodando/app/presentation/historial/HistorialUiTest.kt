package com.rodando.app.presentation.historial

import com.rodando.app.domain.model.EstadoViaje
import com.rodando.app.domain.model.TipoVinilo
import com.rodando.app.domain.model.Viaje
import com.rodando.app.presentation.common.Formato
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class HistorialUiTest {

    private val hoy = LocalDate.of(2026, 10, 1)

    private fun viaje(id: String, dia: LocalDate, hora: LocalTime, km: Double, estado: EstadoViaje): Viaje {
        val inicio = dia.atTime(hora).atZone(Formato.zona).toInstant().toEpochMilli()
        return Viaje(id, "c", TipoVinilo.PARCIAL, inicio, inicio + 42 * 60_000L, km, estado, "", "", 1)
    }

    private val viajes = listOf(
        viaje("a", hoy.minusDays(3), LocalTime.of(18, 22), 18.1, EstadoViaje.SINCRONIZADO),
        viaje("b", hoy, LocalTime.of(8, 42), 18.4, EstadoViaje.PENDIENTE),
        viaje("c", hoy.minusDays(1), LocalTime.of(19, 5), 0.3, EstadoViaje.NO_COMPUTABLE),
        viaje("d", hoy, LocalTime.of(18, 10), 17.9, EstadoViaje.PENDIENTE)
    )

    @Test
    fun agrupa_por_dia_del_mas_nuevo_al_mas_viejo() {
        val ui = armarHistorial(viajes, hoy)

        assertEquals(listOf("HOY", "AYER", "LUNES 28 DE SEPTIEMBRE"), ui.grupos.map { it.titulo })
        assertEquals(listOf("d", "b"), ui.grupos[0].viajes.map { it.id })
    }

    @Test
    fun cada_viaje_muestra_dia_hora_km_y_duracion() {
        val primero = armarHistorial(viajes, hoy).grupos[0].viajes[0]

        assertEquals("Hoy · 18:10", primero.fecha)
        assertEquals("17,9 km · 42 min", primero.detalle)
    }

    @Test
    fun cuenta_los_viajes_pendientes_de_envio() {
        assertEquals(2, armarHistorial(viajes, hoy).pendientes)
    }
}
