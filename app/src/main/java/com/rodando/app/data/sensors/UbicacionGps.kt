package com.rodando.app.data.sensors

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.rodando.app.domain.model.PuntoGps
import com.rodando.app.domain.repository.FuenteUbicacion
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Posiciones reales con Fused Location. Las actualizaciones se piden solo mientras alguien
 * recolecta el Flow y se cancelan al terminar el viaje (RNF04: GPS solo durante el viaje).
 */
class UbicacionGps(
    context: Context,
    private val intervaloMs: Long = 5_000,
    private val distanciaMinimaMetros: Float = 10f
) : FuenteUbicacion {

    private val cliente = LocationServices.getFusedLocationProviderClient(context.applicationContext)

    // El permiso se verifica antes de encender el servicio del viaje (ver Permisos).
    @SuppressLint("MissingPermission")
    override fun ubicaciones(): Flow<PuntoGps> = callbackFlow {
        val pedido = pedido(intervaloMs, distanciaMinimaMetros)
        val callback = object : LocationCallback() {
            override fun onLocationResult(resultado: LocationResult) {
                resultado.locations.forEach { ubicacion ->
                    trySend(
                        PuntoGps(
                            latitud = ubicacion.latitude,
                            longitud = ubicacion.longitude,
                            timestamp = ubicacion.time,
                            precisionMetros = ubicacion.accuracy
                        )
                    )
                }
            }
        }
        cliente.requestLocationUpdates(pedido, callback, Looper.getMainLooper())
        awaitClose { cliente.removeLocationUpdates(callback) }
    }

    companion object {
        /** Mismo pedido para registrar el viaje y para revisar que la ubicación del teléfono esté activada. */
        fun pedido(intervaloMs: Long = 5_000, distanciaMinimaMetros: Float = 10f): LocationRequest =
            LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, intervaloMs)
                .setMinUpdateDistanceMeters(distanciaMinimaMetros)
                .build()
    }
}
