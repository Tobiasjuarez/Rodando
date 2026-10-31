package com.rodando.app

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import com.rodando.app.data.local.DatosDePrueba
import com.rodando.app.data.local.RodandoDatabase
import com.rodando.app.data.repository.CampaniaRepositoryImpl
import com.rodando.app.data.repository.ViajeRepositoryImpl
import com.rodando.app.data.repository.ViniloRepositoryImpl
import com.rodando.app.data.sensors.RegistradorDeViaje
import com.rodando.app.data.sensors.UbicacionSimulada
import com.rodando.app.domain.repository.CampaniaRepository
import com.rodando.app.domain.repository.FuenteUbicacion
import com.rodando.app.domain.repository.ViajeRepository
import com.rodando.app.domain.repository.ViniloRepository

/**
 * Inyección de dependencias manual: crea una sola vez la base de datos y los repositorios
 * y los comparte con los ViewModels.
 */
class AppContainer(context: Context) {

    /** Alcance de toda la app, para tareas que no dependen de una pantalla. */
    val alcance = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val database = RodandoDatabase.crear(context)

    val viajeRepository: ViajeRepository = ViajeRepositoryImpl(database.viajeDao())

    val campaniaRepository: CampaniaRepository = CampaniaRepositoryImpl(database.campaniaDao())

    val viniloRepository: ViniloRepository = ViniloRepositoryImpl(database.viniloDao())

    /** Por ahora simulada; se reemplaza por el GPS real en un servicio en primer plano. */
    private val fuenteUbicacion: FuenteUbicacion = UbicacionSimulada()

    val registradorDeViaje = RegistradorDeViaje(viajeRepository, fuenteUbicacion, alcance)

    val datosDePrueba = DatosDePrueba(database.viajeDao(), database.campaniaDao(), database.viniloDao())
}
