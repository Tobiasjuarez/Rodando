package com.rodando.app

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class RodandoApplication : Application() {

    lateinit var container: AppContainer
        private set

    /** Alcance para tareas que no dependen de una pantalla. */
    private val alcance = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        alcance.launch { container.datosDePrueba.cargarSiHaceFalta() }
    }
}
