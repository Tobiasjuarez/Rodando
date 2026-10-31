package com.rodando.app.presentation.guardado

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rodando.app.R
import com.rodando.app.domain.model.EstadoViaje
import com.rodando.app.presentation.common.ChipEstado
import com.rodando.app.presentation.common.EstadoVacio
import com.rodando.app.presentation.common.FilaDato
import com.rodando.app.presentation.common.UiState
import com.rodando.app.presentation.theme.RodandoTheme

@Composable
fun ViajeGuardadoScreen(
    viewModel: ViajeGuardadoViewModel,
    onVolverAlInicio: () -> Unit,
    onVerDetalle: (viajeId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    ViajeGuardadoContenido(estado, onVolverAlInicio, onVerDetalle, modifier)
}

@Composable
fun ViajeGuardadoContenido(
    estado: UiState<ViajeGuardadoUi>,
    onVolverAlInicio: () -> Unit,
    onVerDetalle: (viajeId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize().padding(horizontal = 24.dp)) {
        Column(modifier = Modifier.weight(1f)) {
            when (estado) {
                UiState.Cargando -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                is UiState.Contenido -> Guardado(estado.datos)
                is UiState.Offline -> Guardado(estado.datosLocales)
                is UiState.Error -> EstadoVacio(
                    icono = R.drawable.ic_historial,
                    titulo = stringResource(R.string.detalle_error_titulo),
                    mensaje = estado.mensaje,
                    modifier = Modifier.padding(top = 96.dp)
                )
                UiState.Vacio -> Unit
            }
        }
        Column(
            modifier = Modifier.padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(onClick = onVolverAlInicio, modifier = Modifier.fillMaxWidth().height(56.dp)) {
                Text(stringResource(R.string.guardado_volver_inicio))
            }
            val viajeId = when (estado) {
                is UiState.Contenido -> estado.datos.viajeId
                is UiState.Offline -> estado.datosLocales.viajeId
                else -> null
            }
            if (viajeId != null) {
                OutlinedButton(
                    onClick = { onVerDetalle(viajeId) },
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text(stringResource(R.string.guardado_ver_detalle))
                }
            }
        }
    }
}

@Composable
private fun Guardado(viaje: ViajeGuardadoUi) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 72.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(72.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(painterResource(R.drawable.ic_listo), contentDescription = null, modifier = Modifier.size(40.dp))
            }
        }
        Text(viaje.titulo, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
        ChipEstado(estado = viaje.estado)
        Text(
            text = viaje.mensaje,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Column(modifier = Modifier.fillMaxWidth()) {
            FilaDato(stringResource(R.string.detalle_distancia), viaje.distancia)
            FilaDato(stringResource(R.string.detalle_duracion), viaje.duracion)
            viaje.ganancia?.let { FilaDato(stringResource(R.string.guardado_ganancia), it) }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GuardadoPreview() {
    RodandoTheme {
        ViajeGuardadoContenido(
            estado = UiState.Contenido(
                ViajeGuardadoUi(
                    "viaje-1", EstadoViaje.PENDIENTE, "¡Viaje guardado!",
                    "Quedó en el teléfono y se va a enviar solo cuando haya conexión.", "18,4 km", "42 min", "+$920"
                )
            ),
            onVolverAlInicio = {},
            onVerDetalle = {}
        )
    }
}
