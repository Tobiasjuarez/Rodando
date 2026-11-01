package com.rodando.app.presentation.detalle

import android.content.Context
import android.graphics.drawable.GradientDrawable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.rodando.app.R
import com.rodando.app.domain.model.PuntoGps
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.CopyrightOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.io.File

/**
 * Mapa (osmdroid, con mosaicos de CARTO sobre datos de OpenStreetMap) con el recorrido y la última posición.
 *
 * - Con [seguirUbicacion] (viaje en curso) el mapa se centra en la última posición cada vez que llega una.
 * - Sin él (detalle) se encuadra el recorrido completo.
 *
 * Sin conexión no se descargan los mosaicos, pero el recorrido se dibuja igual.
 * En las vistas previas de Android Studio se usa un dibujo simple, sin mapa.
 */
@Composable
fun MapaRecorrido(
    puntos: List<PuntoGps>,
    modifier: Modifier = Modifier,
    mensajeSinPuntos: String = stringResource(R.string.detalle_sin_puntos),
    seguirUbicacion: Boolean = false
) {
    val descripcion = stringResource(R.string.detalle_mapa)
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .semantics { contentDescription = descripcion }
    ) {
        if (LocalInspectionMode.current) {
            DibujoRecorrido(puntos, MaterialTheme.colorScheme.primary, Modifier.fillMaxSize().padding(24.dp))
        } else {
            MapaOsm(puntos, seguirUbicacion, MaterialTheme.colorScheme.primary.toArgb(), Modifier.fillMaxSize())
        }
        if (puntos.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                modifier = Modifier.align(Alignment.Center)
            ) {
                Text(
                    text = mensajeSinPuntos,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun MapaOsm(puntos: List<PuntoGps>, seguirUbicacion: Boolean, color: Int, modifier: Modifier) {
    val contexto = LocalContext.current
    val mapa = remember { crearMapa(contexto) }
    val estado = remember { EstadoMapa() }

    // El MapView tiene que enterarse de cuándo la pantalla se pausa y se destruye.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observador = LifecycleEventObserver { _, evento ->
            when (evento) {
                Lifecycle.Event.ON_RESUME -> mapa.onResume()
                Lifecycle.Event.ON_PAUSE -> mapa.onPause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observador)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observador)
            mapa.onDetach()
        }
    }

    AndroidView(
        factory = { mapa },
        modifier = modifier,
        update = { vista -> dibujar(vista, puntos, seguirUbicacion, color, estado) }
    )
}

/** Recuerda si ya se ubicó la cámara del mapa, para no pelear con el conductor si lo mueve. */
private class EstadoMapa {
    var encuadrado = false
    var cantidadDibujada = -1
}

private fun crearMapa(contexto: Context): MapView {
    Configuration.getInstance().apply {
        load(contexto, contexto.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
        // Los servidores de mosaicos rechazan pedidos que no identifican a la app.
        userAgentValue = "Rodando/1.0 (${contexto.packageName}; TPO DAI UADE)"
        osmdroidBasePath = File(contexto.cacheDir, "osmdroid").apply { mkdirs() }
        osmdroidTileCache = File(osmdroidBasePath, "tiles").apply { mkdirs() }
    }
    return MapView(contexto).apply {
        setTileSource(CARTO_VOYAGER)
        setMultiTouchControls(true)
        isTilesScaledToDpi = true
        zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
        controller.setZoom(12.0)
        controller.setCenter(OBELISCO)
    }
}

private fun dibujar(
    mapa: MapView,
    puntos: List<PuntoGps>,
    seguirUbicacion: Boolean,
    color: Int,
    estado: EstadoMapa
) {
    if (puntos.size == estado.cantidadDibujada) return
    estado.cantidadDibujada = puntos.size

    val geo = puntos.map { GeoPoint(it.latitud, it.longitud) }
    mapa.overlays.clear()
    mapa.overlays.add(CopyrightOverlay(mapa.context))
    if (geo.size >= 2) {
        mapa.overlays.add(
            Polyline(mapa).apply {
                setPoints(geo)
                outlinePaint.color = color
                outlinePaint.strokeWidth = 12f
                infoWindow = null
            }
        )
    }
    geo.lastOrNull()?.let { ultimo ->
        mapa.overlays.add(
            Marker(mapa).apply {
                position = ultimo
                icon = puntoActual(color)
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                infoWindow = null
                setOnMarkerClickListener { _, _ -> true }
            }
        )
    }

    when {
        geo.isEmpty() -> Unit
        seguirUbicacion -> {
            if (!estado.encuadrado) mapa.controller.setZoom(16.5)
            mapa.controller.animateTo(geo.last())
            estado.encuadrado = true
        }
        !estado.encuadrado && geo.size >= 2 -> {
            // Hay que esperar a que el mapa tenga tamaño para encuadrar el recorrido.
            mapa.post { mapa.zoomToBoundingBox(BoundingBox.fromGeoPoints(geo), false, 64) }
            estado.encuadrado = true
        }
        !estado.encuadrado -> {
            mapa.controller.setZoom(16.0)
            mapa.controller.setCenter(geo.last())
            estado.encuadrado = true
        }
    }
    mapa.invalidate()
}

/** Círculo con borde blanco para la última posición. */
private fun puntoActual(color: Int) = GradientDrawable().apply {
    shape = GradientDrawable.OVAL
    setColor(color)
    setStroke(6, android.graphics.Color.WHITE)
    setSize(44, 44)
}

private val OBELISCO = GeoPoint(-34.6037, -58.3816)

/**
 * Mosaicos de CARTO (datos de OpenStreetMap), de uso libre para proyectos no comerciales.
 * Se usan en lugar de los servidores de OpenStreetMap, que limitan a las apps.
 */
private val CARTO_VOYAGER = XYTileSource(
    "CartoVoyager",
    1,
    19,
    256,
    ".png",
    arrayOf(
        "https://a.basemaps.cartocdn.com/rastertiles/voyager/",
        "https://b.basemaps.cartocdn.com/rastertiles/voyager/",
        "https://c.basemaps.cartocdn.com/rastertiles/voyager/",
        "https://d.basemaps.cartocdn.com/rastertiles/voyager/"
    ),
    "© OpenStreetMap contributors © CARTO"
)

/** Dibujo simple del recorrido, sin mapa de fondo, para las vistas previas. */
@Composable
private fun DibujoRecorrido(puntos: List<PuntoGps>, colorRecorrido: Color, modifier: Modifier) {
    if (puntos.size < 2) return
    Canvas(modifier = modifier) {
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
        drawCircle(colorRecorrido, radius = 6.dp.toPx(), center = posicion(puntos.first()), style = Stroke(3.dp.toPx()))
        drawCircle(colorRecorrido, radius = 7.dp.toPx(), center = posicion(puntos.last()))
    }
}
