package com.rodando.app.domain.repository

import com.rodando.app.domain.model.Sesion
import kotlinx.coroutines.flow.Flow

interface SesionRepository {
    /** La sesión guardada en el teléfono, o null si hay que iniciar sesión. */
    val sesion: Flow<Sesion?>

    /** Valida email y contraseña contra la API y guarda la sesión. Lanza una excepción con un mensaje para mostrar. */
    suspend fun iniciarSesion(email: String, password: String): Sesion

    /** Borra la sesión. Los viajes quedan guardados en el teléfono. */
    suspend fun cerrarSesion()
}
