package com.rodando.app.data.sensors

import com.rodando.app.domain.model.PuntoGps
import com.rodando.app.domain.repository.FuenteUbicacion
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Ubicación de prueba: avanza de Ramos Mejía hacia Microcentro, unos 50 m cada 2 segundos
 * (90 km/h, por debajo del límite con el que se descartan los saltos del GPS).
 * Permite probar el flujo completo del viaje en el emulador sin moverse.
 * Se activa con AppContainer.USAR_UBICACION_SIMULADA; si no, se usa el GPS real.
 */
class UbicacionSimulada(
    private val intervaloMs: Long = 2_000,
    private val ahora: () -> Long = System::currentTimeMillis
) : FuenteUbicacion {

    override fun ubicaciones(): Flow<PuntoGps> = flow {
        var paso = 0
        while (true) {
            val zigzag = if (paso % 4 in 1..2) 0.0002 else 0.0
            emit(
                PuntoGps(
                    latitud = INICIO_LAT + paso * PASO_LAT + zigzag,
                    longitud = INICIO_LON + paso * PASO_LON,
                    timestamp = ahora(),
                    precisionMetros = 8f
                )
            )
            paso++
            delay(intervaloMs)
        }
    }

    private companion object {
        const val INICIO_LAT = -34.6415
        const val INICIO_LON = -58.5650
        const val PASO_LAT = 0.0001
        const val PASO_LON = 0.0005 // unos 50 m por paso
    }
}
