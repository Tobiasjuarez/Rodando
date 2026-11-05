package com.rodando.app.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.rodando.app.RodandoApplication
import kotlinx.coroutines.flow.first

/**
 * Tarea de WorkManager que sincroniza los viajes. Android la corre cuando hay conexión,
 * aunque la app esté cerrada, y la reintenta con espera creciente si algo falla.
 */
class EnvioDeViajesWorker(contexto: Context, parametros: WorkerParameters) : CoroutineWorker(contexto, parametros) {

    override suspend fun doWork(): Result {
        val contenedor = (applicationContext as RodandoApplication).container
        // Sin sesión no hay a quién enviarle los viajes: quedan guardados hasta el próximo login.
        if (contenedor.sesionRepository.sesion.first() == null) return Result.success()

        // Por si cambió la campaña o no se pudo bajar al iniciar sesión.
        runCatching { contenedor.campaniaRepository.actualizarCampania() }
        val resultado = contenedor.viajeRepository.sincronizarPendientes()
        return when {
            resultado.completo -> Result.success()
            // Después de varios intentos se deja para la sincronización periódica.
            runAttemptCount >= MAX_REINTENTOS -> Result.success()
            else -> Result.retry()
        }
    }

    private companion object {
        const val MAX_REINTENTOS = 8
    }
}
