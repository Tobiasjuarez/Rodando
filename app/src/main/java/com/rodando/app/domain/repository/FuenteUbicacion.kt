package com.rodando.app.domain.repository

import com.rodando.app.domain.model.PuntoGps
import kotlinx.coroutines.flow.Flow

/**
 * De dónde salen las posiciones durante un viaje. Mientras se recolecta el Flow,
 * la fuente está encendida; al cancelarlo se apaga (RNF04: GPS solo durante el viaje).
 */
interface FuenteUbicacion {
    fun ubicaciones(): Flow<PuntoGps>
}
