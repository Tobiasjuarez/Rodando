package com.rodando.app.presentation.detalle

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.rodando.app.R
import com.rodando.app.domain.model.PuntoGps

/**
 * Dibuja el recorrido a partir de los puntos GPS, sin mapa de fondo.
 * El mapa real (osmdroid) se suma junto con el registro por GPS.
 */
@Composable
fun MapaRecorrido(
    puntos: List<PuntoGps>,
    modifier: Modifier = Modifier,
    mensajeSinPuntos: String = stringResource(R.string.detalle_sin_puntos)
) {
    val colorRecorrido = MaterialTheme.colorScheme.primary
    val descripcion = stringResource(R.string.detalle_mapa)
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .semantics { contentDescription = descripcion }
    ) {
        if (puntos.size < 2) {
            Text(
                text = mensajeSinPuntos,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.Center)
            )
            return@Box
        }
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            val minLat = puntos.minOf { it.latitud }
            val maxLat = puntos.maxOf { it.latitud }
            val minLon = puntos.minOf { it.longitud }
            val maxLon = puntos.maxOf { it.longitud }
            val ancho = (maxLon - minLon).coerceAtLeast(1e-6)
            val alto = (maxLat - minLat).coerceAtLeast(1e-6)
            // Misma escala en los dos ejes para no deformar el recorrido.
            val escala = minOf(size.width / ancho, size.height / alto)
            val margenX = ((size.width - ancho * escala) / 2).toFloat()
            val margenY = ((size.height - alto * escala) / 2).toFloat()

            fun posicion(p: PuntoGps) = Offset(
                x = margenX + ((p.longitud - minLon) * escala).toFloat(),
                y = margenY + ((maxLat - p.latitud) * escala).toFloat()
            )

            val camino = Path().apply {
                val inicio = posicion(puntos.first())
                moveTo(inicio.x, inicio.y)
                puntos.drop(1).forEach { val o = posicion(it); lineTo(o.x, o.y) }
            }
            drawPath(
                path = camino,
                color = colorRecorrido,
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
            // Inicio: círculo hueco. Fin: círculo lleno.
            drawCircle(colorRecorrido, radius = 6.dp.toPx(), center = posicion(puntos.first()), style = Stroke(3.dp.toPx()))
            drawCircle(colorRecorrido, radius = 7.dp.toPx(), center = posicion(puntos.last()))
        }
    }
}
