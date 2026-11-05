package com.rodando.app.data.repository

import com.rodando.app.data.local.SesionLocal
import com.rodando.app.data.remote.LoginPedido
import com.rodando.app.data.remote.RodandoApi
import com.rodando.app.domain.model.Sesion
import com.rodando.app.domain.repository.SesionRepository
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException
import java.io.IOException

class SesionRepositoryImpl(
    private val api: RodandoApi,
    private val local: SesionLocal
) : SesionRepository {

    override val sesion: Flow<Sesion?> = local.sesion

    override suspend fun iniciarSesion(email: String, password: String): Sesion {
        val respuesta = try {
            api.login(LoginPedido(email, password))
        } catch (e: HttpException) {
            throw IllegalStateException(
                if (e.code() == 401) "Email o contraseña incorrectos." else "El servidor respondió con un error (${e.code()})."
            )
        } catch (e: IOException) {
            throw IllegalStateException("No hay conexión con el servidor. ¿Está levantada la API simulada?")
        }
        val sesion = Sesion(respuesta.token, respuesta.conductor.nombre, respuesta.conductor.email)
        local.guardar(sesion)
        return sesion
    }

    override suspend fun cerrarSesion() = local.borrar()
}
