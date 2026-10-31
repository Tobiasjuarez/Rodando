package com.rodando.app.presentation.common

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale

/**
 * Formatos de fecha, hora y distancia que usa la UI, en español de Argentina.
 * Los nombres de días y meses están escritos a mano para que el resultado no dependa
 * de la versión de Android.
 */
object Formato {

    val zona: ZoneId = ZoneId.of("America/Argentina/Buenos_Aires")
    private val locale: Locale = Locale.forLanguageTag("es-AR")

    private val diasCortos = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")
    private val diasLargos = listOf("lunes", "martes", "miércoles", "jueves", "viernes", "sábado", "domingo")
    private val meses = listOf(
        "enero", "febrero", "marzo", "abril", "mayo", "junio",
        "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre"
    )

    fun fecha(millis: Long): LocalDate = Instant.ofEpochMilli(millis).atZone(zona).toLocalDate()

    /** "18,4 km" */
    fun km(valor: Double): String = String.format(locale, "%.1f km", valor)

    /** "42 min" */
    fun duracion(inicio: Long, fin: Long?): String = "${((fin ?: inicio) - inicio) / 60_000} min"

    /** "08:42" */
    fun hora(millis: Long): String {
        val t = Instant.ofEpochMilli(millis).atZone(zona).toLocalTime()
        return "%02d:%02d".format(t.hour, t.minute)
    }

    /** "Hoy", "Ayer" o "Lun 28" */
    fun diaCorto(fecha: LocalDate, hoy: LocalDate): String = when (fecha) {
        hoy -> "Hoy"
        hoy.minusDays(1) -> "Ayer"
        else -> "${diasCortos[fecha.dayOfWeek.value - 1]} ${fecha.dayOfMonth}"
    }

    /** Encabezado de grupo: "HOY", "AYER" o "LUNES 28 DE SEPTIEMBRE" */
    fun tituloDia(fecha: LocalDate, hoy: LocalDate): String = when (fecha) {
        hoy -> "HOY"
        hoy.minusDays(1) -> "AYER"
        else -> "${diasLargos[fecha.dayOfWeek.value - 1]} ${fecha.dayOfMonth} de ${meses[fecha.monthValue - 1]}"
            .uppercase(locale)
    }
}
