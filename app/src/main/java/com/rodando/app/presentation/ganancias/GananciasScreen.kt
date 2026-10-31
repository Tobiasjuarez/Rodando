package com.rodando.app.presentation.ganancias

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
 * Pantalla de Ganancias (estado inicial).
 *
 * Por ahora muestra el estado vacío del wireframe. Cuando exista GananciasViewModel,
 * recibirá un UiState con los datos guardados en Room.
 */
@Composable
fun GananciasScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = stringResource(R.string.ganancias_titulo),
            style = MaterialTheme.typography.headlineSmall
        )
        EstadoVacio(
            icono = R.drawable.ic_ganancias,
            titulo = stringResource(R.string.ganancias_vacio_titulo),
            mensaje = stringResource(R.string.ganancias_vacio_mensaje)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GananciasScreenPreview() {
    RodandoTheme {
        GananciasScreen()
    }
}
