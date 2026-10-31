package com.rodando.app.data.local

import com.rodando.app.domain.model.EstadoViaje
import com.rodando.app.domain.model.TipoVinilo
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

/**
 * Carga viajes de ejemplo la primera vez que se abre la app, para poder probar
 * Historial y Detalle antes de tener GPS, cámara y API.
 *
 * Se quita cuando la campaña y los viajes vengan de la API simulada.
 */
class DatosDePrueba(
    private val dao: ViajeDao,
    private val zona: ZoneId = ZoneId.of("America/Argentina/Buenos_Aires"),
    private val hoy: () -> LocalDate = { LocalDate.now(ZoneId.of("America/Argentina/Buenos_Aires")) }
) {

    suspend fun cargarSiHaceFalta() {
        if (dao.contar() > 0) return
        val dia = hoy()
        // Recorridos de Lucía entre Ramos Mejía y Microcentro (ver escenario de la preentrega).
        val viajes = listOf(
            Ejemplo("demo-1", dia, LocalTime.of(18, 10), 39, 17.9, EstadoViaje.PENDIENTE, vuelta = true),
            Ejemplo("demo-2", dia, LocalTime.of(8, 42), 42, 18.4, EstadoViaje.PENDIENTE),
            Ejemplo("demo-3", dia.minusDays(1), LocalTime.of(19, 5), 3, 0.3, EstadoViaje.NO_COMPUTABLE, corto = true),
            Ejemplo("demo-4", dia.minusDays(1), LocalTime.of(8, 37), 45, 18.6, EstadoViaje.SINCRONIZADO),
            Ejemplo("demo-5", dia.minusDays(4), LocalTime.of(18, 22), 40, 18.1, EstadoViaje.SINCRONIZADO, vuelta = true),
            Ejemplo("demo-6", dia.minusDays(4), LocalTime.of(8, 30), 44, 18.3, EstadoViaje.SINCRONIZADO)
        )
        viajes.forEach { guardar(it) }
    }

    private suspend fun guardar(e: Ejemplo) {
        val inicio = e.dia.atTime(e.hora).atZone(zona).toInstant().toEpochMilli()
        val fin = inicio + e.minutos * 60_000L
        dao.guardar(
            ViajeEntity(
                id = e.id,
                campaniaId = CAMPANIA_DEMO,
                tipoVinilo = TipoVinilo.PARCIAL,
                inicio = inicio,
                fin = fin,
                distanciaKm = e.km,
                estado = e.estado,
                fotoInicioPath = "",
                fotoFinPath = "",
                intentosFoto = 1,
                remoteId = if (e.estado == EstadoViaje.SINCRONIZADO) "remoto-${e.id}" else null
            )
        )
        dao.guardarPuntos(recorrido(e, inicio, fin))
    }

    /** Recorrido aproximado con algunas curvas, para dibujar el mapa del detalle. */
    private fun recorrido(e: Ejemplo, inicio: Long, fin: Long): List<PuntoGpsEntity> {
        val desde = if (e.vuelta) MICROCENTRO else RAMOS_MEJIA
        val hasta = when {
            e.corto -> Pair(desde.first + 0.002, desde.second + 0.001)
            e.vuelta -> RAMOS_MEJIA
            else -> MICROCENTRO
        }
        val pasos = 24
        return (0..pasos).map { i ->
            val t = i.toDouble() / pasos
            val curva = if (i % 6 in 2..3) 0.004 else 0.0 // algunas vueltas a la manzana
            PuntoGpsEntity(
                viajeId = e.id,
                latitud = desde.first + (hasta.first - desde.first) * t + curva,
                longitud = desde.second + (hasta.second - desde.second) * t,
                timestamp = inicio + ((fin - inicio) * t).toLong(),
                precisionMetros = 8f
            )
        }
    }

    private data class Ejemplo(
        val id: String,
        val dia: LocalDate,
        val hora: LocalTime,
        val minutos: Int,
        val km: Double,
        val estado: EstadoViaje,
        val vuelta: Boolean = false,
        val corto: Boolean = false
    )

    companion object {
        const val CAMPANIA_DEMO = "campania-demo"
        private val RAMOS_MEJIA = Pair(-34.6415, -58.5650)
        private val MICROCENTRO = Pair(-34.6037, -58.3816)
    }
}
