package com.rodando.app.presentation.inicio

import com.rodando.app.domain.model.Campania
import com.rodando.app.domain.model.EstadoViaje
import com.rodando.app.domain.model.TipoVinilo
import com.rodando.app.domain.model.Viaje
import com.rodando.app.domain.model.Vinilo
import com.rodando.app.presentation.common.Formato
import com.rodando.app.presentation.ganancias.BarraObjetivoUi
import com.rodando.app.presentation.ganancias.armarGanancias
import java.time.LocalDate

/** Lo que dibuja la pantalla de Inicio, ya formateado. */
data class InicioUi(
    val marca: String,
    val periodo: String,
    val vinilo: String?,
    val tarifa: String?,
    val pendientes: Int,
    val totalMes: String?,
    val viajesDelMes: Int,
    val barraKm: BarraObjetivoUi?,
    val barraDias: BarraObjetivoUi?,
    val puedeIniciarViaje: Boolean,
    val hayViajeEnCurso: Boolean
)

fun TipoVinilo.nombre(): String = when (this) {
    TipoVinilo.COMPLETO -> "Vinilo Completo"
    TipoVinilo.PARCIAL -> "Vinilo Parcial"
    TipoVinilo.LIVIANO -> "Vinilo Liviano"
}

/**
 * Arma Inicio a partir de la campaña, el vinilo y los viajes guardados.
 * Sin vinilo instalado no se muestra el resumen ni se puede iniciar un viaje.
 */
fun armarInicio(campania: Campania, vinilo: Vinilo?, viajes: List<Viaje>, hoy: LocalDate): InicioUi {
    val ganancias = vinilo?.let { armarGanancias(viajes, it, hoy) }
    val hayViajeEnCurso = viajes.any { it.fin == null }
    return InicioUi(
        marca = campania.marca,
        periodo = buildString {
            append(Formato.periodo(Formato.fecha(campania.fechaInicio), Formato.fecha(campania.fechaFin)))
            vinilo?.let { append(" · ").append(it.tipo.nombre()) }
        },
        vinilo = vinilo?.tipo?.nombre(),
        tarifa = vinilo?.let { "${Formato.monto(it.fijoMensual)}/mes + ${Formato.monto(it.tarifaPorKm)}/km" },
        pendientes = viajes.count { it.estado == EstadoViaje.PENDIENTE },
        totalMes = ganancias?.total,
        viajesDelMes = ganancias?.viajesComputables ?: 0,
        barraKm = ganancias?.barraKm,
        barraDias = ganancias?.barraDias,
        puedeIniciarViaje = vinilo?.fechaInstalacion != null && !hayViajeEnCurso,
        hayViajeEnCurso = hayViajeEnCurso
    )
}
