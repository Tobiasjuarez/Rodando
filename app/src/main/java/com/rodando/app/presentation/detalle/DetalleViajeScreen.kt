package com.rodando.app.presentation.detalle

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rodando.app.R
import com.rodando.app.domain.model.EstadoViaje
import com.rodando.app.domain.model.PuntoGps
import com.rodando.app.presentation.common.ChipEstado
import com.rodando.app.presentation.common.EstadoVacio
import com.rodando.app.presentation.common.FilaDato
import com.rodando.app.presentation.common.UiState
import com.rodando.app.presentation.theme.RodandoTheme

@Composable
fun DetalleViajeScreen(
    viewModel: DetalleViajeViewModel,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    DetalleViajeContenido(estado = estado, onVolver = onVolver, modifier = modifier)
}

@Composable
fun DetalleViajeContenido(
    estado: UiState<DetalleViajeUi>,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 8.dp, end = 24.dp, top = 16.dp, bottom = 8.dp)
        ) {
            IconButton(onClick = onVolver) {
                Icon(painterResource(R.drawable.ic_volver), contentDescription = stringResource(R.string.volver))
            }
            Text(text = stringResource(R.string.detalle_titulo), style = MaterialTheme.typography.headlineSmall)
        }
        when (estado) {
            UiState.Cargando -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            is UiState.Contenido -> Detalle(estado.datos)
            is UiState.Offline -> Detalle(estado.datosLocales)
            is UiState.Error -> EstadoVacio(
                icono = R.drawable.ic_historial,
                titulo = stringResource(R.string.detalle_error_titulo),
                mensaje = estado.mensaje,
                modifier = Modifier.padding(top = 24.dp)
            )
            UiState.Vacio -> Unit
        }
    }
}

@Composable
private fun Detalle(detalle: DetalleViajeUi) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MapaRecorrido(
            puntos = detalle.puntos,
            modifier = Modifier
                .fillMaxWidth()
                .height(184.dp)
        )
        ChipEstado(estado = detalle.estado)
        Text(text = detalle.horario, style = MaterialTheme.typography.titleMedium)
        Column {
            FilaDato(stringResource(R.string.detalle_distancia), detalle.distancia)
            FilaDato(stringResource(R.string.detalle_duracion), detalle.duracion)
            detalle.ganancia?.let { FilaDato(stringResource(R.string.guardado_ganancia), it) }
        }
        Text(
            text = stringResource(R.string.detalle_fotos),
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(top = 8.dp)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Foto(stringResource(R.string.detalle_foto_inicio, detalle.horaInicio), Modifier.weight(1f))
            detalle.horaFin?.let { hora ->
                Foto(stringResource(R.string.detalle_foto_fin, hora), Modifier.weight(1f))
            }
        }
    }
}

/** Lugar de la foto del vinilo. La imagen real se muestra cuando la app saque fotos con CameraX. */
@Composable
private fun Foto(texto: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .height(104.dp)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_camara),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = texto,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DetallePreview() {
    val puntos = (0..10).map { i ->
        PuntoGps(-34.64 + i * 0.004 + if (i % 3 == 0) 0.003 else 0.0, -58.56 + i * 0.018, i * 1000L, 5f)
    }
    RodandoTheme {
        DetalleViajeContenido(
            estado = UiState.Contenido(
                DetalleViajeUi("Hoy · 08:42 a 09:24", "18,4 km", "42 min", EstadoViaje.SINCRONIZADO, "08:42", "09:24", puntos, "+$920")
            ),
            onVolver = {}
        )
    }
}
