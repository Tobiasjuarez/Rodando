package com.rodando.app.presentation.detalle

import com.rodando.app.domain.model.EstadoViaje
import com.rodando.app.domain.model.TipoVinilo
import com.rodando.app.domain.model.Viaje
import com.rodando.app.domain.model.Vinilo
import com.rodando.app.presentation.common.Formato
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class DetalleViajeUiTest {

    private val hoy = LocalDate.of(2026, 10, 1)
    private val inicio = hoy.atTime(8, 42).atZone(Formato.zona).toInstant().toEpochMilli()

    private fun viaje(fin: Long?) =
        Viaje("v", "c", TipoVinilo.PARCIAL, inicio, fin, 18.4, EstadoViaje.SINCRONIZADO, "", null, 1)

    @Test
    fun un_viaje_finalizado_muestra_horario_distancia_y_duracion() {
        val detalle = armarDetalle(viaje(fin = inicio + 42 * 60_000L), emptyList(), hoy)

        assertEquals("Hoy · 08:42 a 09:24", detalle.horario)
        assertEquals("18,4 km", detalle.distancia)
        assertEquals("42 min", detalle.duracion)
        assertEquals("09:24", detalle.horaFin)
    }

    @Test
    fun un_viaje_en_curso_no_tiene_hora_de_fin() {
        val detalle = armarDetalle(viaje(fin = null), emptyList(), hoy)

        assertEquals("Hoy · desde las 08:42", detalle.horario)
        assertNull(detalle.horaFin)
    }

    @Test
    fun con_el_vinilo_muestra_lo_que_suma_el_viaje_por_km() {
        val vinilo = Vinilo(TipoVinilo.PARCIAL, 50_000.0, 50.0, fechaInstalacion = 0)

        val computable = armarDetalle(viaje(fin = inicio + 1), emptyList(), hoy, vinilo)
        val enCurso = armarDetalle(viaje(fin = null).copy(estado = EstadoViaje.EN_CURSO), emptyList(), hoy, vinilo)

        assertEquals("+$920", computable.ganancia)
        assertNull(enCurso.ganancia)
    }
}
