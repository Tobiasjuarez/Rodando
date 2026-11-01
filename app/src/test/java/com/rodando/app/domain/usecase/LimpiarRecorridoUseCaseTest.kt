package com.rodando.app.domain.usecase

import com.rodando.app.domain.model.PuntoGps
import org.junit.Assert.assertEquals
import org.junit.Test

class LimpiarRecorridoUseCaseTest {

    private val limpiar = LimpiarRecorridoUseCase()

    /** Puntos cada 30 s que avanzan 0,0025° de latitud (unos 280 m, 33 km/h). */
    private fun recorrido(desdeSegundo: Int, cantidad: Int, latInicial: Double = -34.6415) =
        (0 until cantidad).map { i ->
            PuntoGps(latInicial - i * 0.0025, -58.5650, (desdeSegundo + i * 30) * 1000L, 5f)
        }

    @Test
    fun un_recorrido_normal_queda_igual() {
        val puntos = recorrido(0, 5)

        assertEquals(puntos, limpiar(puntos))
        assertEquals(1.11, limpiar.distanciaKm(puntos), 0.01)
    }

    @Test
    fun descarta_una_posicion_inicial_en_otro_pais() {
        // Como el emulador: arranca con la ubicación por defecto en California.
        val california = PuntoGps(37.4220, -122.0841, 0, 5f)
        val puntos = listOf(california) + recorrido(5, 5)

        assertEquals(recorrido(5, 5), limpiar(puntos))
        assertEquals(1.11, limpiar.distanciaKm(puntos), 0.01)
    }

    @Test
    fun descarta_un_rebote_aislado_en_medio_del_recorrido() {
        val normal = recorrido(0, 6)
        val rebote = PuntoGps(-34.50, -58.40, 75_000, 5f)
        val puntos = normal.take(3) + rebote + normal.drop(3)

        assertEquals(normal, limpiar(puntos))
        assertEquals(1.39, limpiar.distanciaKm(puntos), 0.01)
    }

    @Test
    fun un_recorrido_corto_sin_saltos_se_mantiene() {
        val puntos = recorrido(0, 2)

        assertEquals(puntos, limpiar(puntos))
    }
}
