package com.rodando.app.presentation.detalle

import com.rodando.app.domain.model.EstadoViaje
import com.rodando.app.domain.model.PuntoGps
import com.rodando.app.domain.model.Viaje
import com.rodando.app.presentation.common.Formato
import java.time.LocalDate

/** Lo que dibuja la pantalla de Detalle, ya formateado. */
data class DetalleViajeUi(
    val horario: String,
    val distancia: String,
    val duracion: String,
    val estado: EstadoViaje,
    val horaInicio: String,
    val horaFin: String?,
    val puntos: List<PuntoGps>
)

fun armarDetalle(viaje: Viaje, puntos: List<PuntoGps>, hoy: LocalDate): DetalleViajeUi {
    val dia = Formato.diaCorto(Formato.fecha(viaje.inicio), hoy)
    val horaInicio = Formato.hora(viaje.inicio)
    val horaFin = viaje.fin?.let { Formato.hora(it) }
    return DetalleViajeUi(
        horario = if (horaFin != null) "$dia · $horaInicio a $horaFin" else "$dia · desde las $horaInicio",
        distancia = Formato.km(viaje.distanciaKm),
        duracion = Formato.duracion(viaje.inicio, viaje.fin),
        estado = viaje.estado,
        horaInicio = horaInicio,
        horaFin = horaFin,
        puntos = puntos
    )
}
