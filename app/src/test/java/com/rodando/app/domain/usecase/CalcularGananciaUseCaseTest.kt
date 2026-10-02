package com.rodando.app.domain.usecase

import com.rodando.app.domain.model.EstadoViaje
import com.rodando.app.domain.model.TipoVinilo
import com.rodando.app.domain.model.Viaje
import org.junit.Assert.assertEquals
import org.junit.Test

class CalcularGananciaUseCaseTest {

    private val calcular = CalcularGananciaUseCase()

    private fun viaje(km: Double, estado: EstadoViaje) = Viaje(
        id = "v-$km-$estado",
        campaniaId = "c1",
        tipoVinilo = TipoVinilo.PARCIAL,
        inicio = 0L,
        fin = 1L,
        distanciaKm = km,
        estado = estado,
        fotoInicioPath = "inicio.jpg",
        fotoFinPath = "fin.jpg",
        intentosFoto = 1
    )

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

        val resumen = calcular(viajes, tarifaPorKm = 100.0)

        assertEquals(15.0, resumen.kmComputables, 0.0001)
        assertEquals(2, resumen.viajesComputables)
        assertEquals(1, resumen.viajesPendientes)
        assertEquals(1500.0, resumen.totalEstimado, 0.0001)
    }

    @Test
    fun sin_viajes_la_ganancia_es_cero() {
        val resumen = calcular(emptyList(), tarifaPorKm = 100.0)

        assertEquals(0.0, resumen.totalEstimado, 0.0001)
        assertEquals(0, resumen.viajesComputables)
    }
}
