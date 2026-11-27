package com.rodando.app.domain.model

/** Qué está pasando con el envío de viajes en segundo plano, para contárselo al conductor. */
enum class EstadoDeEnvio {
    /** No hay nada en marcha. */
    QUIETO,

    /** Hay un envío pedido, esperando que haya conexión. */
    ESPERANDO_CONEXION,
    ENVIANDO,

    /** El último intento falló (servidor caído, por ejemplo): se reintenta solo más tarde. */
    REINTENTANDO
}
