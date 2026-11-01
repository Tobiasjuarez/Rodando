package com.rodando.app.presentation.guardado

import com.rodando.app.domain.model.EstadoEnvio
import com.rodando.app.domain.model.EstadoVerificacion
import com.rodando.app.domain.model.Viaje
import com.rodando.app.domain.usecase.CalcularGananciaUseCase
import com.rodando.app.presentation.common.Formato

/** Lo que dibuja la confirmación de viaje guardado, ya formateado. */
data class ViajeGuardadoUi(
    val viajeId: String,
    val verificacion: EstadoVerificacion,
    val envio: EstadoEnvio,
    val titulo: String,
    val mensaje: String,
    val distancia: String,
    val duracion: String,
    /** Lo que suma este viaje por km, a confirmar; null si no suma. */
    val ganancia: String?
)

/**
 * RF02: resumen del viaje recién finalizado. El fijo mensual no se reparte por viaje,
 * así que la ganancia del viaje es solo la parte por km, con la tarifa del viaje.
 */
fun armarViajeGuardado(
    viaje: Viaje,
    calcular: CalcularGananciaUseCase = CalcularGananciaUseCase(Formato.zona)
): ViajeGuardadoUi {
    val minimo = Formato.decimal(CalcularGananciaUseCase.DISTANCIA_MINIMA_KM)
    val envio = if (viaje.envio == EstadoEnvio.PENDIENTE) {
        " Se va a enviar solo cuando haya conexión."
    } else {
        ""
    }
    val (titulo, mensaje) = when (viaje.verificacion) {
        EstadoVerificacion.POR_APROBAR ->
            "¡Viaje guardado!" to "Suma cuando el equipo lo apruebe.$envio"
        EstadoVerificacion.EN_REVISION ->
            "Viaje guardado en revisión" to "Va a sumar si el equipo aprueba las fotos del vinilo.$envio"
        EstadoVerificacion.NO_COMPUTABLE ->
            "Viaje guardado" to "Recorriste menos de $minimo km, así que este viaje no suma a la campaña."
        EstadoVerificacion.APROBADO ->
            "¡Viaje guardado!" to "Ya está aprobado y suma a tu campaña."
        EstadoVerificacion.RECHAZADO ->
            "Viaje guardado" to "El equipo no aprobó este viaje, así que no suma."
    }
    return ViajeGuardadoUi(
        viajeId = viaje.id,
        verificacion = viaje.verificacion,
        envio = viaje.envio,
        titulo = titulo,
        mensaje = mensaje,
        distancia = Formato.km(viaje.distanciaKm),
        duracion = Formato.duracion(viaje.inicio, viaje.fin),
        ganancia = calcular.gananciaPorKm(viaje)?.let { "+" + Formato.monto(it) }
    )
}
