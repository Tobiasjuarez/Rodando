package com.rodando.app.presentation.encurso

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rodando.app.R
import com.rodando.app.domain.model.EstadoViaje
import com.rodando.app.domain.model.PuntoGps
import com.rodando.app.presentation.common.ChipEstado
import com.rodando.app.presentation.common.EstadoVacio
import com.rodando.app.presentation.common.UiState
import com.rodando.app.presentation.detalle.MapaRecorrido
import com.rodando.app.presentation.theme.RodandoTheme

@Composable
fun ViajeEnCursoScreen(
    viewModel: ViajeEnCursoViewModel,
    onFinalizar: (viajeId: String) -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    ViajeEnCursoContenido(estado = estado, onFinalizar = onFinalizar, onVolver = onVolver, modifier = modifier)
}

@Composable
fun ViajeEnCursoContenido(
    estado: UiState<ViajeEnCursoUi>,
    onFinalizar: (viajeId: String) -> Unit,
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
            Text(text = stringResource(R.string.en_curso_titulo), style = MaterialTheme.typography.headlineSmall)
        }
        when (estado) {
            UiState.Cargando -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            is UiState.Contenido -> EnCurso(estado.datos, onFinalizar)
            is UiState.Offline -> EnCurso(estado.datosLocales, onFinalizar)
            UiState.Vacio -> EstadoVacio(
                icono = R.drawable.ic_auto,
                titulo = stringResource(R.string.en_curso_sin_viaje_titulo),
                mensaje = stringResource(R.string.en_curso_sin_viaje_mensaje),
                modifier = Modifier.padding(top = 96.dp)
            )
            is UiState.Error -> EstadoVacio(
                icono = R.drawable.ic_auto,
                titulo = stringResource(R.string.en_curso_error_titulo),
                mensaje = estado.mensaje,
                modifier = Modifier.padding(top = 96.dp)
            )
        }
    }
}

@Composable
private fun EnCurso(viaje: ViajeEnCursoUi, onFinalizar: (viajeId: String) -> Unit) {
    var confirmando by rememberSaveable { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ChipEstado(estado = viaje.estado)
                Text(viaje.desde, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = MaterialTheme.shapes.large,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(20.dp)) {
                    Dato(stringResource(R.string.en_curso_tiempo), viaje.tiempo, Modifier.weight(1f))
                    Dato(stringResource(R.string.en_curso_distancia), viaje.distancia, Modifier.weight(1f))
                }
            }

            MapaRecorrido(
                puntos = viaje.puntos,
                mensajeSinPuntos = stringResource(R.string.en_curso_esperando_ubicacion),
                modifier = Modifier.fillMaxWidth().weight(1f)
            )

            if (viaje.sinSenal) {
                Text(
                    text = stringResource(R.string.en_curso_sin_senal),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
            if (viaje.estado == EstadoViaje.EN_REVISION) {
                Text(
                    text = stringResource(R.string.en_curso_aviso_revision),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
            Text(
                text = stringResource(R.string.en_curso_aviso_segundo_plano),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { confirmando = true },
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text(stringResource(R.string.en_curso_finalizar))
        }
        Spacer(Modifier.height(16.dp))
    }

    if (confirmando) {
        AlertDialog(
            onDismissRequest = { confirmando = false },
            title = { Text(stringResource(R.string.en_curso_confirmar_titulo)) },
            text = { Text(stringResource(R.string.en_curso_confirmar_mensaje)) },
            confirmButton = {
                TextButton(onClick = {
                    confirmando = false
                    onFinalizar(viaje.viajeId)
                }) { Text(stringResource(R.string.en_curso_confirmar_si)) }
            },
            dismissButton = {
                OutlinedButton(onClick = { confirmando = false }) { Text(stringResource(R.string.en_curso_confirmar_no)) }
            }
        )
    }
}

@Composable
private fun Dato(etiqueta: String, valor: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.semantics(mergeDescendants = true) {}) {
        Text(etiqueta, style = MaterialTheme.typography.labelMedium)
        Text(valor, style = MaterialTheme.typography.headlineMedium)
    }
}

@Preview(showBackground = true)
@Composable
private fun EnCursoPreview() {
    val puntos = (0..8).map { i -> PuntoGps(-34.64 + i * 0.0004, -58.565 + i * 0.002, i * 1000L, 5f) }
    RodandoTheme {
        ViajeEnCursoContenido(
            estado = UiState.Contenido(
                ViajeEnCursoUi("viaje-1", EstadoViaje.EN_CURSO, "Desde las 08:42", "00:12:40", "1,6 km", puntos)
            ),
            onFinalizar = {},
            onVolver = {}
        )
    }
}
