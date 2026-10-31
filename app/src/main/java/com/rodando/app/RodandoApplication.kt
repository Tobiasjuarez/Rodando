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
    }
}
