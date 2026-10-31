package com.rodando.app

import android.app.Application
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class RodandoApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        container.alcance.launch {
            container.datosDePrueba.cargarSiHaceFalta()
            // RNF02: si la app se cerró con un viaje en curso, se sigue registrando el recorrido.
            container.viajeRepository.observarViajeEnCurso().first()?.let { viaje ->
                container.registradorDeViaje.iniciar(viaje.id)
            }
        }
    }
}
