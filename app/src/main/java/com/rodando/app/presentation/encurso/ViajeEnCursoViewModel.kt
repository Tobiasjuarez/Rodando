package com.rodando.app.presentation.encurso

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.rodando.app.domain.model.Viaje
import com.rodando.app.domain.repository.ViajeRepository
import com.rodando.app.presentation.common.UiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

/**
 * RF01: muestra el viaje en curso con el tiempo, la distancia y el recorrido.
 * La pantalla solo lee de Room; quien guarda los puntos es el registrador del recorrido.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ViajeEnCursoViewModel(
    repositorio: ViajeRepository,
    private val ahora: () -> Long = System::currentTimeMillis
) : ViewModel() {

    val estado: StateFlow<UiState<ViajeEnCursoUi>> = repositorio.observarViajeEnCurso()
        .flatMapLatest<Viaje?, UiState<ViajeEnCursoUi>> { viaje ->
            if (viaje == null) {
                flowOf(UiState.Vacio)
            } else {
                combine(repositorio.observarPuntos(viaje.id), reloj()) { puntos, instante ->
                    UiState.Contenido(armarViajeEnCurso(viaje, puntos, instante))
                }
            }
        }
        .catch { emit(UiState.Error("No pudimos leer el viaje guardado en el teléfono.")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Cargando)

    /** Emite la hora actual una vez por segundo para el cronómetro. */
    private fun reloj(): Flow<Long> = flow {
        while (true) {
            emit(ahora())
            delay(1_000)
        }
    }

    companion object {
        fun fabrica(repositorio: ViajeRepository) = viewModelFactory {
            initializer { ViajeEnCursoViewModel(repositorio) }
        }
    }
}
