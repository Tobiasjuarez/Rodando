package com.rodando.app.data.repository

import com.rodando.app.data.local.aEntidad
import com.rodando.app.data.remote.RemotoDeViajes
import com.rodando.app.data.remote.aDto
import com.rodando.app.data.remote.verificacionDe
import com.rodando.app.domain.model.EstadoEnvio
import com.rodando.app.domain.model.EstadoVerificacion
import com.rodando.app.domain.model.PuntoGps
import com.rodando.app.domain.model.Viaje
import com.rodando.app.domain.model.viajeDePrueba
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class SincronizacionTest {

    /** Servidor falso: guarda lo que recibe, puede fallar en ciertos envíos y devolver aprobaciones. */
    private class ServidorFalso(
        private val fallaEnElEnvio: Set<Int> = emptySet(),
        private val sinConexion: Boolean = false
    ) : RemotoDeViajes {
        val recibidos = linkedMapOf<String, String>()
        val estadosDelEquipo = mutableMapOf<String, EstadoVerificacion>()
        private var envios = 0

        override suspend fun enviar(viaje: Viaje, puntos: List<PuntoGps>): String {
            envios++
            if (sinConexion || envios in fallaEnElEnvio) throw IOException("sin conexión")
            // El mismo id ya llegado devuelve el mismo remoteId: no se duplica.
            return recibidos.getOrPut(viaje.id) { "R-${recibidos.size + 1}" }
        }

        override suspend fun estados(): Map<String, EstadoVerificacion> {
            if (sinConexion) throw IOException("sin conexión")
            return estadosDelEquipo
        }
    }

    private val dao = FakeViajeDao()

    private fun pendientes(cantidad: Int) = runBlocking {
        (1..cantidad).forEach { i ->
            dao.guardar(
                viajeDePrueba(
                    id = "v$i",
                    inicio = i * 3_600_000L,
                    verificacion = EstadoVerificacion.POR_APROBAR,
                    envio = EstadoEnvio.PENDIENTE
                ).aEntidad()
            )
        }
    }

    private suspend fun envio(id: String) = dao.obtenerPorId(id)!!.envio

    @Test
    fun envia_los_pendientes_y_los_marca_enviados() = runBlocking {
        pendientes(3)
        val servidor = ServidorFalso()

        val resultado = ViajeRepositoryImpl(dao, servidor).sincronizarPendientes()

        assertEquals(3, resultado.enviados)
        assertTrue(resultado.completo)
        assertEquals(EstadoEnvio.ENVIADO, envio("v1"))
        assertEquals("R-1", dao.obtenerPorId("v1")!!.remoteId)
    }

    @Test
    fun envio_parcial_deja_enviados_los_que_llegaron_y_el_resto_se_reintenta() = runBlocking {
        pendientes(5)
        val servidor = ServidorFalso(fallaEnElEnvio = setOf(3, 4)) // se corta en el 3.º y el 4.º
        val repositorio = ViajeRepositoryImpl(dao, servidor)

        val primero = repositorio.sincronizarPendientes()

        assertEquals(3, primero.enviados)
        assertEquals(2, primero.fallidos)
        assertFalse(primero.completo)
        assertEquals(EstadoEnvio.PENDIENTE, envio("v3"))
        assertEquals(EstadoEnvio.PENDIENTE, envio("v4"))

        val reintento = repositorio.sincronizarPendientes()

        assertEquals(2, reintento.enviados)
        assertTrue(reintento.completo)
        assertEquals(5, servidor.recibidos.size)
    }

    @Test
    fun si_se_pierde_la_respuesta_el_reenvio_no_duplica_el_viaje() = runBlocking {
        pendientes(1)
        val servidor = ServidorFalso()
        // El servidor recibió el viaje pero la respuesta no llegó: en el teléfono sigue pendiente.
        servidor.enviar(dao.obtenerPorId("v1")!!.let { viajeDePrueba(id = it.id) }, emptyList())

        ViajeRepositoryImpl(dao, servidor).sincronizarPendientes()

        assertEquals(1, servidor.recibidos.size)
        assertEquals(EstadoEnvio.ENVIADO, envio("v1"))
    }

    @Test
    fun sin_conexion_no_se_pierde_nada() = runBlocking {
        pendientes(2)

        val resultado = ViajeRepositoryImpl(dao, ServidorFalso(sinConexion = true)).sincronizarPendientes()

        assertEquals(2, resultado.fallidos)
        assertFalse(resultado.estadosActualizados)
        assertEquals(2, dao.pendientesDeEnvio().size)
    }

    @Test
    fun trae_las_aprobaciones_y_rechazos_del_equipo() = runBlocking {
        pendientes(2)
        val servidor = ServidorFalso().apply {
            estadosDelEquipo["v1"] = EstadoVerificacion.APROBADO
            estadosDelEquipo["v2"] = EstadoVerificacion.RECHAZADO
        }
        val repositorio = ViajeRepositoryImpl(dao, servidor)

        repositorio.sincronizarPendientes()

        val viajes = repositorio.observarViajes().first().associateBy { it.id }
        assertEquals(EstadoVerificacion.APROBADO, viajes.getValue("v1").verificacion)
        assertEquals(EstadoVerificacion.RECHAZADO, viajes.getValue("v2").verificacion)
    }

    @Test
    fun el_viaje_en_curso_no_se_envia() = runBlocking {
        dao.guardar(viajeDePrueba(id = "en-curso", fin = null, envio = EstadoEnvio.PENDIENTE).aEntidad())
        val servidor = ServidorFalso()

        ViajeRepositoryImpl(dao, servidor).sincronizarPendientes()

        assertTrue(servidor.recibidos.isEmpty())
    }

    @Test
    fun el_viaje_viaja_con_su_tarifa_puntos_y_estados_como_texto() {
        val dto = viajeDePrueba(id = "v1", tarifaPorKm = 45.0, verificacion = EstadoVerificacion.EN_REVISION)
            .aDto(listOf(PuntoGps(-34.6, -58.5, 1, 5f)), fotoInicio = "abc", fotoFin = null)

        assertEquals(45.0, dto.tarifaPorKm, 0.0)
        assertEquals("EN_REVISION", dto.verificacion)
        assertEquals("PARCIAL", dto.tipoVinilo)
        assertEquals(1, dto.puntos.size)
        assertNull(dto.fotoFin)
        assertEquals(EstadoVerificacion.APROBADO, verificacionDe("APROBADO"))
        assertNull(verificacionDe("ESTADO_NUEVO"))
    }
}
