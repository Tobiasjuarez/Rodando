package com.rodando.app.fakes

import com.rodando.app.domain.model.Campania
import com.rodando.app.domain.model.Sesion
import com.rodando.app.domain.model.TipoVinilo
import com.rodando.app.domain.model.Vinilo
import com.rodando.app.domain.repository.AlmacenDeFotos
import com.rodando.app.domain.repository.CampaniaRepository
import com.rodando.app.domain.repository.ProgramadorDeEnvios
import com.rodando.app.domain.repository.RegistroDeRecorrido
import com.rodando.app.domain.repository.SesionRepository
import com.rodando.app.domain.repository.VerificadorDeVinilo
import com.rodando.app.domain.repository.ViniloRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf

// Implementaciones en memoria de las interfaces del dominio, para probar ViewModels y casos de uso sin Android.

const val CODIGO_DE_PRUEBA = "RODANDO-VINILO-0001"

class CampaniasFalsas(
    private val campania: Campania? = Campania("campania-1", "Pampa Bebidas", 0, 1)
) : CampaniaRepository {
    override fun observarCampaniaActiva(): Flow<Campania?> = flowOf(campania)
    override suspend fun actualizarCampania() = Unit
}

class VinilosFalsos(
    private val vinilo: Vinilo? = Vinilo(TipoVinilo.PARCIAL, 50_000.0, 50.0, fechaInstalacion = 0, codigo = CODIGO_DE_PRUEBA)
) : ViniloRepository {
    override fun observarVinilo(): Flow<Vinilo?> = flowOf(vinilo)
}

class RegistroFalso : RegistroDeRecorrido {
    var viajeActual: String? = null
    override fun iniciar(viajeId: String) {
        viajeActual = viajeId
    }
    override fun detener() {
        viajeActual = null
    }
}

class EnviosFalsos : ProgramadorDeEnvios {
    var pedidos = 0
    override fun enviarAhora() {
        pedidos++
    }
    override fun programarPeriodico() = Unit
}

class FotosFalsas : AlmacenDeFotos {
    private var numero = 0
    val borradas = mutableListOf<String>()
    override fun nuevaRuta(): String = "foto-${++numero}.jpg"
    override fun borrar(ruta: String) {
        borradas += ruta
    }
}

/** "Lee" en cada foto el código que diga [leidos] para esa ruta; si no está, no encuentra nada. */
class LectorFalso(private val leidos: Map<String, String> = emptyMap()) : VerificadorDeVinilo {
    override suspend fun reconoce(rutaFoto: String, codigoEsperado: String): Boolean = leidos[rutaFoto] == codigoEsperado
}

class SesionesFalsas(private val passwordCorrecta: String = "rodando123") : SesionRepository {
    override val sesion = MutableStateFlow<Sesion?>(null)
    override suspend fun iniciarSesion(email: String, password: String): Sesion {
        check(password == passwordCorrecta) { "Email o contraseña incorrectos." }
        return Sesion("token", "Lucía", email).also { sesion.value = it }
    }
    override suspend fun cerrarSesion() {
        sesion.value = null
    }
}
