package com.rodando.app.presentation.historial

import com.rodando.app.domain.model.EstadoEnvio
import com.rodando.app.domain.model.EstadoVerificacion
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
    val enCurso: Boolean,
    val verificacion: EstadoVerificacion,
    val envio: EstadoEnvio
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
                        enCurso = viaje.enCurso,
                        verificacion = viaje.verificacion,
                        envio = viaje.envio
                    )
                }
            )
        }
    return HistorialUi(
        grupos = grupos,
        pendientes = viajes.count { !it.enCurso && it.envio == EstadoEnvio.PENDIENTE }
    )
}
