package com.rodando.app.presentation.common

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
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
import com.rodando.app.domain.model.EstadoEnvio
import com.rodando.app.domain.model.EstadoVerificacion

@StringRes
fun EstadoVerificacion.etiqueta(): Int = when (this) {
    EstadoVerificacion.POR_APROBAR -> R.string.estado_por_aprobar
    EstadoVerificacion.EN_REVISION -> R.string.estado_en_revision
    EstadoVerificacion.APROBADO -> R.string.estado_aprobado
    EstadoVerificacion.RECHAZADO -> R.string.estado_rechazado
    EstadoVerificacion.NO_COMPUTABLE -> R.string.estado_no_computable
}

@StringRes
fun EstadoEnvio.etiqueta(): Int = when (this) {
    EstadoEnvio.PENDIENTE -> R.string.envio_pendiente
    EstadoEnvio.ENVIADO -> R.string.envio_enviado
}

/**
 * Estado de verificación de un viaje ("Por aprobar", "Aprobado", ...), o "En curso" si no terminó.
 * Siempre lleva texto, nunca solo color (RNF06).
 */
@Composable
fun ChipEstado(enCurso: Boolean, verificacion: EstadoVerificacion, modifier: Modifier = Modifier) {
    val colores = MaterialTheme.colorScheme
    val (fondo: Color, texto: Color) = when {
        enCurso -> colores.primaryContainer to colores.onPrimaryContainer
        else -> when (verificacion) {
            EstadoVerificacion.POR_APROBAR -> colores.tertiaryContainer to colores.onTertiaryContainer
            EstadoVerificacion.EN_REVISION, EstadoVerificacion.RECHAZADO ->
                colores.errorContainer to colores.onErrorContainer
            EstadoVerificacion.APROBADO -> colores.secondaryContainer to colores.onSecondaryContainer
            EstadoVerificacion.NO_COMPUTABLE -> colores.surfaceContainerHigh to colores.onSurfaceVariant
        }
    }
    val etiqueta = if (enCurso) R.string.estado_en_curso else verificacion.etiqueta()
    Surface(color = fondo, contentColor = texto, shape = RoundedCornerShape(8.dp), modifier = modifier) {
        Text(
            text = stringResource(etiqueta),
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

/** Si el viaje ya llegó al servidor. Es independiente de la aprobación, por eso va en otro chip. */
@Composable
fun ChipEnvio(envio: EstadoEnvio, modifier: Modifier = Modifier) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Text(
            text = stringResource(envio.etiqueta()),
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}
