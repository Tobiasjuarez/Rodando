package com.rodando.app.domain.usecase

import com.rodando.app.domain.model.Viaje
import com.rodando.app.domain.repository.CampaniaRepository
import com.rodando.app.domain.repository.RegistroDeRecorrido
import com.rodando.app.domain.repository.ViajeRepository
import com.rodando.app.domain.repository.ViniloRepository
import kotlinx.coroutines.flow.first

/**
 * RF01: con la foto del vinilo ya tomada, crea el viaje y empieza a registrar el recorrido.
 * Falla si no hay campaña activa, si el vinilo no está instalado o si ya hay un viaje en curso.
 */
class IniciarViajeUseCase(
    private val campanias: CampaniaRepository,
    private val vinilos: ViniloRepository,
    private val viajes: ViajeRepository,
    private val registro: RegistroDeRecorrido
) {

    suspend operator fun invoke(fotoPath: String, intentosFoto: Int, enRevision: Boolean): Viaje {
        val campania = checkNotNull(campanias.observarCampaniaActiva().first()) { "No tenés una campaña activa." }
        val vinilo = vinilos.observarVinilo().first()?.takeIf { it.fechaInstalacion != null }
            ?: throw IllegalStateException("Todavía no tenés un vinilo instalado.")
        val viaje = viajes.iniciarViaje(campania.id, vinilo.tipo, fotoPath, intentosFoto, enRevision)
        registro.iniciar(viaje.id)
        return viaje
    }
}
