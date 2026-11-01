package com.rodando.app.data.sensors

import com.rodando.app.data.repository.FakeViajeDao
import com.rodando.app.data.repository.ViajeRepositoryImpl
import com.rodando.app.domain.model.PuntoGps
import com.rodando.app.domain.repository.FuenteUbicacion
import com.rodando.app.domain.usecase.CalcularDistanciaUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RegistradorDeViajeTest {

    private val dao = FakeViajeDao()
    private val repositorio = ViajeRepositoryImpl(dao)

    private val tresPuntos = object : FuenteUbicacion {
        override fun ubicaciones(): Flow<PuntoGps> = flowOf(
            PuntoGps(-34.60, -58.38, 1, 5f),
            PuntoGps(-34.61, -58.38, 2, 5f),
            PuntoGps(-34.62, -58.38, 3, 5f)
        )
    }

    @Test
    fun guarda_cada_posicion_del_viaje_en_curso() = runBlocking {
        val registrador = RegistradorDeViaje(repositorio, tresPuntos, CoroutineScope(Dispatchers.Unconfined))

        registrador.iniciar("viaje-1")

        assertEquals(3, repositorio.observarPuntos("viaje-1").first().size)
    }

    @Test
    fun al_detener_deja_de_registrar() {
        val registrador = RegistradorDeViaje(repositorio, UbicacionSimulada(), CoroutineScope(Dispatchers.Unconfined))

        registrador.iniciar("viaje-1")
        assertTrue(registrador.registrando)
        registrador.detener()

        assertFalse(registrador.registrando)
    }

    @Test
    fun la_ubicacion_simulada_avanza_unos_50_metros_por_paso() = runBlocking {
        val puntos = UbicacionSimulada(intervaloMs = 0).ubicaciones().take(6).toList()

        val km = CalcularDistanciaUseCase()(puntos)

        assertTrue("recorrió $km km", km in 0.2..0.32)
    }

    @Test
    fun descarta_las_posiciones_imprecisas() = runBlocking {
        val conUnaImprecisa = object : FuenteUbicacion {
            override fun ubicaciones(): Flow<PuntoGps> = flowOf(
                PuntoGps(-34.60, -58.38, 1, 5f),
                PuntoGps(-34.70, -58.38, 2, 120f),
                PuntoGps(-34.61, -58.38, 3, 12f)
            )
        }
        val registrador = RegistradorDeViaje(repositorio, conUnaImprecisa, CoroutineScope(Dispatchers.Unconfined))

        registrador.iniciar("viaje-1")

        assertEquals(listOf(1L, 3L), repositorio.observarPuntos("viaje-1").first().map { it.timestamp })
    }

    @Test
    fun descarta_la_ultima_ubicacion_conocida_si_es_de_antes_del_viaje() = runBlocking {
        val inicioViaje = 10 * 60_000L
        val conUnaVieja = object : FuenteUbicacion {
            override fun ubicaciones(): Flow<PuntoGps> = flowOf(
                PuntoGps(-34.639, -58.525, 0, 5f), // de un viaje anterior, 10 minutos antes
                PuntoGps(-34.6415, -58.565, inicioViaje + 5_000, 5f),
                PuntoGps(-34.6413, -58.564, inicioViaje + 10_000, 5f)
            )
        }
        val registrador = RegistradorDeViaje(repositorio, conUnaVieja, CoroutineScope(Dispatchers.Unconfined))

        registrador.iniciar("viaje-1", desde = inicioViaje)

        assertEquals(
            listOf(inicioViaje + 5_000, inicioViaje + 10_000),
            repositorio.observarPuntos("viaje-1").first().map { it.timestamp }
        )
    }
}
