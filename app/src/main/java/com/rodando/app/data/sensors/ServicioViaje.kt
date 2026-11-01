package com.rodando.app.data.sensors

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.rodando.app.MainActivity
import com.rodando.app.R
import com.rodando.app.RodandoApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Servicio en primer plano que mantiene el registro del recorrido con la pantalla apagada
 * o la app en segundo plano (RNF02). Muestra una notificación fija mientras dura el viaje.
 *
 * No recibe el id del viaje: lo busca en Room. Así, si Android lo reinicia (START_STICKY),
 * retoma solo el viaje que quedó en curso.
 */
class ServicioViaje : Service() {

    private val alcance = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Sin permiso de ubicación, Android no deja iniciar un servicio de tipo "location".
        if (!Permisos.tieneUbicacion(this)) {
            stopSelf()
            return START_NOT_STICKY
        }
        enPrimerPlano(notificacion(inicio = null))

        val contenedor = (application as RodandoApplication).container
        alcance.launch {
            val viaje = contenedor.viajeRepository.observarViajeEnCurso().first()
            if (viaje == null) {
                stopSelf()
                return@launch
            }
            getSystemService(NotificationManager::class.java).notify(ID_NOTIFICACION, notificacion(viaje.inicio))
            contenedor.registradorDeViaje.iniciar(viaje.id, desde = viaje.inicio)
        }
        return START_STICKY
    }

    override fun onDestroy() {
        (application as RodandoApplication).container.registradorDeViaje.detener()
        alcance.cancel()
        super.onDestroy()
    }

    private fun enPrimerPlano(notificacion: Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(ID_NOTIFICACION, notificacion, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
        } else {
            startForeground(ID_NOTIFICACION, notificacion)
        }
    }

    /** Con [inicio], la notificación muestra un cronómetro desde esa hora. */
    private fun notificacion(inicio: Long?): Notification {
        crearCanal()
        val abrirApp = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        return NotificationCompat.Builder(this, CANAL)
            .setSmallIcon(R.drawable.ic_auto)
            .setContentTitle(getString(R.string.notificacion_titulo))
            .setContentText(getString(R.string.notificacion_texto))
            .setContentIntent(abrirApp)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_NAVIGATION)
            .apply {
                if (inicio != null) {
                    setWhen(inicio)
                    setUsesChronometer(true)
                    setShowWhen(true)
                }
            }
            .build()
    }

    private fun crearCanal() {
        val canal = NotificationChannel(CANAL, getString(R.string.notificacion_canal), NotificationManager.IMPORTANCE_LOW)
        getSystemService(NotificationManager::class.java).createNotificationChannel(canal)
    }

    companion object {
        private const val CANAL = "viaje_en_curso"
        private const val ID_NOTIFICACION = 1

        fun iniciar(context: Context) {
            ContextCompat.startForegroundService(context, Intent(context, ServicioViaje::class.java))
        }

        fun detener(context: Context) {
            context.stopService(Intent(context, ServicioViaje::class.java))
        }
    }
}
