package com.rodando.app.presentation.foto

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.rodando.app.domain.repository.AlmacenDeFotos
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
    /** La cámara está guardando la foto. */
    val sacando: Boolean = false,
    /** Foto ya sacada, esperando que el conductor la use o la repita. */
    val fotoTomada: String? = null,
    /** Se está iniciando o finalizando el viaje. */
    val guardando: Boolean = false,
    val error: String? = null,
    /** Id del viaje que se inició o finalizó: la pantalla navega cuando deja de ser null. */
    val viajeListo: String? = null
)

/**
 * RF01 / RF02: foto del vinilo al iniciar y al finalizar el viaje.
 *
 * La cámara (CameraX) vive en la pantalla; acá se decide dónde se guarda la foto y qué se hace
 * con ella: repetirla o usarla para iniciar o finalizar el viaje. La verificación con ML Kit
 * (hasta 3 intentos) se suma en la próxima etapa.
 */
class FotoViniloViewModel(
    private val momento: MomentoFoto,
    private val viajeId: String?,
    private val iniciarViaje: IniciarViajeUseCase,
    private val finalizarViaje: FinalizarViajeUseCase,
    private val fotos: AlmacenDeFotos
) : ViewModel() {

    private val _estado = MutableStateFlow(FotoUi(momento))
    val estado: StateFlow<FotoUi> = _estado.asStateFlow()

    /** Devuelve dónde guardar la foto que está por sacar la cámara. */
    fun empezarCaptura(): String {
        _estado.update { it.copy(sacando = true, error = null) }
        return fotos.nuevaRuta()
    }

    fun fotoGuardada(ruta: String) {
        _estado.update { it.copy(sacando = false, fotoTomada = ruta) }
    }

    fun errorDeCamara(ruta: String) {
        fotos.borrar(ruta)
        _estado.update { it.copy(sacando = false, error = "No pudimos sacar la foto. Probá de nuevo.") }
    }

    fun repetir() {
        _estado.value.fotoTomada?.let(fotos::borrar)
        _estado.update { it.copy(fotoTomada = null, error = null) }
    }

    fun usarFoto() {
        val actual = _estado.value
        val ruta = actual.fotoTomada ?: return
        if (actual.guardando || actual.viajeListo != null) return
        _estado.update { it.copy(guardando = true, error = null) }
        viewModelScope.launch {
            runCatching {
                when (momento) {
                    MomentoFoto.INICIO -> iniciarViaje(ruta, intentosFoto = 1, enRevision = false).id
                    MomentoFoto.FIN -> finalizarViaje(requireNotNull(viajeId), ruta).id
                }
            }.onSuccess { id ->
                _estado.update { it.copy(guardando = false, viajeListo = id) }
            }.onFailure { e ->
                _estado.update { it.copy(guardando = false, error = e.message ?: "No pudimos guardar el viaje.") }
            }
        }
    }

    /** Si el conductor se va sin usar la foto, no queda un archivo suelto en el teléfono. */
    override fun onCleared() {
        val actual = _estado.value
        if (actual.viajeListo == null) actual.fotoTomada?.let(fotos::borrar)
    }

    companion object {
        fun fabrica(
            momento: MomentoFoto,
            viajeId: String?,
            iniciarViaje: IniciarViajeUseCase,
            finalizarViaje: FinalizarViajeUseCase,
            fotos: AlmacenDeFotos
        ) = viewModelFactory {
            initializer { FotoViniloViewModel(momento, viajeId, iniciarViaje, finalizarViaje, fotos) }
        }
    }
}
