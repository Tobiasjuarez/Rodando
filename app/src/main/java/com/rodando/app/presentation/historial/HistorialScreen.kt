package com.rodando.app.presentation.historial

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rodando.app.R
import com.rodando.app.domain.model.EstadoViaje
import com.rodando.app.presentation.common.ChipEstado
import com.rodando.app.presentation.common.EstadoVacio
import com.rodando.app.presentation.common.UiState
import com.rodando.app.presentation.theme.RodandoTheme

@Composable
fun HistorialScreen(
    viewModel: HistorialViewModel,
    onAbrirViaje: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    HistorialContenido(estado = estado, onAbrirViaje = onAbrirViaje, modifier = modifier)
}

/** Versión sin ViewModel: dibuja cualquier estado (sirve para las vistas previas). */
@Composable
fun HistorialContenido(
    estado: UiState<HistorialUi>,
    onAbrirViaje: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = stringResource(R.string.historial_titulo),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 8.dp)
        )
        when (estado) {
            UiState.Cargando -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            UiState.Vacio -> EstadoVacio(
                icono = R.drawable.ic_historial,
                titulo = stringResource(R.string.historial_vacio_titulo),
                mensaje = stringResource(R.string.historial_vacio_mensaje),
                modifier = Modifier.padding(top = 24.dp)
            )
            is UiState.Error -> EstadoVacio(
                icono = R.drawable.ic_historial,
                titulo = stringResource(R.string.historial_error_titulo),
                mensaje = estado.mensaje,
                modifier = Modifier.padding(top = 24.dp)
            )
            is UiState.Contenido -> ListaViajes(estado.datos, onAbrirViaje)
            is UiState.Offline -> ListaViajes(estado.datosLocales, onAbrirViaje)
        }
    }
}

@Composable
private fun ListaViajes(historial: HistorialUi, onAbrirViaje: (String) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        if (historial.pendientes > 0) {
            item(key = "aviso") { AvisoPendientes(historial.pendientes) }
        }
        historial.grupos.forEach { grupo ->
            item(key = "grupo-${grupo.titulo}") {
                Text(
                    text = grupo.titulo,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 4.dp)
                )
            }
            items(grupo.viajes, key = { it.id }) { item ->
                FilaViaje(item = item, onClick = { onAbrirViaje(item.id) })
            }
        }
    }
}

@Composable
private fun AvisoPendientes(cantidad: Int) {
    Surface(
        color = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
    ) {
        Text(
            text = pluralStringResource(R.plurals.historial_aviso_pendientes, cantidad, cantidad),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
private fun FilaViaje(item: ItemViaje, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClickLabel = stringResource(R.string.historial_ver_detalle), onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_auto),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(20.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = item.fecha, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = item.detalle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        ChipEstado(estado = item.estado)
    }
}

@Preview(showBackground = true)
@Composable
private fun HistorialPreview() {
    RodandoTheme {
        HistorialContenido(
            estado = UiState.Contenido(
                HistorialUi(
                    grupos = listOf(
                        GrupoViajes(
                            "HOY",
                            listOf(
                                ItemViaje("1", "Hoy · 18:10", "17,9 km · 39 min", EstadoViaje.PENDIENTE),
                                ItemViaje("2", "Hoy · 08:42", "18,4 km · 42 min", EstadoViaje.SINCRONIZADO)
                            )
                        )
                    ),
                    pendientes = 1
                )
            ),
            onAbrirViaje = {}
        )
    }
}
