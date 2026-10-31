package com.rodando.app.presentation.inicio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.rodando.app.domain.model.Campania
import com.rodando.app.domain.model.Viaje
import com.rodando.app.domain.model.Vinilo
import com.rodando.app.domain.repository.CampaniaRepository
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

/** Inicio: campaña activa, vinilo y resumen del mes, todo leído de Room. */
class InicioViewModel(
    campaniaRepository: CampaniaRepository,
    viniloRepository: ViniloRepository,
    viajeRepository: ViajeRepository,
    hoy: () -> LocalDate = { LocalDate.now(Formato.zona) }
) : ViewModel() {

    val estado: StateFlow<UiState<InicioUi>> =
        combine<Campania?, Vinilo?, List<Viaje>, UiState<InicioUi>>(
            campaniaRepository.observarCampaniaActiva(),
            viniloRepository.observarVinilo(),
            viajeRepository.observarViajes()
        ) { campania, vinilo, viajes ->
            if (campania == null) UiState.Vacio else UiState.Contenido(armarInicio(campania, vinilo, viajes, hoy()))
        }
            .catch { emit(UiState.Error("No pudimos leer los datos guardados en el teléfono.")) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Cargando)

    companion object {
        fun fabrica(
            campaniaRepository: CampaniaRepository,
            viniloRepository: ViniloRepository,
            viajeRepository: ViajeRepository
        ) = viewModelFactory {
            initializer { InicioViewModel(campaniaRepository, viniloRepository, viajeRepository) }
        }
    }
}
