package com.rodando.app.presentation.ganancias

import com.rodando.app.domain.model.Viaje
import com.rodando.app.domain.model.Vinilo
import com.rodando.app.domain.usecase.CalcularGananciaUseCase
import com.rodando.app.presentation.common.Formato
import java.time.LocalDate
import java.time.YearMonth

/** Lo que dibuja la pantalla de Ganancias, ya formateado. */
data class GananciasUi(
    val encabezado: String,
    /** Estimado: aprobados más los que esperan aprobación. */
    val total: String,
    /** Solo lo aprobado por el equipo. */
    val confirmado: String,
    val detalle: String,
    val fijoEntero: String,
    val barraDias: BarraObjetivoUi,
    /** "186,0 km": km que se pagan este mes. */
    val kmPagos: String,
    /** "1.500": tope de km pagos por mes. */
    val kmTope: String,
    val aprobados: Int,
    val porAprobar: Int,
    val enRevision: Int,
    val sinPago: Int,
    val sinEnviar: Int
)

/** Una barra de avance hacia un mínimo del mes: "5 de 12 días", con lo que falta o "Cumplido". */
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

    // Cada viaje se paga con su propia tarifa, por eso se muestra el monto por km y no "km × tarifa".
    val fijo = if (r.fijo >= vinilo.fijoMensual) "${Formato.monto(r.fijo)} fijo" else "${Formato.monto(r.fijo)} de fijo"
    val detalle = "$fijo + ${Formato.monto(r.montoKm)} por ${Formato.km(r.kmPagados)}"

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
        total = Formato.monto(r.estimado),
        confirmado = Formato.monto(r.confirmado),
        detalle = detalle,
        fijoEntero = Formato.monto(vinilo.fijoMensual),
        barraDias = barraDias,
        kmPagos = Formato.km(r.kmPagados),
        kmTope = Formato.miles(vinilo.kmTope.toLong()),
        aprobados = r.viajesAprobados,
        porAprobar = r.viajesPorAprobar,
        enRevision = r.viajesEnRevision,
        sinPago = r.viajesSinPago,
        sinEnviar = r.viajesSinEnviar
    )
}
