package com.rodando.app.presentation.common

/**
 * Estados posibles de una pantalla, según el diseño de experiencia:
 * carga -> contenido -> vacío -> error -> offline.
 *
 * Cada ViewModel expone un StateFlow<UiState<T>> y la pantalla solo dibuja el estado.
 */
sealed interface UiState<out T> {
    data object Cargando : UiState<Nothing>
    data class Contenido<T>(val datos: T) : UiState<T>
    data object Vacio : UiState<Nothing>
    data class Error(val mensaje: String) : UiState<Nothing>

    /** Sin conexión, mostrando los últimos datos guardados en el teléfono. */
    data class Offline<T>(val datosLocales: T, val ultimaSincronizacion: Long?) : UiState<T>
}
