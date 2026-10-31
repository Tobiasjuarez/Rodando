package com.rodando.app.presentation.historial

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rodando.app.R
import com.rodando.app.presentation.common.EstadoVacio
import com.rodando.app.presentation.theme.RodandoTheme

/**
 * Pantalla de Historial (estado inicial).
 *
 * Por ahora muestra el estado vacío del wireframe. Cuando exista HistorialViewModel,
 * recibirá un UiState con los datos guardados en Room.
 */
@Composable
fun HistorialScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = stringResource(R.string.historial_titulo),
            style = MaterialTheme.typography.headlineSmall
        )
        EstadoVacio(
            icono = R.drawable.ic_historial,
            titulo = stringResource(R.string.historial_vacio_titulo),
            mensaje = stringResource(R.string.historial_vacio_mensaje)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HistorialScreenPreview() {
    RodandoTheme {
        HistorialScreen()
    }
}
