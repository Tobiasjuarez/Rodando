package com.rodando.app.presentation.ganancias

import com.rodando.app.domain.model.EstadoViaje
import com.rodando.app.domain.model.Viaje
import com.rodando.app.domain.model.Vinilo
import com.rodando.app.domain.usecase.CalcularGananciaUseCase
import com.rodando.app.presentation.common.Formato
import java.time.LocalDate
import java.time.YearMonth

/** Lo que dibuja la pantalla de Ganancias, ya formateado. */
data class GananciasUi(
    val encabezado: String,
    val total: String,
    val detalle: String,
    val fijoEntero: String,
    val barraKm: BarraObjetivoUi,
    val barraDias: BarraObjetivoUi,
    val viajesComputables: Int,
    val pendientes: Int,
    val noComputables: Int
)

/** Una barra de avance hacia un mínimo del mes: "642,8 de 400 km", con lo que falta o "Cumplido". */
data class BarraObjetivoUi(
    val valor: String,
    val progreso: Float,
    val ayuda: String,
    val cumplido: Boolean
)

/**
 * RF04: arma el resumen del mes con la regla de [CalcularGananciaUseCase].
 * Solo cuentan los viajes que empezaron en el mes de [hoy].
 */
fun armarGanancias(
    viajes: List<Viaje>,
    vinilo: Vinilo,
    hoy: LocalDate,
    calcular: CalcularGananciaUseCase = CalcularGananciaUseCase(Formato.zona)
): GananciasUi {
    val mes = YearMonth.from(hoy)
    val delMes = viajes.filter { YearMonth.from(Formato.fecha(it.inicio)) == mes }
    val r = calcular(delMes, vinilo)

    val fijoCompleto = r.fijoCobrado >= vinilo.fijoMensual
    val detalle = if (fijoCompleto) {
        "${Formato.monto(r.fijoCobrado)} fijo + ${Formato.decimal(r.kmPagados)} km × ${Formato.monto(vinilo.tarifaPorKm)}"
    } else {
        "${Formato.monto(r.fijoCobrado)} de fijo + ${Formato.decimal(r.kmPagados)} km × ${Formato.monto(vinilo.tarifaPorKm)}"
    }

    val barraKm = BarraObjetivoUi(
        valor = "${Formato.decimal(r.objetivoKm.actual)} de ${Formato.miles(r.objetivoKm.meta.toLong())} km",
        progreso = r.objetivoKm.progreso.toFloat(),
        ayuda = if (r.objetivoKm.cumplido) {
            "Cumplido · te quedan ${Formato.miles(r.kmPagosRestantes.toLong())} km pagos " +
                "(tope ${Formato.miles(vinilo.kmTope.toLong())})"
        } else {
            "Te faltan ${Formato.miles(Math.ceil(r.objetivoKm.faltante).toLong())} km"
        },
        cumplido = r.objetivoKm.cumplido
    )
    val diasFaltan = r.objetivoDias.faltante.toInt()
    val barraDias = BarraObjetivoUi(
        valor = "${r.diasConViajes} de ${vinilo.diasMinimos} días",
        progreso = r.objetivoDias.progreso.toFloat(),
        ayuda = when {
            r.objetivoDias.cumplido -> "Cumplido"
            diasFaltan == 1 -> "Te falta 1 día con viajes"
            else -> "Te faltan $diasFaltan días con viajes"
        },
        cumplido = r.objetivoDias.cumplido
    )

    return GananciasUi(
        encabezado = "${Formato.mes(hoy)} · ESTIMADO",
        total = Formato.monto(r.totalEstimado),
        detalle = detalle,
        fijoEntero = Formato.monto(vinilo.fijoMensual),
        barraKm = barraKm,
        barraDias = barraDias,
        viajesComputables = r.viajesComputables,
        pendientes = r.viajesPendientes,
        noComputables = delMes.count { it.estado == EstadoViaje.NO_COMPUTABLE }
    )
}
