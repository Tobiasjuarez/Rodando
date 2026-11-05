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

    /** [token] da el token de la sesión guardada, o null si no hay sesión. */
    fun crear(token: () -> String?): RodandoApi {
        val cliente = OkHttpClient.Builder()
            .addInterceptor { cadena ->
                val pedido = token()?.let { cadena.request().newBuilder().header("Authorization", "Bearer $it").build() }
                cadena.proceed(pedido ?: cadena.request())
            }
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
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
