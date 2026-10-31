package com.rodando.app.presentation.inicio

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rodando.app.R
import com.rodando.app.presentation.common.EstadoVacio
import com.rodando.app.presentation.common.UiState
import com.rodando.app.presentation.ganancias.BarraObjetivoUi
import com.rodando.app.presentation.theme.RodandoTheme

@Composable
fun InicioScreen(
    viewModel: InicioViewModel,
    /** null mientras el flujo de RF01 no esté disponible: el botón queda deshabilitado. */
    onIniciarViaje: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    InicioContenido(estado = estado, onIniciarViaje = onIniciarViaje, modifier = modifier)
}

@Composable
fun InicioContenido(
    estado: UiState<InicioUi>,
    onIniciarViaje: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        when (estado) {
            UiState.Cargando -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            UiState.Vacio -> SinCampania()
            is UiState.Error -> EstadoVacio(
                icono = R.drawable.ic_auto,
                titulo = stringResource(R.string.inicio_error_titulo),
                mensaje = estado.mensaje,
                modifier = Modifier.padding(top = 96.dp)
            )
            is UiState.Contenido -> Inicio(estado.datos, onIniciarViaje)
            is UiState.Offline -> Inicio(estado.datosLocales, onIniciarViaje)
        }
    }
}

@Composable
private fun Inicio(inicio: InicioUi, onIniciarViaje: (() -> Unit)?) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(top = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = stringResource(R.string.inicio_saludo), style = MaterialTheme.typography.headlineSmall)
                Text(
                    text = if (inicio.pendientes > 0) {
                        pluralStringResource(R.plurals.inicio_pendientes, inicio.pendientes, inicio.pendientes)
                    } else {
                        stringResource(R.string.inicio_todo_sincronizado)
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Campaña activa
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = MaterialTheme.shapes.large,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(stringResource(R.string.inicio_campania_activa), style = MaterialTheme.typography.labelMedium)
                    Text(inicio.marca, style = MaterialTheme.typography.titleLarge)
                    Text(inicio.periodo, style = MaterialTheme.typography.bodyMedium)
                }
            }

            // Vinilo vigente
            if (inicio.vinilo != null && inicio.tarifa != null) {
                OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_auto),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Column {
                            Text(inicio.vinilo, style = MaterialTheme.typography.titleMedium)
                            Text(
                                inicio.tarifa,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Resumen del mes con barras compactas
            if (inicio.totalMes != null && inicio.barraKm != null && inicio.barraDias != null) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = pluralStringResource(R.plurals.inicio_este_mes, inicio.viajesDelMes, inicio.viajesDelMes),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(inicio.totalMes, style = MaterialTheme.typography.displaySmall)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            BarraCompacta(inicio.barraKm, Modifier.weight(1f))
                            BarraCompacta(inicio.barraDias, Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // Acción principal, abajo y al alcance del pulgar.
        Column(
            modifier = Modifier.padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { onIniciarViaje?.invoke() },
                enabled = inicio.puedeIniciarViaje && onIniciarViaje != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(stringResource(R.string.inicio_iniciar_viaje))
            }
            Text(
                text = stringResource(R.string.inicio_aviso_foto),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun BarraCompacta(barra: BarraObjetivoUi, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.semantics(mergeDescendants = true) {},
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = barra.valor,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        LinearProgressIndicator(
            progress = { barra.progreso },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
        )
    }
}

@Composable
private fun SinCampania() {
    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(Modifier.weight(1f))
        EstadoVacio(
            icono = R.drawable.ic_auto,
            titulo = stringResource(R.string.inicio_sin_campania_titulo),
            mensaje = stringResource(R.string.inicio_sin_campania_mensaje)
        )
        Spacer(Modifier.weight(1f))
        Button(
            onClick = {},
            enabled = false,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .height(56.dp)
        ) {
            Text(stringResource(R.string.inicio_iniciar_viaje))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun InicioPreview() {
    RodandoTheme {
        InicioContenido(
            estado = UiState.Contenido(
                InicioUi(
                    marca = "Pampa Bebidas",
                    periodo = "1 al 31 de octubre · Vinilo Parcial",
                    vinilo = "Vinilo Parcial",
                    tarifa = "$50.000/mes + $50/km",
                    pendientes = 2,
                    totalMes = "$82.140",
                    viajesDelMes = 31,
                    barraKm = BarraObjetivoUi("642,8 de 400 km", 1f, "Cumplido", true),
                    barraDias = BarraObjetivoUi("14 de 12 días", 1f, "Cumplido", true),
                    puedeIniciarViaje = true,
                    hayViajeEnCurso = false
                )
            ),
            onIniciarViaje = {}
        )
    }
}
