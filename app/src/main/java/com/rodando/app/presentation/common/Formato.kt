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

    /** "$82.140": pesos sin decimales y con punto de miles. */
    fun monto(valor: Double): String = "$" + miles(Math.round(valor))

    /** "1.500" */
    fun miles(valor: Long): String = String.format(Locale.US, "%,d", valor).replace(',', '.')

    /** "642,8" sin unidad */
    fun decimal(valor: Double): String = String.format(locale, "%.1f", valor)

    /** "1 al 31 de octubre" o "15 de octubre al 14 de noviembre" */
    fun periodo(desde: LocalDate, hasta: LocalDate): String =
        if (desde.month == hasta.month && desde.year == hasta.year) {
            "${desde.dayOfMonth} al ${hasta.dayOfMonth} de ${meses[hasta.monthValue - 1]}"
        } else {
            "${desde.dayOfMonth} de ${meses[desde.monthValue - 1]} al ${hasta.dayOfMonth} de ${meses[hasta.monthValue - 1]}"
        }

    /** "OCTUBRE" */
    fun mes(fecha: LocalDate): String = meses[fecha.monthValue - 1].uppercase(locale)

    /** "42 min" */
    fun duracion(inicio: Long, fin: Long?): String = "${((fin ?: inicio) - inicio) / 60_000} min"

    /** "00:42:15": tiempo transcurrido de un viaje en curso. */
    fun cronometro(millis: Long): String {
        val segundos = (millis / 1000).coerceAtLeast(0)
        return "%02d:%02d:%02d".format(segundos / 3600, segundos / 60 % 60, segundos % 60)
    }

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
