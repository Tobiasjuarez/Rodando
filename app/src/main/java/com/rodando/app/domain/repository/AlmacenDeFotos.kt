package com.rodando.app.domain.repository

/**
 * Dónde se guardan las fotos del vinilo en el teléfono. El dominio solo maneja rutas:
 * no sabe si es un archivo, ni dónde está.
 */
interface AlmacenDeFotos {
    /** Ruta nueva y única para guardar una foto. */
    fun nuevaRuta(): String

    /** Borra una foto descartada (por ejemplo, al repetirla). */
    fun borrar(ruta: String)
}
