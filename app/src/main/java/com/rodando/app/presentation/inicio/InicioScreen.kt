package com.rodando.app.presentation.inicio

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rodando.app.presentation.theme.RodandoTheme

/**
 * Pantalla de Inicio (placeholder).
 *
 * Más adelante recibirá un UiState desde InicioViewModel con la campaña activa,
 * el resumen del mes y el estado de sincronización. El botón "Iniciar viaje"
 * queda deshabilitado hasta implementar RF01.
 */
@Composable
fun InicioScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "Rodando", style = MaterialTheme.typography.headlineMedium)
        Text(
            text = "Tu auto te paga mientras hacés tus recorridos de siempre.",
            style = MaterialTheme.typography.bodyLarge
        )
        Button(
            onClick = { /* RF01: iniciar viaje con foto */ },
            enabled = false,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Iniciar viaje")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun InicioScreenPreview() {
    RodandoTheme {
        InicioScreen()
    }
}
