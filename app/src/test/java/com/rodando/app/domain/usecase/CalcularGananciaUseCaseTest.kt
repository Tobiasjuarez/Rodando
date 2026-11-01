package com.rodando.app.domain.usecase

import com.rodando.app.domain.model.EstadoEnvio
import com.rodando.app.domain.model.EstadoVerificacion
import com.rodando.app.domain.model.TipoVinilo
import com.rodando.app.domain.model.Vinilo
import com.rodando.app.domain.model.viajeDePrueba
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class CalcularGananciaUseCaseTest {

    private val zona = ZoneId.of("America/Argentina/Buenos_Aires")
    private val calcular = CalcularGananciaUseCase(zona)

    /** Vinilo Parcial de la tarifa propuesta: $50.000 fijo + $50 por km. */
    private val parcial = Vinilo(TipoVinilo.PARCIAL, fijoMensual = 50_000.0, tarifaPorKm = 50.0, fechaInstalacion = 0L)
    private val soloKm = parcial.copy(fijoMensual = 0.0)

    private fun viaje(
        km: Double,
        verificacion: EstadoVerificacion = EstadoVerificacion.APROBADO,
        dia: Int = 1,
        tarifa: Double = 50.0,
        terminado: Boolean = true,
        envio: EstadoEnvio = EstadoEnvio.ENVIADO
    ) = LocalDate.of(2026, 10, dia).atTime(8, 30).atZone(zona).toInstant().toEpochMilli().let { inicio ->
        viajeDePrueba(
            id = "v-$dia-$km-$verificacion",
            inicio = inicio,
            fin = if (terminado) inicio + 1 else null,
            km = km,
            verificacion = verificacion,
            envio = envio,
            tarifaPorKm = tarifa
        )
    }

    private fun viajesEnDias(dias: Int, kmPorDia: Double) = (1..dias).map { viaje(kmPorDia, dia = it) }

    @Test
    fun el_confirmado_suma_solo_aprobados_y_el_estimado_suma_tambien_los_por_aprobar() {
        val viajes = listOf(
            viaje(10.0, EstadoVerificacion.APROBADO),
            viaje(5.0, EstadoVerificacion.POR_APROBAR),
            viaje(6.0, EstadoVerificacion.EN_REVISION),   // no suma hasta que se apruebe
            viaje(7.0, EstadoVerificacion.RECHAZADO),
            viaje(0.3, EstadoVerificacion.NO_COMPUTABLE),
            viaje(8.0, EstadoVerificacion.POR_APROBAR, terminado = false) // en curso
        )

        val resumen = calcular(viajes, soloKm)

        assertEquals(500.0, resumen.confirmado, 0.0001)
        assertEquals(750.0, resumen.estimado, 0.0001)
        assertEquals(1, resumen.viajesAprobados)
        assertEquals(1, resumen.viajesPorAprobar)
        assertEquals(1, resumen.viajesEnRevision)
        assertEquals(2, resumen.viajesSinPago)
    }

    @Test
    fun enviado_y_aprobado_son_independientes() {
        // Un viaje sin enviar puede estar aprobado (por ejemplo, revisado en el local) y uno enviado, sin aprobar.
        val viajes = listOf(
            viaje(10.0, EstadoVerificacion.POR_APROBAR, dia = 1, envio = EstadoEnvio.ENVIADO),
            viaje(10.0, EstadoVerificacion.APROBADO, dia = 2, envio = EstadoEnvio.PENDIENTE)
        )

        val resumen = calcular(viajes, soloKm)

        assertEquals(500.0, resumen.confirmado, 0.0001)
        assertEquals(1, resumen.viajesSinEnviar)
    }

    @Test
    fun cada_viaje_se_paga_con_la_tarifa_que_tenia_al_iniciarse() {
        // La tarifa del vinilo subió a $60 a mitad de mes: los viajes viejos se siguen pagando a $50.
        val viajes = listOf(viaje(10.0, dia = 1, tarifa = 50.0), viaje(10.0, dia = 20, tarifa = 60.0))

        val resumen = calcular(viajes, soloKm.copy(tarifaPorKm = 60.0))

        assertEquals(1_100.0, resumen.estimado, 0.0001)
    }

    @Test
    fun con_12_dias_con_viajes_cobra_el_fijo_entero() {
        val resumen = calcular(viajesEnDias(12, 40.0), parcial) // 480 km en 12 días

        assertEquals(12, resumen.diasConViajes)
        assertEquals(50_000.0, resumen.fijo, 0.0001)
        assertEquals(50_000.0 + 480 * 50.0, resumen.estimado, 0.0001)
    }

    @Test
    fun con_menos_de_12_dias_el_fijo_es_proporcional_a_los_dias() {
        val resumen = calcular(viajesEnDias(6, 100.0), parcial) // 600 km pero solo 6 días

        assertEquals(25_000.0, resumen.fijo, 0.0001)
        assertEquals(25_000.0 + 600 * 50.0, resumen.estimado, 0.0001)
    }

    @Test
    fun hacer_pocos_km_no_reduce_el_fijo() {
        // Recorridos cortos todos los días: no hay mínimo de km que obligue a manejar de más.
        val resumen = calcular(viajesEnDias(12, 3.0), parcial) // 36 km en 12 días

        assertEquals(50_000.0, resumen.fijo, 0.0001)
        assertEquals(50_000.0 + 36 * 50.0, resumen.estimado, 0.0001)
    }

    @Test
    fun varios_viajes_el_mismo_dia_cuentan_como_un_dia() {
        val viajes = listOf(viaje(20.0, dia = 3), viaje(18.0, dia = 3), viaje(25.0, dia = 4))

        assertEquals(2, calcular(viajes, parcial).diasConViajes)
    }

    @Test
    fun se_pagan_como_maximo_1500_km() {
        val resumen = calcular(viajesEnDias(20, 100.0), parcial) // 2.000 km

        assertEquals(2000.0, resumen.kmComputables, 0.0001)
        assertEquals(1500.0, resumen.kmPagados, 0.0001)
        assertEquals(50_000.0 + 1500 * 50.0, resumen.estimado, 0.0001)
    }

    @Test
    fun el_tope_se_aplica_en_orden_cronologico_con_la_tarifa_de_cada_viaje() {
        // 1.400 km a $50 y después 200 km a $60: solo entran 100 km de los últimos.
        val viajes = listOf(viaje(1_400.0, dia = 1, tarifa = 50.0), viaje(200.0, dia = 2, tarifa = 60.0))

        val resumen = calcular(viajes, soloKm)

        assertEquals(1_400 * 50.0 + 100 * 60.0, resumen.estimado, 0.0001)
    }

    @Test
    fun sin_viajes_la_ganancia_es_cero() {
        val resumen = calcular(emptyList(), parcial)

        assertEquals(0.0, resumen.estimado, 0.0001)
        assertEquals(0.0, resumen.confirmado, 0.0001)
        assertEquals(0, resumen.viajesAprobados)
    }

    @Test
    fun informa_cuanto_falta_para_los_objetivos_del_mes() {
        val resumen = calcular(viajesEnDias(5, 37.2), parcial) // 186 km en 5 días

        assertEquals(7.0, resumen.objetivoDias.faltante, 0.0001)
        assertEquals(5.0 / 12, resumen.objetivoDias.progreso, 0.0001)
        assertFalse(resumen.objetivoDias.cumplido)
        assertEquals(1314.0, resumen.kmPagosRestantes, 0.0001)
    }

    @Test
    fun con_los_objetivos_cumplidos_el_progreso_no_pasa_del_100() {
        val resumen = calcular(viajesEnDias(14, 45.0), parcial) // 630 km en 14 días

        assertEquals(1.0, resumen.objetivoDias.progreso, 0.0001)
        assertEquals(0.0, resumen.objetivoDias.faltante, 0.0001)
        assertTrue(resumen.objetivoDias.cumplido)
        assertEquals(870.0, resumen.kmPagosRestantes, 0.0001)
    }
}
