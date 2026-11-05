package com.rodando.app.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.rodando.app.domain.model.Sesion
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

private val Context.almacenSesion by preferencesDataStore(name = "sesion")

/** Guarda la sesión con DataStore: sobrevive a cerrar la app y se lee como Flow. */
class SesionLocal(context: Context) {

    private val almacen = context.applicationContext.almacenSesion

    val sesion: Flow<Sesion?> = almacen.data.map { datos ->
        val token = datos[TOKEN] ?: return@map null
        Sesion(token = token, nombre = datos[NOMBRE].orEmpty(), email = datos[EMAIL].orEmpty())
    }

    /**
     * Para el cliente HTTP, que pide el token fuera de una corrutina. Se llama desde un hilo
     * de OkHttp, nunca desde el principal, así que esperar a DataStore no traba la pantalla.
     */
    fun token(): String? = runBlocking { sesion.first()?.token }

    suspend fun guardar(sesion: Sesion) {
        almacen.edit {
            it[TOKEN] = sesion.token
            it[NOMBRE] = sesion.nombre
            it[EMAIL] = sesion.email
        }
    }

    suspend fun borrar() {
        almacen.edit { it.clear() }
    }

    private companion object {
        val TOKEN = stringPreferencesKey("token")
        val NOMBRE = stringPreferencesKey("nombre")
        val EMAIL = stringPreferencesKey("email")
    }
}
