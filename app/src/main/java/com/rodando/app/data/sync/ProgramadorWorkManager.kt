package com.rodando.app.data.sync

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.rodando.app.domain.repository.ProgramadorDeEnvios
import java.util.concurrent.TimeUnit

class ProgramadorWorkManager(context: Context) : ProgramadorDeEnvios {

    private val workManager = WorkManager.getInstance(context.applicationContext)

    private val conConexion = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

    override fun enviarAhora() {
        val pedido = OneTimeWorkRequestBuilder<EnvioDeViajesWorker>()
            .setConstraints(conConexion)
            // Si falla: reintenta a los 30 s, 1 min, 2 min, 4 min...
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()
        // REPLACE: un pedido nuevo (por ejemplo, "Actualizar") no espera el reintento anterior.
        // Si se interrumpe un envío, el servidor no duplica el viaje porque el id es el mismo.
        workManager.enqueueUniqueWork(ENVIO_INMEDIATO, ExistingWorkPolicy.REPLACE, pedido)
    }

    override fun programarPeriodico() {
        val pedido = PeriodicWorkRequestBuilder<EnvioDeViajesWorker>(15, TimeUnit.MINUTES)
            .setConstraints(conConexion)
            .build()
        workManager.enqueueUniquePeriodicWork(ENVIO_PERIODICO, ExistingPeriodicWorkPolicy.KEEP, pedido)
    }

    private companion object {
        const val ENVIO_INMEDIATO = "envio-de-viajes"
        const val ENVIO_PERIODICO = "envio-de-viajes-periodico"
    }
}
