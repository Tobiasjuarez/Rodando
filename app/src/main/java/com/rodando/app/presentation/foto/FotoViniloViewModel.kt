package com.rodando.app.presentation.foto

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.rodando.app.domain.repository.AlmacenDeFotos
import com.rodando.app.domain.usecase.FinalizarViajeUseCase
import com.rodando.app.domain.usecase.IniciarViajeUseCase
import com.rodando.app.domain.usecase.ResultadoFoto
import com.rodando.app.domain.usecase.VerificarFotoUseCase
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
    /** Cuántas fotos se verificaron en este paso (máximo 3). */
    val intento: Int = 0,
    /** ML Kit está buscando el código del vinilo en la foto. */
    val verificando: Boolean = false,
    /** Qué dijo la verificación de la última foto; null mientras no hay foto o se está verificando. */
    val resultado: ResultadoFoto? = null,
    /** Se está iniciando o finalizando el viaje. */
    val guardando: Boolean = false,
    val error: String? = null,
    /** Id del viaje que se inició o finalizó: la pantalla navega cuando deja de ser null. */
    val viajeListo: String? = null
)

/**
 * RF01 / RF02: foto del vinilo al iniciar y al finalizar el viaje.
 *
 * La cámara (CameraX) vive en la pantalla; acá se decide dónde se guarda la foto, se verifica
 * con ML Kit que tenga el código del vinilo y se decide qué sigue:
 * - Reconocida: se puede usar para iniciar o finalizar el viaje.
 * - No reconocida: se repite, hasta 3 intentos.
 * - Tres intentos fallidos: se puede seguir igual y el viaje queda en revisión.
 */
class FotoViniloViewModel(
    private val momento: MomentoFoto,
    private val viajeId: String?,
    private val iniciarViaje: IniciarViajeUseCase,
    private val finalizarViaje: FinalizarViajeUseCase,
    private val verificarFoto: VerificarFotoUseCase,
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
        val intento = _estado.value.intento + 1
        _estado.update { it.copy(sacando = false, fotoTomada = ruta, intento = intento, verificando = true, resultado = null) }
        viewModelScope.launch {
            val resultado = verificarFoto(ruta, intento)
            _estado.update { it.copy(verificando = false, resultado = resultado) }
        }
    }

    fun errorDeCamara(ruta: String) {
        fotos.borrar(ruta)
        _estado.update { it.copy(sacando = false, error = "No pudimos sacar la foto. Probá de nuevo.") }
    }

    fun repetir() {
        val actual = _estado.value
        // Después del tercer intento fallido ya no se repite: se sigue en revisión o se cancela.
        if (actual.resultado == ResultadoFoto.OfrecerRevision || actual.verificando) return
        actual.fotoTomada?.let(fotos::borrar)
        _estado.update { it.copy(fotoTomada = null, resultado = null, error = null) }
    }

    /** Usa la foto verificada. */
    fun usarFoto() {
        if (_estado.value.resultado == ResultadoFoto.Aprobada) guardar(enRevision = false)
    }

    /** Sigue sin poder verificar el vinilo: el viaje queda en revisión hasta que el equipo mire las fotos. */
    fun seguirEnRevision() {
        if (_estado.value.resultado == ResultadoFoto.OfrecerRevision) guardar(enRevision = true)
    }

    private fun guardar(enRevision: Boolean) {
        val actual = _estado.value
        val ruta = actual.fotoTomada ?: return
        if (actual.guardando || actual.viajeListo != null) return
        _estado.update { it.copy(guardando = true, error = null) }
        viewModelScope.launch {
            runCatching {
                when (momento) {
                    MomentoFoto.INICIO -> iniciarViaje(ruta, intentosFoto = actual.intento, enRevision = enRevision).id
                    MomentoFoto.FIN -> finalizarViaje(requireNotNull(viajeId), ruta, fotoVerificada = !enRevision).id
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
            verificarFoto: VerificarFotoUseCase,
            fotos: AlmacenDeFotos
        ) = viewModelFactory {
            initializer { FotoViniloViewModel(momento, viajeId, iniciarViaje, finalizarViaje, verificarFoto, fotos) }
        }
    }
}
