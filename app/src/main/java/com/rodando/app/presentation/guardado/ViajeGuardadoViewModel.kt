package com.rodando.app.presentation.guardado

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.rodando.app.domain.model.Viaje
import com.rodando.app.domain.model.Vinilo
import com.rodando.app.domain.repository.ViajeRepository
import com.rodando.app.domain.repository.ViniloRepository
import com.rodando.app.presentation.common.UiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/** RF02: confirmación después de finalizar un viaje. */
class ViajeGuardadoViewModel(
    viajeRepository: ViajeRepository,
    viniloRepository: ViniloRepository,
    viajeId: String
) : ViewModel() {

    val estado: StateFlow<UiState<ViajeGuardadoUi>> =
        combine<Viaje?, Vinilo?, UiState<ViajeGuardadoUi>>(
            viajeRepository.observarViaje(viajeId),
            viniloRepository.observarVinilo()
        ) { viaje, vinilo ->
            if (viaje == null) UiState.Error("Puede que se haya borrado del teléfono.")
            else UiState.Contenido(armarViajeGuardado(viaje, vinilo))
        }
            .catch { emit(UiState.Error("No pudimos leer el viaje guardado en el teléfono.")) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Cargando)

    companion object {
        fun fabrica(viajeRepository: ViajeRepository, viniloRepository: ViniloRepository, viajeId: String) =
            viewModelFactory {
                initializer { ViajeGuardadoViewModel(viajeRepository, viniloRepository, viajeId) }
            }
    }
}
