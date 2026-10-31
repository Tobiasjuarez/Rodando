package com.rodando.app.presentation.guardado

import com.rodando.app.domain.model.EstadoViaje
import com.rodando.app.domain.model.Viaje
import com.rodando.app.domain.model.Vinilo
import com.rodando.app.domain.usecase.CalcularGananciaUseCase
import com.rodando.app.presentation.common.Formato

/** Lo que dibuja la confirmación de viaje guardado, ya formateado. */
data class ViajeGuardadoUi(
    val viajeId: String,
    val estado: EstadoViaje,
    val titulo: String,
    val mensaje: String,
    val distancia: String,
    val duracion: String,
    /** Lo que suma este viaje por km recorrido; null si no computa. */
    val ganancia: String?
)

/**
 * RF02: resumen del viaje recién finalizado. El fijo mensual no se reparte por viaje,
 * así que la ganancia del viaje es solo la parte por km.
 */
fun armarViajeGuardado(
    viaje: Viaje,
    vinilo: Vinilo?,
    calcular: CalcularGananciaUseCase = CalcularGananciaUseCase(Formato.zona)
): ViajeGuardadoUi {
    val minimo = Formato.decimal(CalcularGananciaUseCase.DISTANCIA_MINIMA_KM)
    val (titulo, mensaje) = when (viaje.estado) {
        EstadoViaje.EN_REVISION ->
            "Viaje guardado en revisión" to "Va a sumar cuando el equipo apruebe las fotos del vinilo."
        EstadoViaje.NO_COMPUTABLE ->
            "Viaje guardado" to "Recorriste menos de $minimo km, así que este viaje no suma a la campaña."
        EstadoViaje.SINCRONIZADO ->
            "¡Viaje guardado!" to "Ya se envió y suma a tu campaña."
        EstadoViaje.PENDIENTE, EstadoViaje.EN_CURSO ->
            "¡Viaje guardado!" to "Quedó en el teléfono y se va a enviar solo cuando haya conexión."
    }
    return ViajeGuardadoUi(
        viajeId = viaje.id,
        estado = viaje.estado,
        titulo = titulo,
        mensaje = mensaje,
        distancia = Formato.km(viaje.distanciaKm),
        duracion = Formato.duracion(viaje.inicio, viaje.fin),
        ganancia = vinilo?.let { calcular.gananciaPorKm(viaje, it) }?.let { "+" + Formato.monto(it) }
    )
}
