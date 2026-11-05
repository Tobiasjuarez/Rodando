package com.rodando.app.domain.model

/** Conductor con la sesión iniciada. El token acompaña cada pedido a la API. */
data class Sesion(val token: String, val nombre: String, val email: String)
