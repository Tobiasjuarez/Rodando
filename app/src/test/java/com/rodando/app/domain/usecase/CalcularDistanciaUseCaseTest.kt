package com.rodando.app.domain.usecase

import com.rodando.app.domain.model.PuntoGps
import org.junit.Assert.assertEquals
import org.junit.Test

class CalcularDistanciaUseCaseTest {

    private val calcular = CalcularDistanciaUseCase()

    private fun punto(lat: Double, lon: Double) = PuntoGps(lat, lon, timestamp = 0L, precisionMetros = 5f)

    @Test
    fun sin_puntos_o_con_uno_solo_la_distancia_es_cero() {
        assertEquals(0.0, calcular(emptyList()), 0.0001)
        assertEquals(0.0, calcular(listOf(punto(-34.60, -58.38))), 0.0001)
    }

    @Test
    fun suma_los_tramos_entre_puntos_consecutivos() {
        // 0,01 grados de latitud son aproximadamente 1,112 km
        val puntos = listOf(
            punto(-34.60, -58.38),
            punto(-34.61, -58.38),
            punto(-34.62, -58.38)
        )

        assertEquals(2.224, calcular(puntos), 0.001)
    }
}
