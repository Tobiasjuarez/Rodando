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
import com.rodando.app.data.sensors.RegistroConServicio
import com.rodando.app.data.sensors.UbicacionGps
import com.rodando.app.data.sensors.UbicacionSimulada
import com.rodando.app.domain.repository.CampaniaRepository
import com.rodando.app.domain.repository.FuenteUbicacion
import com.rodando.app.domain.repository.RegistroDeRecorrido
import com.rodando.app.domain.repository.ViajeRepository
import com.rodando.app.domain.repository.ViniloRepository
import com.rodando.app.domain.usecase.FinalizarViajeUseCase
import com.rodando.app.domain.usecase.IniciarViajeUseCase

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

    /** GPS real, o el recorrido simulado para probar en el emulador sin cargar una ruta. */
    private val fuenteUbicacion: FuenteUbicacion =
        if (USAR_UBICACION_SIMULADA) UbicacionSimulada() else UbicacionGps(context)

    val registradorDeViaje = RegistradorDeViaje(viajeRepository, fuenteUbicacion, alcance)

    /** El registro corre dentro de un servicio en primer plano para seguir con la pantalla apagada. */
    val registroDeRecorrido: RegistroDeRecorrido = RegistroConServicio(context.applicationContext, registradorDeViaje)

    val iniciarViaje = IniciarViajeUseCase(campaniaRepository, viniloRepository, viajeRepository, registroDeRecorrido)

    val finalizarViaje = FinalizarViajeUseCase(viajeRepository, registroDeRecorrido)

    val datosDePrueba = DatosDePrueba(database.viajeDao(), database.campaniaDao(), database.viniloDao())

    companion object {
        /** true: el auto "avanza" solo desde Ramos Mejía, sin GPS. Útil para demos en el emulador. */
        const val USAR_UBICACION_SIMULADA = false
    }
}
