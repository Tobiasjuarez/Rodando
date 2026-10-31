package com.rodando.app.presentation.historial

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.rodando.app.domain.model.Viaje
import com.rodando.app.domain.repository.ViajeRepository
import com.rodando.app.presentation.common.Formato
import com.rodando.app.presentation.common.UiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

/**
 * RF03: observa los viajes guardados en Room. Como la fuente es local,
 * el historial se ve completo aunque no haya conexión.
 */
class HistorialViewModel(
    repositorio: ViajeRepository,
    hoy: () -> LocalDate = { LocalDate.now(Formato.zona) }
) : ViewModel() {

    val estado: StateFlow<UiState<HistorialUi>> = repositorio.observarViajes()
        .map<List<Viaje>, UiState<HistorialUi>> { viajes ->
            if (viajes.isEmpty()) UiState.Vacio else UiState.Contenido(armarHistorial(viajes, hoy()))
        }
        .catch { emit(UiState.Error("No pudimos leer los viajes guardados en el teléfono.")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Cargando)

    companion object {
        fun fabrica(repositorio: ViajeRepository) = viewModelFactory {
            initializer { HistorialViewModel(repositorio) }
        }
    }
}
