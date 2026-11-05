package com.rodando.app.data.sensors

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

/**
 * Permisos de la app: ubicación y notificaciones para el viaje, y red local para hablar
 * con la API simulada que corre en la computadora.
 */
object Permisos {

    /**
     * Desde Android 17 (API 37), una app necesita este permiso para conectarse a direcciones
     * de la red local, como 10.0.2.2 (la computadora vista desde el emulador). Sin él, Android
     * descarta la conexión en silencio y el pedido termina en un timeout.
     */
    const val RED_LOCAL = "android.permission.ACCESS_LOCAL_NETWORK"

    fun necesitaRedLocal(context: Context): Boolean =
        Build.VERSION.SDK_INT >= 37 && !concedido(context, RED_LOCAL)

    val paraViaje: Array<String> = buildList {
        add(Manifest.permission.ACCESS_FINE_LOCATION)
        add(Manifest.permission.ACCESS_COARSE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) add(Manifest.permission.POST_NOTIFICATIONS)
    }.toTypedArray()

    /** Alcanza con la ubicación aproximada, aunque el recorrido sale menos preciso. */
    fun tieneUbicacion(context: Context): Boolean =
        concedido(context, Manifest.permission.ACCESS_FINE_LOCATION) ||
            concedido(context, Manifest.permission.ACCESS_COARSE_LOCATION)

    /** true si hay que mostrar el pedido de permisos antes de iniciar un viaje. */
    fun faltaAlguno(context: Context): Boolean =
        !tieneUbicacion(context) ||
            (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                !concedido(context, Manifest.permission.POST_NOTIFICATIONS))

    private fun concedido(context: Context, permiso: String) =
        ContextCompat.checkSelfPermission(context, permiso) == PackageManager.PERMISSION_GRANTED
}
