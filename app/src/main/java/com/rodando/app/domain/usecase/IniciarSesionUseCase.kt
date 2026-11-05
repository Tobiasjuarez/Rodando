package com.rodando.app.domain.usecase

import com.rodando.app.domain.model.Sesion
import com.rodando.app.domain.repository.CampaniaRepository
import com.rodando.app.domain.repository.SesionRepository

/**
 * Inicia sesión y descarga la campaña y el vinilo del conductor. Si la campaña no se puede
 * bajar, la sesión queda igual: se reintenta en la próxima sincronización.
 */
class IniciarSesionUseCase(
    private val sesiones: SesionRepository,
    private val campanias: CampaniaRepository
) {

    suspend operator fun invoke(email: String, password: String): Sesion {
        require(email.isNotBlank() && password.isNotBlank()) { "Completá el email y la contraseña." }
        val sesion = sesiones.iniciarSesion(email.trim(), password)
        runCatching { campanias.actualizarCampania() }
        return sesion
    }
}
