package com.rodando.app.presentation.navegacion

/** Rutas que no están en la barra inferior. Cada una tiene un prefijo propio para no chocar con otra. */
object Rutas {
    const val ARG_VIAJE_ID = "viajeId"
    const val DETALLE_VIAJE = "viaje/{$ARG_VIAJE_ID}"
    const val FOTO_INICIO = "foto-inicio"
    const val VIAJE_EN_CURSO = "en-curso"
    const val FOTO_FIN = "foto-fin/{$ARG_VIAJE_ID}"
    const val VIAJE_GUARDADO = "guardado/{$ARG_VIAJE_ID}"

    fun detalleViaje(viajeId: String) = "viaje/$viajeId"
    fun fotoFin(viajeId: String) = "foto-fin/$viajeId"
    fun viajeGuardado(viajeId: String) = "guardado/$viajeId"
}
