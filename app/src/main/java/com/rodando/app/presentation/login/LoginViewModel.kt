package com.rodando.app.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.rodando.app.domain.usecase.IniciarSesionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUi(
    val email: String = "",
    val password: String = "",
    val ingresando: Boolean = false,
    val error: String? = null
)

/** Login contra la API simulada. Al guardarse la sesión, la app pasa sola a Inicio. */
class LoginViewModel(private val iniciarSesion: IniciarSesionUseCase) : ViewModel() {

    private val _estado = MutableStateFlow(LoginUi())
    val estado: StateFlow<LoginUi> = _estado.asStateFlow()

    fun cambiarEmail(email: String) = _estado.update { it.copy(email = email, error = null) }

    fun cambiarPassword(password: String) = _estado.update { it.copy(password = password, error = null) }

    fun ingresar() {
        val actual = _estado.value
        if (actual.ingresando) return
        _estado.update { it.copy(ingresando = true, error = null) }
        viewModelScope.launch {
            runCatching { iniciarSesion(actual.email, actual.password) }
                .onFailure { e -> _estado.update { it.copy(ingresando = false, error = e.message ?: "No pudimos iniciar sesión.") } }
                .onSuccess { _estado.update { it.copy(ingresando = false) } }
        }
    }

    companion object {
        fun fabrica(iniciarSesion: IniciarSesionUseCase) = viewModelFactory {
            initializer { LoginViewModel(iniciarSesion) }
        }
    }
}
