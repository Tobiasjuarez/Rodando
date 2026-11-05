package com.rodando.app.fakes

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * Los ViewModels lanzan corrutinas en el hilo principal de Android, que no existe en un test de JVM.
 * Esta regla lo reemplaza por uno de prueba que corre todo enseguida.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ReglaDeHiloPrincipal : TestWatcher() {
    override fun starting(description: Description) = Dispatchers.setMain(UnconfinedTestDispatcher())
    override fun finished(description: Description) = Dispatchers.resetMain()
}
