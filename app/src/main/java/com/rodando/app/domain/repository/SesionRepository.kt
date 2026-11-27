package com.rodando.app.domain.repository

import com.rodando.app.domain.model.Sesion
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

interface SesionRepository {
    /** La sesión guardada en el teléfono, o null si hay que iniciar sesión. */
    val sesion: Flow<Sesion?>

    /** true si la sesión se cerró sola porque el servidor dejó de reconocerla. */
    val sesionVencida: Flow<Boolean> get() = flowOf(false)

    /** Valida email y contraseña contra la API y guarda la sesión. Lanza una excepción con un mensaje para mostrar. */
    suspend fun iniciarSesion(email: String, password: String): Sesion

    /** Borra la sesión. Los viajes quedan guardados en el teléfono. */
    suspend fun cerrarSesion()
}
