package com.rodando.app.domain.usecase

import com.rodando.app.domain.model.PuntoGps

/**
 * Saca del recorrido los saltos imposibles del GPS: una posición vieja que quedó en caché,
 * un rebote entre edificios o, en el emulador, la ubicación por defecto en otro país.
 *
 * Dos puntos seguidos son un salto si para ir de uno al otro habría que superar
 * [VELOCIDAD_MAXIMA_KMH]. Los puntos se agrupan en tramos coherentes; un punto que no encaja
 * en el último tramo se prueba con los anteriores y, si no encaja en ninguno, abre uno nuevo.
 * Al final se descartan los tramos de menos de [MIN_PUNTOS_TRAMO] puntos (si no queda
 * ninguno, se conserva el más largo). La distancia se suma solo dentro de cada tramo.
 */
class LimpiarRecorridoUseCase(
    private val calcularDistancia: CalcularDistanciaUseCase = CalcularDistanciaUseCase()
) {

    /** Los puntos válidos, en orden. */
    operator fun invoke(puntos: List<PuntoGps>): List<PuntoGps> =
        tramos(puntos).flatten().sortedBy { it.timestamp }

    /** Distancia del recorrido sin contar los saltos. */
    fun distanciaKm(puntos: List<PuntoGps>): Double = tramos(puntos).sumOf { calcularDistancia(it) }

    fun tramos(puntos: List<PuntoGps>): List<List<PuntoGps>> {
        if (puntos.size < 2) return listOf(puntos)
        val tramos = mutableListOf(mutableListOf(puntos.first()))
        for (punto in puntos.drop(1)) {
            val tramo = tramos.lastOrNull { !esSalto(it.last(), punto) }
            if (tramo != null) tramo.add(punto) else tramos.add(mutableListOf(punto))
        }
        if (tramos.size == 1) return tramos
        val validos = tramos.filter { it.size >= MIN_PUNTOS_TRAMO }
        return validos.ifEmpty { listOf(tramos.maxBy { it.size }) }
    }

    private fun esSalto(desde: PuntoGps, hasta: PuntoGps): Boolean {
        val metros = calcularDistancia(listOf(desde, hasta)) * 1000
        val segundos = (hasta.timestamp - desde.timestamp) / 1000.0
        // Sin tiempo transcurrido no se puede medir la velocidad: solo se acepta si casi no se movió.
        if (segundos <= 0) return metros > DISTANCIA_SIN_TIEMPO_METROS
        return metros / segundos * 3.6 > VELOCIDAD_MAXIMA_KMH
    }

    companion object {
        const val VELOCIDAD_MAXIMA_KMH = 200.0
        const val MIN_PUNTOS_TRAMO = 3
        private const val DISTANCIA_SIN_TIEMPO_METROS = 100.0
    }
}
