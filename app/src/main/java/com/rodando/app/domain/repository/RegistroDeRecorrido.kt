package com.rodando.app.domain.repository

/**
 * Enciende y apaga el registro de posiciones de un viaje.
 * El dominio no sabe si las posiciones vienen del GPS real o de una fuente simulada.
 */
interface RegistroDeRecorrido {
    fun iniciar(viajeId: String)
    fun detener()
}
