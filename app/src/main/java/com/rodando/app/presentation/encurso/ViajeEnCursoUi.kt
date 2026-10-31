package com.rodando.app.presentation.encurso

import com.rodando.app.domain.model.EstadoViaje
import com.rodando.app.domain.model.PuntoGps
import com.rodando.app.domain.model.Viaje
import com.rodando.app.domain.usecase.CalcularDistanciaUseCase
import com.rodando.app.presentation.common.Formato

/** Lo que dibuja la pantalla de viaje en curso, ya formateado. */
data class ViajeEnCursoUi(
    val viajeId: String,
    val estado: EstadoViaje,
    val desde: String,
    val tiempo: String,
    val distancia: String,
    val puntos: List<PuntoGps>
)

/** La distancia se calcula en vivo con los puntos registrados hasta [ahora]. */
fun armarViajeEnCurso(
    viaje: Viaje,
    puntos: List<PuntoGps>,
    ahora: Long,
    calcularDistancia: CalcularDistanciaUseCase = CalcularDistanciaUseCase()
): ViajeEnCursoUi = ViajeEnCursoUi(
    viajeId = viaje.id,
    estado = viaje.estado,
    desde = "Desde las ${Formato.hora(viaje.inicio)}",
    tiempo = Formato.cronometro(ahora - viaje.inicio),
    distancia = Formato.km(calcularDistancia(puntos)),
    puntos = puntos
)
