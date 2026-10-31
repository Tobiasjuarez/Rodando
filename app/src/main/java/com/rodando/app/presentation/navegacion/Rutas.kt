package com.rodando.app.presentation.navegacion

/** Rutas que no están en la barra inferior. */
object Rutas {
    const val ARG_VIAJE_ID = "viajeId"
    const val DETALLE_VIAJE = "viaje/{$ARG_VIAJE_ID}"

    fun detalleViaje(viajeId: String) = "viaje/$viajeId"
}
