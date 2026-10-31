package com.rodando.app.presentation.ganancias

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.rodando.app.domain.model.Viaje
import com.rodando.app.domain.model.Vinilo
import com.rodando.app.domain.repository.ViajeRepository
import com.rodando.app.domain.repository.ViniloRepository
import com.rodando.app.presentation.common.Formato
import com.rodando.app.presentation.common.UiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

/**
 * RF04: combina los viajes y el vinilo guardados en Room y recalcula la ganancia
 * cada vez que cambia alguno de los dos (por ejemplo, al finalizar un viaje).
 */
class GananciasViewModel(
    viajeRepository: ViajeRepository,
    viniloRepository: ViniloRepository,
    hoy: () -> LocalDate = { LocalDate.now(Formato.zona) }
) : ViewModel() {

    val estado: StateFlow<UiState<GananciasUi>> =
        combine<List<Viaje>, Vinilo?, UiState<GananciasUi>>(
            viajeRepository.observarViajes(),
            viniloRepository.observarVinilo()
        ) { viajes, vinilo ->
            if (vinilo == null) UiState.Vacio else UiState.Contenido(armarGanancias(viajes, vinilo, hoy()))
        }
            .catch { emit(UiState.Error("No pudimos calcular las ganancias con los datos del teléfono.")) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Cargando)

    companion object {
        fun fabrica(viajeRepository: ViajeRepository, viniloRepository: ViniloRepository) = viewModelFactory {
            initializer { GananciasViewModel(viajeRepository, viniloRepository) }
        }
    }
}
