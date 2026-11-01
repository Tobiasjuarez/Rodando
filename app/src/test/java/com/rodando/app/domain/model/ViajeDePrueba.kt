package com.rodando.app.domain.model

/** Viaje para los tests: cada caso indica solo lo que le importa. */
fun viajeDePrueba(
    id: String = "v",
    inicio: Long = 0L,
    fin: Long? = inicio + 42 * 60_000L,
    km: Double = 18.4,
    verificacion: EstadoVerificacion = EstadoVerificacion.APROBADO,
    envio: EstadoEnvio = EstadoEnvio.ENVIADO,
    tarifaPorKm: Double = 50.0
) = Viaje(
    id = id,
    campaniaId = "c",
    tipoVinilo = TipoVinilo.PARCIAL,
    tarifaPorKm = tarifaPorKm,
    inicio = inicio,
    fin = fin,
    distanciaKm = km,
    envio = envio,
    verificacion = verificacion,
    fotoInicioPath = "inicio.jpg",
    fotoFinPath = if (fin == null) null else "fin.jpg",
    intentosFoto = 1
)
