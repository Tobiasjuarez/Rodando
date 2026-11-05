package com.rodando.app.presentation.foto

import com.rodando.app.data.repository.FakeViajeDao
import com.rodando.app.data.repository.ViajeRepositoryImpl
import com.rodando.app.domain.model.EstadoVerificacion
import com.rodando.app.domain.usecase.FinalizarViajeUseCase
import com.rodando.app.domain.usecase.IniciarViajeUseCase
import com.rodando.app.domain.usecase.ResultadoFoto
import com.rodando.app.domain.usecase.VerificarFotoUseCase
import com.rodando.app.fakes.CODIGO_DE_PRUEBA
import com.rodando.app.fakes.CampaniasFalsas
import com.rodando.app.fakes.EnviosFalsos
import com.rodando.app.fakes.FotosFalsas
import com.rodando.app.fakes.LectorFalso
import com.rodando.app.fakes.RegistroFalso
import com.rodando.app.fakes.ReglaDeHiloPrincipal
import com.rodando.app.fakes.VinilosFalsos
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

/** El flujo de la foto del vinilo, paso a paso, como lo usa la pantalla. */
class FotoViniloViewModelTest {

    @get:Rule val hiloPrincipal = ReglaDeHiloPrincipal()

    private val viajes = ViajeRepositoryImpl(FakeViajeDao(), nuevoId = { "viaje-1" })
    private val fotos = FotosFalsas()
    private val registro = RegistroFalso()

    /** [reconocidas]: rutas de las fotos en las que "se lee" el QR del vinilo. */
    private fun viewModel(vararg reconocidas: String) = FotoViniloViewModel(
        momento = MomentoFoto.INICIO,
        viajeId = null,
        iniciarViaje = IniciarViajeUseCase(CampaniasFalsas(), VinilosFalsos(), viajes, registro),
        finalizarViaje = FinalizarViajeUseCase(viajes, registro, EnviosFalsos()),
        verificarFoto = VerificarFotoUseCase(VinilosFalsos(), LectorFalso(reconocidas.associateWith { CODIGO_DE_PRUEBA })),
        fotos = fotos
    )

    /** Lo que hace la pantalla al tocar "Sacar foto" y que la cámara la guarde bien. */
    private fun FotoViniloViewModel.sacarFoto() = fotoGuardada(empezarCaptura())

    @Test
    fun una_foto_con_el_qr_se_aprueba_y_permite_iniciar_el_viaje() = runBlocking {
        val vm = viewModel("foto-1.jpg")

        vm.sacarFoto()
        assertEquals(ResultadoFoto.Aprobada, vm.estado.value.resultado)
        vm.usarFoto()

        assertEquals("viaje-1", vm.estado.value.viajeListo)
        val viaje = viajes.obtenerPorIdParaTest("viaje-1")
        assertEquals(EstadoVerificacion.POR_APROBAR, viaje.verificacion)
        assertEquals(1, viaje.intentosFoto)
        assertEquals("viaje-1", registro.viajeActual)
    }

    @Test
    fun una_foto_sin_qr_pide_repetir_y_borra_la_descartada() {
        val vm = viewModel("foto-2.jpg")

        vm.sacarFoto()
        assertEquals(ResultadoFoto.Reintentar(2), vm.estado.value.resultado)
        vm.usarFoto() // sin verificar no se puede usar
        assertNull(vm.estado.value.viajeListo)

        vm.repetir()
        vm.sacarFoto()

        assertEquals(listOf("foto-1.jpg"), fotos.borradas)
        assertEquals(ResultadoFoto.Aprobada, vm.estado.value.resultado)
        assertEquals(2, vm.estado.value.intento)
    }

    @Test
    fun tres_fotos_sin_qr_permiten_seguir_en_revision() = runBlocking {
        val vm = viewModel()

        repeat(2) {
            vm.sacarFoto()
            vm.repetir()
        }
        vm.sacarFoto()
        assertEquals(ResultadoFoto.OfrecerRevision, vm.estado.value.resultado)
        vm.repetir() // después del tercer intento ya no se repite
        vm.seguirEnRevision()

        val viaje = viajes.obtenerPorIdParaTest("viaje-1")
        assertEquals(EstadoVerificacion.EN_REVISION, viaje.verificacion)
        assertEquals(3, viaje.intentosFoto)
        assertEquals("foto-3.jpg", viaje.fotoInicioPath)
    }

    @Test
    fun si_la_camara_falla_se_avisa_y_no_queda_el_archivo() {
        val vm = viewModel()

        val ruta = vm.empezarCaptura()
        vm.errorDeCamara(ruta)

        assertEquals("No pudimos sacar la foto. Probá de nuevo.", vm.estado.value.error)
        assertEquals(listOf(ruta), fotos.borradas)
        assertNull(vm.estado.value.fotoTomada)
    }
}

private suspend fun ViajeRepositoryImpl.obtenerPorIdParaTest(id: String) =
    requireNotNull(observarViajes().first().find { it.id == id })
