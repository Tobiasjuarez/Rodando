package com.rodando.app.domain.usecase

import com.rodando.app.domain.model.EstadoViaje
import com.rodando.app.domain.model.TipoVinilo
import com.rodando.app.domain.model.Viaje
import com.rodando.app.domain.model.Vinilo
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class CalcularGananciaUseCaseTest {

    private val zona = ZoneId.of("America/Argentina/Buenos_Aires")
    private val calcular = CalcularGananciaUseCase(zona)

    /** Vinilo Parcial de la tarifa propuesta: $50.000 fijo + $50 por km. */
    private val parcial = Vinilo(TipoVinilo.PARCIAL, fijoMensual = 50_000.0, tarifaPorKm = 50.0, fechaInstalacion = 0L)

    private fun viaje(km: Double, estado: EstadoViaje = EstadoViaje.SINCRONIZADO, dia: Int = 1): Viaje {
        val inicio = LocalDate.of(2026, 10, dia).atTime(8, 30).atZone(zona).toInstant().toEpochMilli()
        return Viaje(
            id = "v-$dia-$km-$estado",
            campaniaId = "c1",
            tipoVinilo = TipoVinilo.PARCIAL,
            inicio = inicio,
            fin = inicio + 1,
            distanciaKm = km,
            estado = estado,
            fotoInicioPath = "inicio.jpg",
            fotoFinPath = "fin.jpg",
            intentosFoto = 1
        )
    }

    private fun viajesEnDias(dias: Int, kmPorDia: Double) = (1..dias).map { viaje(kmPorDia, dia = it) }

    @Test
    fun suma_solo_los_viajes_finalizados_y_con_distancia_minima() {
        val viajes = listOf(
            viaje(10.0, EstadoViaje.SINCRONIZADO),
            viaje(5.0, EstadoViaje.PENDIENTE),
            viaje(0.3, EstadoViaje.PENDIENTE),     // menos de 0,5 km: no computa
            viaje(8.0, EstadoViaje.EN_CURSO),      // no finalizado: no computa
            viaje(4.0, EstadoViaje.NO_COMPUTABLE),
            viaje(6.0, EstadoViaje.EN_REVISION)    // en revisión: no computa hasta aprobarse
        )
        val soloKm = parcial.copy(fijoMensual = 0.0, tarifaPorKm = 100.0)

        val resumen = calcular(viajes, soloKm)

        assertEquals(15.0, resumen.kmComputables, 0.0001)
        assertEquals(2, resumen.viajesComputables)
        assertEquals(1, resumen.viajesPendientes)
        assertEquals(1, resumen.diasConViajes)
        assertEquals(1500.0, resumen.totalEstimado, 0.0001)
    }

    @Test
    fun con_400_km_y_12_dias_cobra_el_fijo_entero() {
        val resumen = calcular(viajesEnDias(12, 40.0), parcial) // 480 km en 12 días

        assertEquals(12, resumen.diasConViajes)
        assertEquals(50_000.0, resumen.fijoCobrado, 0.0001)
        assertEquals(50_000.0 + 480 * 50.0, resumen.totalEstimado, 0.0001)
    }

    @Test
    fun por_debajo_del_minimo_el_fijo_es_proporcional_a_la_condicion_mas_baja() {
        val resumen = calcular(viajesEnDias(6, 100.0), parcial) // 600 km pero solo 6 días

        assertEquals(25_000.0, resumen.fijoCobrado, 0.0001)
        assertEquals(25_000.0 + 600 * 50.0, resumen.totalEstimado, 0.0001)
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
        assertEquals(50_000.0 + 1500 * 50.0, resumen.totalEstimado, 0.0001)
    }

    @Test
    fun sin_viajes_la_ganancia_es_cero() {
        val resumen = calcular(emptyList(), parcial)

        assertEquals(0.0, resumen.totalEstimado, 0.0001)
        assertEquals(0, resumen.viajesComputables)
    }
}
