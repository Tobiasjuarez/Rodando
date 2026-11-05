package com.rodando.app.presentation.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MuestreoTest {

    @Test
    fun una_foto_de_12_mp_se_lee_a_la_mitad() {
        // 4.000 × 3.000 → 2.000 × 1.500: achicarla más dejaría el lado largo por debajo de 1.024 px.
        assertEquals(2, muestreoPara(4_000, 3_000, 1_024))
        assertEquals(4, muestreoPara(8_000, 6_000, 1_024))
    }

    @Test
    fun una_foto_chica_se_lee_entera() {
        assertEquals(1, muestreoPara(800, 600, 1_024))
    }

    @Test
    fun la_orientacion_exif_indica_cuanto_girar() {
        assertEquals(90f, gradosSegunExif(6))
        assertEquals(180f, gradosSegunExif(3))
        assertEquals(270f, gradosSegunExif(8))
        assertEquals(0f, gradosSegunExif(1))
    }

    @Test
    fun las_fotos_simuladas_y_vacias_no_son_reales() {
        assertTrue(esFotoReal("/data/user/0/com.rodando.app/files/fotos/vinilo-1.jpg"))
        assertFalse(esFotoReal("simulada://vinilo"))
        assertFalse(esFotoReal(""))
        assertFalse(esFotoReal(null))
    }
}
