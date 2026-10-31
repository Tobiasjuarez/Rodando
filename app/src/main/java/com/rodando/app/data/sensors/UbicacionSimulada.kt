package com.rodando.app.data.sensors

import com.rodando.app.domain.model.PuntoGps
import com.rodando.app.domain.repository.FuenteUbicacion
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Ubicación de prueba: avanza de Ramos Mejía hacia Microcentro, unos 200 m cada 2 segundos.
 * Permite probar el flujo completo del viaje en el emulador sin moverse.
 * Se reemplaza por el GPS real (Fused Location en un servicio en primer plano).
 */
class UbicacionSimulada(
    private val intervaloMs: Long = 2_000,
    private val ahora: () -> Long = System::currentTimeMillis
) : FuenteUbicacion {

    override fun ubicaciones(): Flow<PuntoGps> = flow {
        var paso = 0
        while (true) {
            val zigzag = if (paso % 4 in 1..2) 0.0008 else 0.0
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
        const val PASO_LAT = 0.0004
        const val PASO_LON = 0.0020 // unos 200 m por paso
    }
}
