package com.rodando.app.domain.usecase

import com.rodando.app.domain.model.PuntoGps
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Calcula la distancia recorrida (en km) sumando los tramos entre puntos GPS
 * consecutivos con la fórmula de Haversine.
 */
class CalcularDistanciaUseCase {

    operator fun invoke(puntos: List<PuntoGps>): Double =
        puntos.zipWithNext { a, b -> distanciaKm(a, b) }.sum()

    private fun distanciaKm(a: PuntoGps, b: PuntoGps): Double {
        val dLat = Math.toRadians(b.latitud - a.latitud)
        val dLon = Math.toRadians(b.longitud - a.longitud)
        val h = sin(dLat / 2).pow(2) +
            cos(Math.toRadians(a.latitud)) * cos(Math.toRadians(b.latitud)) * sin(dLon / 2).pow(2)
        return 2 * RADIO_TIERRA_KM * asin(sqrt(h))
    }

    companion object {
        const val RADIO_TIERRA_KM = 6371.0
    }
}
