package com.rodando.app.presentation.ganancias

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rodando.app.R
import com.rodando.app.presentation.common.EstadoVacio
import com.rodando.app.presentation.common.FilaDato
import com.rodando.app.presentation.common.UiState
import com.rodando.app.presentation.theme.RodandoTheme

@Composable
fun GananciasScreen(
    viewModel: GananciasViewModel,
    onVerViajes: () -> Unit,
    modifier: Modifier = Modifier
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    GananciasContenido(estado = estado, onVerViajes = onVerViajes, modifier = modifier)
}

@Composable
fun GananciasContenido(
    estado: UiState<GananciasUi>,
    onVerViajes: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = stringResource(R.string.ganancias_titulo),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 8.dp)
        )
        when (estado) {
            UiState.Cargando -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            UiState.Vacio -> EstadoVacio(
                icono = R.drawable.ic_ganancias,
                titulo = stringResource(R.string.ganancias_sin_vinilo_titulo),
                mensaje = stringResource(R.string.ganancias_sin_vinilo_mensaje),
                modifier = Modifier.padding(top = 24.dp)
            )
            is UiState.Error -> EstadoVacio(
                icono = R.drawable.ic_ganancias,
                titulo = stringResource(R.string.ganancias_error_titulo),
                mensaje = estado.mensaje,
                modifier = Modifier.padding(top = 24.dp)
            )
            is UiState.Contenido -> Resumen(estado.datos, onVerViajes)
            is UiState.Offline -> Resumen(estado.datosLocales, onVerViajes)
        }
    }
}

@Composable
private fun Resumen(ganancias: GananciasUi, onVerViajes: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Total estimado del mes
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            shape = MaterialTheme.shapes.large,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = ganancias.encabezado, style = MaterialTheme.typography.labelMedium)
                Text(text = ganancias.total, style = MaterialTheme.typography.displaySmall)
                Text(text = ganancias.detalle, style = MaterialTheme.typography.bodyMedium)
            }
        }

        // Objetivos para cobrar el fijo entero
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainer,
            shape = MaterialTheme.shapes.large,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(R.string.ganancias_objetivos, ganancias.fijoEntero),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                BarraObjetivo(stringResource(R.string.ganancias_km), ganancias.barraKm)
                BarraObjetivo(stringResource(R.string.ganancias_dias), ganancias.barraDias)
            }
        }

        Column {
            FilaDato(stringResource(R.string.ganancias_computables), ganancias.viajesComputables.toString())
            FilaDato(stringResource(R.string.ganancias_pendientes), ganancias.pendientes.toString())
            FilaDato(stringResource(R.string.ganancias_no_computables), ganancias.noComputables.toString())
        }

        OutlinedButton(
            onClick = onVerViajes,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text(stringResource(R.string.ganancias_ver_viajes))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GananciasPreview() {
    RodandoTheme {
        GananciasContenido(
            estado = UiState.Contenido(
                GananciasUi(
                    encabezado = "OCTUBRE · ESTIMADO",
                    total = "$30.133",
                    detalle = "$20.833 de fijo + 186,0 km × $50",
                    fijoEntero = "$50.000",
                    barraKm = BarraObjetivoUi("186,0 de 400 km", 0.465f, "Te faltan 214 km", false),
                    barraDias = BarraObjetivoUi("5 de 12 días", 0.42f, "Te faltan 7 días con viajes", false),
                    viajesComputables = 9,
                    pendientes = 0,
                    noComputables = 0
                )
            ),
            onVerViajes = {}
        )
    }
}
