package com.rodando.app.presentation.foto

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.rodando.app.domain.usecase.FinalizarViajeUseCase
import com.rodando.app.domain.usecase.IniciarViajeUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** En qué punto del viaje se saca la foto del vinilo. */
enum class MomentoFoto { INICIO, FIN }

data class FotoUi(
    val momento: MomentoFoto,
    val guardando: Boolean = false,
    val error: String? = null,
    /** Id del viaje que se inició o finalizó: la pantalla navega cuando deja de ser null. */
    val viajeListo: String? = null
)

/**
 * RF01 / RF02: foto del vinilo al iniciar y al finalizar el viaje.
 *
 * Por ahora la cámara es simulada: la foto siempre se considera válida.
 * CameraX y la verificación con ML Kit (hasta 3 intentos) se suman en las próximas etapas.
 */
class FotoViniloViewModel(
    private val momento: MomentoFoto,
    private val viajeId: String?,
    private val iniciarViaje: IniciarViajeUseCase,
    private val finalizarViaje: FinalizarViajeUseCase
) : ViewModel() {

    private val _estado = MutableStateFlow(FotoUi(momento))
    val estado: StateFlow<FotoUi> = _estado.asStateFlow()

    fun capturar() {
        val actual = _estado.value
        if (actual.guardando || actual.viajeListo != null) return
        _estado.update { it.copy(guardando = true, error = null) }
        viewModelScope.launch {
            runCatching {
                when (momento) {
                    MomentoFoto.INICIO -> iniciarViaje(FOTO_SIMULADA, intentosFoto = 1, enRevision = false).id
                    MomentoFoto.FIN -> finalizarViaje(requireNotNull(viajeId), FOTO_SIMULADA).id
                }
            }.onSuccess { id ->
                _estado.update { it.copy(guardando = false, viajeListo = id) }
            }.onFailure { e ->
                _estado.update { it.copy(guardando = false, error = e.message ?: "No pudimos guardar el viaje.") }
            }
        }
    }

    companion object {
        /** Ruta que se guarda mientras no haya cámara real. */
        const val FOTO_SIMULADA = "simulada://vinilo"

        fun fabrica(
            momento: MomentoFoto,
            viajeId: String?,
            iniciarViaje: IniciarViajeUseCase,
            finalizarViaje: FinalizarViajeUseCase
        ) = viewModelFactory {
            initializer { FotoViniloViewModel(momento, viajeId, iniciarViaje, finalizarViaje) }
        }
    }
}
