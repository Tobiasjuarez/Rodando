package com.rodando.app

import android.app.Application
import kotlinx.coroutines.launch

class RodandoApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        container.alcance.launch { container.datosDePrueba.cargarSiHaceFalta() }
        // Al abrir la app se envía lo que haya quedado pendiente y se programa el envío periódico.
        container.envios.programarPeriodico()
        container.envios.enviarAhora()
    }
}
