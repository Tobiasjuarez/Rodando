package com.rodando.app.domain.usecase

import com.rodando.app.data.repository.FakeViajeDao
import com.rodando.app.data.repository.ViajeRepositoryImpl
import com.rodando.app.domain.model.Campania
import com.rodando.app.domain.model.EstadoViaje
import com.rodando.app.domain.model.PuntoGps
import com.rodando.app.domain.model.TipoVinilo
import com.rodando.app.domain.model.Vinilo
import com.rodando.app.domain.repository.CampaniaRepository
import com.rodando.app.domain.repository.RegistroDeRecorrido
import com.rodando.app.domain.repository.ViniloRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class IniciarYFinalizarViajeTest {

    private val campania = Campania("campania-1", "Pampa Bebidas", 0, 1)
    private val vinilo = Vinilo(TipoVinilo.PARCIAL, 50_000.0, 50.0, fechaInstalacion = 0)

    private class RegistroFalso : RegistroDeRecorrido {
        var viajeActual: String? = null
        override fun iniciar(viajeId: String) { viajeActual = viajeId }
        override fun detener() { viajeActual = null }
    }

    private class Campanias(private val c: Campania?) : CampaniaRepository {
        override fun observarCampaniaActiva(): Flow<Campania?> = flowOf(c)
        override suspend fun actualizarCampania() = Unit
    }

    private class Vinilos(private val v: Vinilo?) : ViniloRepository {
        override fun observarVinilo(): Flow<Vinilo?> = flowOf(v)
    }

    private val viajes = ViajeRepositoryImpl(FakeViajeDao(), nuevoId = { "viaje-1" })
    private val registro = RegistroFalso()

    private fun iniciar(c: Campania? = campania, v: Vinilo? = vinilo) =
        IniciarViajeUseCase(Campanias(c), Vinilos(v), viajes, registro)

    @Test
    fun iniciar_crea_el_viaje_con_el_vinilo_vigente_y_enciende_el_registro() = runBlocking {
        val viaje = iniciar()("foto.jpg", intentosFoto = 1, enRevision = false)

        assertEquals("campania-1", viaje.campaniaId)
        assertEquals(TipoVinilo.PARCIAL, viaje.tipoVinilo)
        assertEquals(EstadoViaje.EN_CURSO, viaje.estado)
        assertEquals("viaje-1", registro.viajeActual)
    }

    @Test
    fun sin_campania_o_sin_vinilo_instalado_no_se_inicia() = runBlocking {
        val sinCampania = runCatching { iniciar(c = null)("foto.jpg", 1, false) }
        val sinInstalar = runCatching { iniciar(v = vinilo.copy(fechaInstalacion = null))("foto.jpg", 1, false) }

        assertTrue(sinCampania.isFailure)
        assertTrue(sinInstalar.isFailure)
        assertNull(viajes.observarViajeEnCurso().first())
        assertNull(registro.viajeActual)
    }

    @Test
    fun finalizar_apaga_el_registro_y_deja_el_viaje_pendiente() = runBlocking {
        iniciar()("inicio.jpg", 1, false)
        viajes.registrarPunto("viaje-1", PuntoGps(-34.60, -58.38, 1, 5f))
        viajes.registrarPunto("viaje-1", PuntoGps(-34.61, -58.38, 2, 5f))

        val viaje = FinalizarViajeUseCase(viajes, registro)("viaje-1", "fin.jpg")

        assertNull(registro.viajeActual)
        assertEquals(EstadoViaje.PENDIENTE, viaje.estado)
        assertEquals("fin.jpg", viaje.fotoFinPath)
        assertNull(viajes.observarViajeEnCurso().first())
    }
}
