package com.rodando.app.presentation.historial

import com.rodando.app.domain.model.EstadoViaje
import com.rodando.app.domain.model.Viaje
import com.rodando.app.presentation.common.Formato
import java.time.LocalDate

/** Lo que dibuja la pantalla de Historial, ya formateado. */
data class HistorialUi(
    val grupos: List<GrupoViajes>,
    val pendientes: Int
)

data class GrupoViajes(val titulo: String, val viajes: List<ItemViaje>)

data class ItemViaje(
    val id: String,
    val fecha: String,
    val detalle: String,
    val estado: EstadoViaje
)

/**
 * Agrupa los viajes por día, del más nuevo al más viejo, y cuenta los pendientes de envío.
 * Es una función pura para poder probarla sin Android.
 */
fun armarHistorial(viajes: List<Viaje>, hoy: LocalDate): HistorialUi {
    val grupos = viajes
        .sortedByDescending { it.inicio }
        .groupBy { Formato.fecha(it.inicio) }
        .map { (dia, delDia) ->
            GrupoViajes(
                titulo = Formato.tituloDia(dia, hoy),
                viajes = delDia.map { viaje ->
                    ItemViaje(
                        id = viaje.id,
                        fecha = "${Formato.diaCorto(dia, hoy)} · ${Formato.hora(viaje.inicio)}",
                        detalle = "${Formato.km(viaje.distanciaKm)} · ${Formato.duracion(viaje.inicio, viaje.fin)}",
                        estado = viaje.estado
                    )
                }
            )
        }
    return HistorialUi(grupos = grupos, pendientes = viajes.count { it.estado == EstadoViaje.PENDIENTE })
}
