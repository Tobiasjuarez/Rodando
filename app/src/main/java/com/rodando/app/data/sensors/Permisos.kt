package com.rodando.app.data.sensors

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

/** Permisos que necesita un viaje: ubicación para el recorrido y, desde Android 13, notificaciones. */
object Permisos {

    val paraViaje: Array<String> = buildList {
        add(Manifest.permission.ACCESS_FINE_LOCATION)
        add(Manifest.permission.ACCESS_COARSE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) add(Manifest.permission.POST_NOTIFICATIONS)
    }.toTypedArray()

    /** Alcanza con la ubicación aproximada, aunque el recorrido sale menos preciso. */
    fun tieneUbicacion(context: Context): Boolean =
        concedido(context, Manifest.permission.ACCESS_FINE_LOCATION) ||
            concedido(context, Manifest.permission.ACCESS_COARSE_LOCATION)

    private fun concedido(context: Context, permiso: String) =
        ContextCompat.checkSelfPermission(context, permiso) == PackageManager.PERMISSION_GRANTED
}
