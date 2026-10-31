package com.rodando.app.presentation.ganancias

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/**
 * Barra de avance hacia un mínimo del mes (400 km o 12 días con viajes).
 * TalkBack lee la etiqueta, el valor y lo que falta como un solo elemento.
 */
@Composable
fun BarraObjetivo(etiqueta: String, barra: BarraObjetivoUi, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {},
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = etiqueta,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            Text(text = barra.valor, style = MaterialTheme.typography.labelLarge)
        }
        LinearProgressIndicator(
            progress = { barra.progreso },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
        )
        Text(
            text = barra.ayuda,
            style = MaterialTheme.typography.labelMedium,
            color = if (barra.cumplido) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
