package com.rodando.app.presentation.common

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.rodando.app.R
import com.rodando.app.domain.model.EstadoViaje

@StringRes
fun EstadoViaje.etiqueta(): Int = when (this) {
    EstadoViaje.EN_CURSO -> R.string.estado_en_curso
    EstadoViaje.EN_REVISION -> R.string.estado_en_revision
    EstadoViaje.PENDIENTE -> R.string.estado_pendiente
    EstadoViaje.SINCRONIZADO -> R.string.estado_sincronizado
    EstadoViaje.NO_COMPUTABLE -> R.string.estado_no_computable
}

/**
 * Estado de sincronización de un viaje. Siempre lleva texto, nunca solo color (RNF06).
 */
@Composable
fun ChipEstado(estado: EstadoViaje, modifier: Modifier = Modifier) {
    val colores = MaterialTheme.colorScheme
    val (fondo: Color, texto: Color) = when (estado) {
        EstadoViaje.EN_CURSO -> colores.primaryContainer to colores.onPrimaryContainer
        EstadoViaje.EN_REVISION -> colores.errorContainer to colores.onErrorContainer
        EstadoViaje.PENDIENTE -> colores.tertiaryContainer to colores.onTertiaryContainer
        EstadoViaje.SINCRONIZADO -> colores.secondaryContainer to colores.onSecondaryContainer
        EstadoViaje.NO_COMPUTABLE -> colores.surfaceContainerHigh to colores.onSurfaceVariant
    }
    Surface(color = fondo, contentColor = texto, shape = RoundedCornerShape(8.dp), modifier = modifier) {
        Text(
            text = stringResource(estado.etiqueta()),
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}
