package com.rodando.app.presentation.common

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class FormatoTest {

    private val hoy = LocalDate.of(2026, 10, 1) // jueves

    @Test
    fun los_km_usan_coma_decimal() {
        assertEquals("18,4 km", Formato.km(18.43))
    }

    @Test
    fun la_duracion_se_muestra_en_minutos() {
        assertEquals("42 min", Formato.duracion(0L, 42 * 60_000L))
        assertEquals("0 min", Formato.duracion(1_000L, null))
    }

    @Test
    fun hoy_y_ayer_se_nombran_y_el_resto_lleva_dia_y_numero() {
        assertEquals("Hoy", Formato.diaCorto(hoy, hoy))
        assertEquals("Ayer", Formato.diaCorto(hoy.minusDays(1), hoy))
        assertEquals("Lun 28", Formato.diaCorto(LocalDate.of(2026, 9, 28), hoy))
    }

    @Test
    fun el_titulo_de_un_dia_anterior_lleva_fecha_completa_en_mayusculas() {
        assertEquals("HOY", Formato.tituloDia(hoy, hoy))
        assertEquals("LUNES 28 DE SEPTIEMBRE", Formato.tituloDia(LocalDate.of(2026, 9, 28), hoy))
    }
}
