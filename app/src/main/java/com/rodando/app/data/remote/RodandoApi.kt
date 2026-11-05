package com.rodando.app.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * Rutas de la API simulada (api-mock/server.js). Retrofit arma las llamadas; si el servidor
 * responde un error (4xx o 5xx) lanza HttpException, y si no hay conexión, IOException.
 */
interface RodandoApi {

    @POST("login")
    suspend fun login(@Body pedido: LoginPedido): LoginRespuesta

    @GET("campania")
    suspend fun campania(): CampaniaRespuesta

    /** Si el viaje ya había llegado (mismo id), responde 200 con el mismo remoteId: no se duplica. */
    @POST("viajes")
    suspend fun enviarViaje(@Body viaje: ViajeDto): EnvioRespuesta

    @GET("viajes/estados")
    suspend fun estados(): List<EstadoDto>
}
