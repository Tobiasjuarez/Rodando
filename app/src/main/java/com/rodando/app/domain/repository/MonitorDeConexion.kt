package com.rodando.app.domain.repository

import kotlinx.coroutines.flow.Flow

/** Si el teléfono tiene red. Las pantallas lo usan para avisar que se está trabajando sin conexión. */
interface MonitorDeConexion {
    val conectado: Flow<Boolean>
}
