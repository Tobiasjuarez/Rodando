package com.rodando.app.presentation.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rodando.app.R
import com.rodando.app.presentation.theme.RodandoTheme

/**
 * Franja arriba de todas las pantallas mientras no hay red. La app sigue funcionando:
 * avisa que lo nuevo queda en el teléfono y desde cuándo no se sincroniza.
 */
@Composable
fun AvisoSinConexion(ultimaSincronizacion: String?, modifier: Modifier = Modifier) {
    Surface(
        color = MaterialTheme.colorScheme.inverseSurface,
        contentColor = MaterialTheme.colorScheme.inverseOnSurface,
        // TalkBack lo anuncia apenas aparece.
        modifier = modifier
            .fillMaxWidth()
            .semantics { liveRegion = LiveRegionMode.Polite }
    ) {
        Text(
            text = buildString {
                append(stringResource(R.string.sin_conexion))
                ultimaSincronizacion?.let { append(" ").append(stringResource(R.string.sin_conexion_ultima, it)) }
            },
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 10.dp)
        )
    }
}

@Preview
@Composable
private fun AvisoPreview() {
    RodandoTheme { AvisoSinConexion("hoy a las 15:04") }
}
