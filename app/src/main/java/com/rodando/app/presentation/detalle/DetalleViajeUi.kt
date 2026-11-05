package com.rodando.app.presentation.detalle

import com.rodando.app.domain.model.EstadoEnvio
import com.rodando.app.domain.model.EstadoVerificacion
import com.rodando.app.domain.model.PuntoGps
import com.rodando.app.domain.model.Viaje
import com.rodando.app.domain.usecase.CalcularGananciaUseCase
import com.rodando.app.domain.usecase.LimpiarRecorridoUseCase
import com.rodando.app.presentation.common.Formato
import java.time.LocalDate

/** Lo que dibuja la pantalla de Detalle, ya formateado. */
data class DetalleViajeUi(
    val horario: String,
    val distancia: String,
    val duracion: String,
    val enCurso: Boolean,
    val verificacion: EstadoVerificacion,
    val envio: EstadoEnvio,
    val horaInicio: String,
    val horaFin: String?,
    val puntos: List<PuntoGps>,
    /** "$50/km": la tarifa con la que se paga este viaje, aunque después cambie. */
    val tarifa: String,
    /** Lo que suma el viaje por km; null si no suma. */
    val ganancia: String? = null,
    /** Rutas de las fotos del vinilo en el teléfono (vacías en los viajes de ejemplo). */
    val fotoInicio: String? = null,
    val fotoFin: String? = null
)

fun armarDetalle(
    viaje: Viaje,
    puntos: List<PuntoGps>,
    hoy: LocalDate,
    calcular: CalcularGananciaUseCase = CalcularGananciaUseCase(Formato.zona),
    limpiarRecorrido: LimpiarRecorridoUseCase = LimpiarRecorridoUseCase()
): DetalleViajeUi {
    val dia = Formato.diaCorto(Formato.fecha(viaje.inicio), hoy)
    val horaInicio = Formato.hora(viaje.inicio)
    val horaFin = viaje.fin?.let { Formato.hora(it) }
    return DetalleViajeUi(
        horario = if (horaFin != null) "$dia · $horaInicio a $horaFin" else "$dia · desde las $horaInicio",
        distancia = Formato.km(viaje.distanciaKm),
        duracion = Formato.duracion(viaje.inicio, viaje.fin),
        enCurso = viaje.enCurso,
        verificacion = viaje.verificacion,
        envio = viaje.envio,
        horaInicio = horaInicio,
        horaFin = horaFin,
        puntos = limpiarRecorrido(puntos),
        tarifa = "${Formato.monto(viaje.tarifaPorKm)}/km",
        ganancia = calcular.gananciaPorKm(viaje)?.let { "+" + Formato.monto(it) },
        fotoInicio = viaje.fotoInicioPath,
        fotoFin = viaje.fotoFinPath
    )
}
