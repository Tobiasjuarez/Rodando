package com.rodando.app.presentation.historial

import com.rodando.app.domain.model.EstadoEnvio
import com.rodando.app.domain.model.EstadoVerificacion
import com.rodando.app.domain.model.Viaje
import com.rodando.app.domain.model.viajeDePrueba
import com.rodando.app.presentation.common.Formato
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class HistorialUiTest {

    private val hoy = LocalDate.of(2026, 10, 1)

    private fun viaje(
        id: String,
        dia: LocalDate,
        hora: LocalTime,
        km: Double,
        verificacion: EstadoVerificacion,
        envio: EstadoEnvio
    ): Viaje {
        val inicio = dia.atTime(hora).atZone(Formato.zona).toInstant().toEpochMilli()
        return viajeDePrueba(id = id, inicio = inicio, km = km, verificacion = verificacion, envio = envio)
    }

    private val viajes = listOf(
        viaje("a", hoy.minusDays(3), LocalTime.of(18, 22), 18.1, EstadoVerificacion.APROBADO, EstadoEnvio.ENVIADO),
        viaje("b", hoy, LocalTime.of(8, 42), 18.4, EstadoVerificacion.POR_APROBAR, EstadoEnvio.PENDIENTE),
        viaje("c", hoy.minusDays(1), LocalTime.of(19, 5), 0.3, EstadoVerificacion.NO_COMPUTABLE, EstadoEnvio.ENVIADO),
        viaje("d", hoy, LocalTime.of(18, 10), 17.9, EstadoVerificacion.POR_APROBAR, EstadoEnvio.PENDIENTE)
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

    @Test
    fun cada_viaje_lleva_su_aprobacion_y_su_envio_por_separado() {
        val aprobado = armarHistorial(viajes, hoy).grupos.last().viajes.single()

        assertEquals(EstadoVerificacion.APROBADO, aprobado.verificacion)
        assertEquals(EstadoEnvio.ENVIADO, aprobado.envio)
    }
}
