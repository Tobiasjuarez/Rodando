package com.rodando.app.domain.usecase

import com.rodando.app.domain.model.Viaje
import com.rodando.app.domain.repository.ProgramadorDeEnvios
import com.rodando.app.domain.repository.RegistroDeRecorrido
import com.rodando.app.domain.repository.ViajeRepository

/**
 * RF02: apaga el registro del recorrido y cierra el viaje con la foto final.
 * El viaje queda guardado en el teléfono y se pide enviarlo apenas haya conexión.
 */
class FinalizarViajeUseCase(
    private val viajes: ViajeRepository,
    private val registro: RegistroDeRecorrido,
    private val envios: ProgramadorDeEnvios
) {

    suspend operator fun invoke(viajeId: String, fotoPath: String, fotoVerificada: Boolean = true): Viaje {
        // Primero se apaga la ubicación para que no entren puntos después de la hora de fin.
        registro.detener()
        val viaje = viajes.finalizarViaje(viajeId, fotoPath, fotoVerificada)
        envios.enviarAhora()
        return viaje
    }
}
