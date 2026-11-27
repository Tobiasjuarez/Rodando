package com.rodando.app.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ClienteApi {

    /**
     * 10.0.2.2 es la computadora vista desde el emulador (en Mac y en Windows).
     * En un teléfono real, cambiar por la IP de la computadora en la red Wi-Fi.
     */
    const val URL_API = "http://10.0.2.2:3000/"

    /**
     * [token] da el token de la sesión guardada, o null si no hay sesión.
     * [alVencerSesion] se llama si el servidor rechaza un token (401): por ejemplo, si se
     * reinició la API simulada con datos nuevos. Así la app vuelve al login en vez de
     * reintentar envíos que nunca van a llegar.
     * [registrarPedidos] anota cada pedido en Logcat (solo en desarrollo).
     */
    fun crear(token: () -> String?, alVencerSesion: () -> Unit = {}, registrarPedidos: Boolean = false): RodandoApi {
        val cliente = OkHttpClient.Builder()
            .addInterceptor { cadena ->
                val tokenActual = token()
                val pedido = tokenActual?.let { cadena.request().newBuilder().header("Authorization", "Bearer $it").build() }
                val respuesta = cadena.proceed(pedido ?: cadena.request())
                if (respuesta.code == 401 && tokenActual != null) alVencerSesion()
                respuesta
            }
            // BASIC: método, ruta, código y tiempo. Nunca BODY: anotaría el token y las fotos en base64.
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = if (registrarPedidos) HttpLoggingInterceptor.Level.BASIC else HttpLoggingInterceptor.Level.NONE
                }
            )
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
        return Retrofit.Builder()
            .baseUrl(URL_API)
            .client(cliente)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RodandoApi::class.java)
    }
}
