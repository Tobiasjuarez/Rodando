package com.rodando.app.domain.model

/**
 * Un viaje registrado por el conductor (RF01, RF02).
 *
 * El id es un UUID generado en el teléfono: si un envío a la API se reintenta,
 * el backend reconoce el mismo viaje y no lo duplica.
 *
 * Tiene dos estados independientes:
 * - [envio]: si ya llegó al servidor (técnico).
 * - [verificacion]: si el equipo lo aprobó (de negocio). Un viaje enviado puede estar todavía sin aprobar.
 *
 * [tarifaPorKm] es la tarifa vigente al iniciar el viaje: si después cambia el vinilo
 * o la tarifa, lo ya recorrido se sigue pagando como se acordó.
 */
data class Viaje(
    val id: String,
    val campaniaId: String,
    val tipoVinilo: TipoVinilo,
    val tarifaPorKm: Double,
    val inicio: Long,
    val fin: Long?,
    val distanciaKm: Double,
    val envio: EstadoEnvio,
    val verificacion: EstadoVerificacion,
    val fotoInicioPath: String,
    val fotoFinPath: String?,
    val intentosFoto: Int
) {
    val enCurso: Boolean get() = fin == null
}

/** Si el viaje ya llegó al servidor. */
enum class EstadoEnvio {
    PENDIENTE,
    ENVIADO
}

/** Qué dice el equipo de Rodando sobre el viaje. Solo los aprobados se cobran. */
enum class EstadoVerificacion {
    /** La foto del vinilo se reconoció en el teléfono; falta la aprobación del equipo. */
    POR_APROBAR,

    /** La foto no se reconoció: el equipo la revisa a mano antes de decidir. */
    EN_REVISION,
    APROBADO,
    RECHAZADO,

    /** Recorrió menos de la distancia mínima: queda registrado pero no se paga. */
    NO_COMPUTABLE
}
