package com.rodando.app.presentation.detalle

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

/** RF03: detalle de un viaje guardado en el teléfono, con su recorrido. */
class DetalleViajeViewModel(
    repositorio: ViajeRepository,
    viajeId: String,
    hoy: () -> LocalDate = { LocalDate.now(Formato.zona) }
) : ViewModel() {

    val estado: StateFlow<UiState<DetalleViajeUi>> = repositorio.observarViaje(viajeId)
        .map<Viaje?, UiState<DetalleViajeUi>> { viaje ->
            if (viaje == null) {
                UiState.Error("Puede que se haya borrado del teléfono.")
            } else {
                UiState.Contenido(armarDetalle(viaje, repositorio.obtenerPuntos(viaje.id), hoy()))
            }
        }
        .catch { emit(UiState.Error("No pudimos leer el viaje guardado en el teléfono.")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Cargando)

    companion object {
        fun fabrica(repositorio: ViajeRepository, viajeId: String) = viewModelFactory {
            initializer { DetalleViajeViewModel(repositorio, viajeId) }
        }
    }
}
