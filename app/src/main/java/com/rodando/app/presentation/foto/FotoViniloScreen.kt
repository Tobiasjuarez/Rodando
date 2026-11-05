package com.rodando.app.presentation.foto

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rodando.app.R
import com.rodando.app.presentation.common.rememberFoto
import com.rodando.app.presentation.theme.RodandoTheme
import java.io.File

@Composable
fun FotoViniloScreen(
    viewModel: FotoViniloViewModel,
    onListo: (viajeId: String) -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    LaunchedEffect(estado.viajeListo) { estado.viajeListo?.let(onListo) }

    val contexto = LocalContext.current
    var permisoCamara by remember { mutableStateOf(tienePermisoDeCamara(contexto)) }
    val pedirPermiso = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        permisoCamara = it
    }
    LaunchedEffect(Unit) { if (!permisoCamara) pedirPermiso.launch(Manifest.permission.CAMERA) }

    val controlador = remember {
        LifecycleCameraController(contexto).apply {
            setEnabledUseCases(CameraController.IMAGE_CAPTURE)
            cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        }
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, permisoCamara) {
        if (permisoCamara) controlador.bindToLifecycle(lifecycleOwner)
        onDispose { controlador.unbind() }
    }

    FotoViniloContenido(
        estado = estado,
        permisoCamara = permisoCamara,
        camara = { mod ->
            AndroidView(
                factory = {
                    PreviewView(it).apply {
                        // COMPATIBLE respeta las esquinas redondeadas del visor.
                        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                        controller = controlador
                    }
                },
                modifier = mod
            )
        },
        onSacar = {
            val ruta = viewModel.empezarCaptura()
            sacarFoto(controlador, contexto, ruta, viewModel::fotoGuardada, viewModel::errorDeCamara)
        },
        onRepetir = viewModel::repetir,
        onUsar = viewModel::usarFoto,
        onPedirPermiso = { pedirPermiso.launch(Manifest.permission.CAMERA) },
        onVolver = onVolver,
        modifier = modifier
    )
}

private fun tienePermisoDeCamara(contexto: Context) =
    ContextCompat.checkSelfPermission(contexto, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

/** Saca la foto con CameraX y la guarda en [ruta]. Los callbacks llegan en el hilo principal. */
private fun sacarFoto(
    controlador: LifecycleCameraController,
    contexto: Context,
    ruta: String,
    alGuardar: (String) -> Unit,
    alFallar: (String) -> Unit
) {
    val salida = ImageCapture.OutputFileOptions.Builder(File(ruta)).build()
    controlador.takePicture(
        salida,
        ContextCompat.getMainExecutor(contexto),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(resultado: ImageCapture.OutputFileResults) = alGuardar(ruta)
            override fun onError(error: ImageCaptureException) = alFallar(ruta)
        }
    )
}

/**
 * Versión sin ViewModel ni cámara: la vista de la cámara llega por [camara],
 * así la vista previa de Android Studio se puede dibujar sin CameraX.
 */
@Composable
fun FotoViniloContenido(
    estado: FotoUi,
    permisoCamara: Boolean,
    camara: @Composable (Modifier) -> Unit,
    onSacar: () -> Unit,
    onRepetir: () -> Unit,
    onUsar: () -> Unit,
    onPedirPermiso: () -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    val esInicio = estado.momento == MomentoFoto.INICIO
    val ocupado = estado.sacando || estado.guardando
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
            val visor = Modifier
                .weight(1f)
                .fillMaxWidth()
            when {
                estado.fotoTomada != null -> FotoTomada(estado.fotoTomada, visor)
                permisoCamara -> Visor(camara, visor)
                else -> SinPermiso(onPedirPermiso, visor)
            }
            Text(
                text = stringResource(
                    if (estado.fotoTomada != null) R.string.foto_revisar else R.string.foto_consejo
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            estado.error?.let {
                Text(text = it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
            }
        }

        if (estado.fotoTomada == null) {
            Button(
                onClick = onSacar,
                enabled = permisoCamara && !ocupado,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .height(56.dp)
            ) {
                if (estado.sacando) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Text(stringResource(R.string.foto_sacar))
                }
            }
        } else {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(24.dp)
            ) {
                OutlinedButton(
                    onClick = onRepetir,
                    enabled = !ocupado,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                ) {
                    Text(stringResource(R.string.foto_repetir))
                }
                Button(
                    onClick = onUsar,
                    enabled = !ocupado,
                    modifier = Modifier
                        .weight(2f)
                        .height(56.dp)
                ) {
                    if (estado.guardando) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        Text(stringResource(if (esInicio) R.string.foto_usar_inicio else R.string.foto_usar_fin))
                    }
                }
            }
        }
    }
}

/** Vista de la cámara con el recuadro donde tiene que entrar el vinilo. */
@Composable
private fun Visor(camara: @Composable (Modifier) -> Unit, modifier: Modifier = Modifier) {
    val descripcion = stringResource(R.string.foto_visor)
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.inverseSurface)
            .semantics { contentDescription = descripcion },
        contentAlignment = Alignment.Center
    ) {
        if (!LocalInspectionMode.current) camara(Modifier.fillMaxSize())
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(200.dp)
                .border(2.dp, MaterialTheme.colorScheme.inverseOnSurface, RoundedCornerShape(12.dp))
        )
        Text(
            text = stringResource(R.string.foto_encuadre),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.inverseOnSurface,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(12.dp)
                .background(MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun FotoTomada(ruta: String, modifier: Modifier = Modifier) {
    val foto = rememberFoto(ruta)
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.inverseSurface),
        contentAlignment = Alignment.Center
    ) {
        if (foto != null) {
            Image(
                bitmap = foto,
                contentDescription = stringResource(R.string.foto_tomada),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun SinPermiso(onPedirPermiso: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_camara),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(40.dp)
        )
        Text(
            text = stringResource(R.string.foto_sin_permiso_titulo),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(R.string.foto_sin_permiso_mensaje),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        OutlinedButton(onClick = onPedirPermiso) { Text(stringResource(R.string.foto_dar_permiso)) }
    }
}

@Preview(showBackground = true)
@Composable
private fun FotoPreview() {
    RodandoTheme {
        FotoViniloContenido(
            estado = FotoUi(MomentoFoto.INICIO),
            permisoCamara = true,
            camara = {},
            onSacar = {},
            onRepetir = {},
            onUsar = {},
            onPedirPermiso = {},
            onVolver = {}
        )
    }
}
