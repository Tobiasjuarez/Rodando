package com.rodando.app.presentation.navegacion

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.rodando.app.R

/**
 * Destinos de la barra de navegación inferior.
 * El orden del enum es el orden en que aparecen en la barra.
 */
enum class Destino(
    val ruta: String,
    @StringRes val etiqueta: Int,
    @DrawableRes val icono: Int
) {
    INICIO("inicio", R.string.destino_inicio, R.drawable.ic_inicio),
    HISTORIAL("historial", R.string.destino_historial, R.drawable.ic_historial),
    GANANCIAS("ganancias", R.string.destino_ganancias, R.drawable.ic_ganancias);

    companion object {
        /** Pantalla con la que abre la app. */
        val inicial = INICIO
    }
}
