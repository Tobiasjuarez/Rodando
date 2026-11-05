package com.rodando.app.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.rodando.app.domain.repository.RegistroDeSincronizacion
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.almacenSincronizacion by preferencesDataStore(name = "sincronizacion")

class SincronizacionLocal(context: Context) : RegistroDeSincronizacion {

    private val almacen = context.applicationContext.almacenSincronizacion

    override val ultima: Flow<Long?> = almacen.data.map { it[ULTIMA] }

    override suspend fun registrar(momento: Long) {
        almacen.edit { it[ULTIMA] = momento }
    }

    private companion object {
        val ULTIMA = longPreferencesKey("ultima_sincronizacion")
    }
}
