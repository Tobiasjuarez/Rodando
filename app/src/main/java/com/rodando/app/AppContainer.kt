package com.rodando.app

import android.content.Context
import com.rodando.app.data.local.DatosDePrueba
import com.rodando.app.data.local.RodandoDatabase
import com.rodando.app.data.repository.ViajeRepositoryImpl
import com.rodando.app.domain.repository.ViajeRepository

/**
 * Inyección de dependencias manual: crea una sola vez la base de datos y los repositorios
 * y los comparte con los ViewModels.
 */
class AppContainer(context: Context) {

    private val database = RodandoDatabase.crear(context)

    val viajeRepository: ViajeRepository = ViajeRepositoryImpl(database.viajeDao())

    val datosDePrueba = DatosDePrueba(database.viajeDao())
}
