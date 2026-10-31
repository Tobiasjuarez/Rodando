package com.rodando.app.presentation.navegacion

import org.junit.Assert.assertEquals
import org.junit.Test

class DestinoTest {

    @Test
    fun la_app_abre_en_inicio() {
        assertEquals(Destino.INICIO, Destino.inicial)
    }

    @Test
    fun cada_destino_tiene_una_ruta_unica() {
        val rutas = Destino.entries.map { it.ruta }
        assertEquals(rutas.size, rutas.toSet().size)
    }

    @Test
    fun la_barra_muestra_inicio_historial_y_ganancias_en_ese_orden() {
        assertEquals(listOf("inicio", "historial", "ganancias"), Destino.entries.map { it.ruta })
    }
}
