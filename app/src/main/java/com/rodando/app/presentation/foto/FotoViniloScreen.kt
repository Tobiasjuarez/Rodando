package com.rodando.app.presentation.foto

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rodando.app.R
import com.rodando.app.presentation.theme.RodandoTheme

@Composable
fun FotoViniloScreen(
    viewModel: FotoViniloViewModel,
    onListo: (viajeId: String) -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    LaunchedEffect(estado.viajeListo) { estado.viajeListo?.let(onListo) }
    FotoViniloContenido(estado = estado, onCapturar = viewModel::capturar, onVolver = onVolver, modifier = modifier)
}

@Composable
fun FotoViniloContenido(
    estado: FotoUi,
    onCapturar: () -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    val esInicio = estado.momento == MomentoFoto.INICIO
    Column(modifier = modifier.fillMaxSize()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 8.dp, end = 24.dp, top = 16.dp, bottom = 8.dp)
        ) {
            IconButton(onClick = onVolver, enabled = !estado.guardando) {
                Icon(painterResource(R.drawable.ic_volver), contentDescription = stringResource(R.string.volver))
            }
            Column {
                Text(
                    text = stringResource(if (esInicio) R.string.foto_titulo_inicio else R.string.foto_titulo_fin),
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = stringResource(if (esInicio) R.string.foto_subtitulo_inicio else R.string.foto_subtitulo_fin),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Visor(Modifier.weight(1f).fillMaxWidth())
            Text(
                text = stringResource(R.string.foto_consejo),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            estado.error?.let {
                Text(text = it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
            }
        }

        Button(
            onClick = onCapturar,
            enabled = !estado.guardando,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .height(56.dp)
        ) {
            if (estado.guardando) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            } else {
                Text(stringResource(if (esInicio) R.string.foto_sacar_inicio else R.string.foto_sacar_fin))
            }
        }
    }
}

/** Vista de cámara simulada con el recuadro donde tiene que entrar el vinilo. */
@Composable
private fun Visor(modifier: Modifier = Modifier) {
    val descripcion = stringResource(R.string.foto_visor)
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.inverseSurface)
            .semantics { contentDescription = descripcion },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(200.dp)
                .border(2.dp, MaterialTheme.colorScheme.inverseOnSurface, RoundedCornerShape(12.dp)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_camara),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.inverseOnSurface,
                modifier = Modifier.size(40.dp)
            )
            Text(
                text = stringResource(R.string.foto_encuadre),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.inverseOnSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp, start = 16.dp, end = 16.dp)
            )
        }
        Text(
            text = stringResource(R.string.foto_simulada),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.inverseOnSurface,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(12.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FotoPreview() {
    RodandoTheme {
        FotoViniloContenido(estado = FotoUi(MomentoFoto.INICIO), onCapturar = {}, onVolver = {})
    }
}
