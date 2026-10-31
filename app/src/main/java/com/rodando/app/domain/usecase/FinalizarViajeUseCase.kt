package com.rodando.app.domain.usecase

import com.rodando.app.domain.model.Viaje
import com.rodando.app.domain.repository.RegistroDeRecorrido
import com.rodando.app.domain.repository.ViajeRepository

/**
 * RF02: apaga el registro del recorrido y cierra el viaje con la foto final.
 * El viaje queda pendiente de envío (o no computable si fue muy corto).
 */
class FinalizarViajeUseCase(
    private val viajes: ViajeRepository,
    private val registro: RegistroDeRecorrido
) {

    suspend operator fun invoke(viajeId: String, fotoPath: String): Viaje {
        // Primero se apaga la ubicación para que no entren puntos después de la hora de fin.
        registro.detener()
        return viajes.finalizarViaje(viajeId, fotoPath)
    }
}
