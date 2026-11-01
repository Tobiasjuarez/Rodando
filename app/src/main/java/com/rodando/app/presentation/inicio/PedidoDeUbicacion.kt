package com.rodando.app.presentation.inicio

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.rodando.app.data.sensors.Permisos
import com.rodando.app.data.sensors.UbicacionGps

/** Por qué no se puede iniciar un viaje todavía. */
enum class SinUbicacion {
    /** El conductor rechazó el permiso de ubicación. */
    PERMISO,

    /** La ubicación del teléfono está apagada y no se quiso activar. */
    APAGADA
}

/**
 * Devuelve la acción de "Iniciar viaje": pide los permisos que falten y, si la ubicación
 * del teléfono está apagada, muestra el diálogo del sistema para activarla con un toque.
 * Llama a [onListo] cuando todo está en orden.
 */
@Composable
fun rememberPedidoDeUbicacion(onListo: () -> Unit, onSinUbicacion: (SinUbicacion) -> Unit): () -> Unit {
    val contexto = LocalContext.current
    val listo by rememberUpdatedState(onListo)
    val sinUbicacion by rememberUpdatedState(onSinUbicacion)

    val activarUbicacion =
        rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { resultado ->
            if (resultado.resultCode == Activity.RESULT_OK) listo() else sinUbicacion(SinUbicacion.APAGADA)
        }

    fun revisarUbicacionActivada() {
        val pedido = LocationSettingsRequest.Builder().addLocationRequest(UbicacionGps.pedido()).build()
        LocationServices.getSettingsClient(contexto).checkLocationSettings(pedido)
            .addOnSuccessListener { listo() }
            .addOnFailureListener { error ->
                if (error is ResolvableApiException) {
                    activarUbicacion.launch(IntentSenderRequest.Builder(error.resolution).build())
                } else {
                    sinUbicacion(SinUbicacion.APAGADA)
                }
            }
    }

    val pedirPermisos =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            if (Permisos.tieneUbicacion(contexto)) revisarUbicacionActivada() else sinUbicacion(SinUbicacion.PERMISO)
        }

    return {
        if (Permisos.faltaAlguno(contexto)) pedirPermisos.launch(Permisos.paraViaje) else revisarUbicacionActivada()
    }
}
