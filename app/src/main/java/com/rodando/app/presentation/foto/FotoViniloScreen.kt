package com.rodando.app.presentation.foto

import android.Manifest
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rodando.app.R
import com.rodando.app.domain.usecase.ResultadoFoto
import com.rodando.app.presentation.common.rememberFoto
import com.rodando.app.presentation.theme.RodandoTheme
import java.io.File
import com.rodando.app.presentation.common.encabezado

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
        // Modo prueba, solo en las versiones de desarrollo: usa el QR de ejemplo en vez de la cámara.
        onSacarPrueba = if (esVersionDeDesarrollo(contexto)) {
            {
                val ruta = viewModel.empezarCaptura()
                if (copiarQrDePrueba(contexto, ruta)) viewModel.fotoGuardada(ruta) else viewModel.errorDeCamara(ruta)
            }
        } else {
            null
        },
        onRepetir = viewModel::repetir,
        onUsar = viewModel::usarFoto,
        onSeguirEnRevision = viewModel::seguirEnRevision,
        onPedirPermiso = { pedirPermiso.launch(Manifest.permission.CAMERA) },
        onVolver = onVolver,
        modifier = modifier
    )
}

private fun tienePermisoDeCamara(contexto: Context) =
    ContextCompat.checkSelfPermission(contexto, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

private fun esVersionDeDesarrollo(contexto: Context) =
    (contexto.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0

/**
 * Guarda en [ruta], como JPEG, el QR de ejemplo que trae la versión de desarrollo
 * (app/src/debug/assets). En la versión final el archivo no existe y devuelve false.
 */
private fun copiarQrDePrueba(contexto: Context, ruta: String): Boolean = runCatching {
    val imagen = contexto.assets.open("qr-vinilo-demo.png").use { BitmapFactory.decodeStream(it) }
    File(ruta).outputStream().use { imagen.compress(Bitmap.CompressFormat.JPEG, 90, it) }
}.getOrDefault(false)

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
    onSeguirEnRevision: () -> Unit,
    onPedirPermiso: () -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier,
    /** Mantener apretado "Sacar foto" usa el QR de ejemplo (solo en desarrollo); null en la versión final. */
    onSacarPrueba: (() -> Unit)? = null
) {
    val esInicio = estado.momento == MomentoFoto.INICIO
    val ocupado = estado.sacando || estado.guardando || estado.verificando
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
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.encabezado()
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
            if (estado.fotoTomada != null) {
                AvisoVerificacion(estado.verificando, estado.resultado, esInicio)
            } else {
                Text(
                    text = stringResource(R.string.foto_consejo),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            estado.error?.let {
                Text(text = it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
            }
        }

        val botones = Modifier.padding(24.dp)
        val resultado = estado.resultado
        if (resultado is ResultadoFoto.Reintentar && !ocupado) {
            Button(
                onClick = onRepetir,
                modifier = botones
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
            ) {
                Text(stringResource(R.string.foto_otra))
            }
        } else if (resultado == ResultadoFoto.OfrecerRevision) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = botones) {
                OutlinedButton(
                    onClick = onVolver,
                    enabled = !estado.guardando,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 56.dp)
                ) {
                    Text(stringResource(R.string.foto_cancelar))
                }
                Button(
                    onClick = onSeguirEnRevision,
                    enabled = !estado.guardando,
                    modifier = Modifier
                        .weight(2f)
                        .heightIn(min = 56.dp)
                ) {
                    if (estado.guardando) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        Text(stringResource(if (esInicio) R.string.foto_revision_inicio else R.string.foto_revision_fin))
                    }
                }
            }
        } else if (estado.fotoTomada == null) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                BotonSacar(
                    enabled = permisoCamara && !ocupado,
                    sacando = estado.sacando,
                    onClick = onSacar,
                    onLongClick = onSacarPrueba
                )
                if (onSacarPrueba != null) {
                    Text(
                        text = stringResource(R.string.foto_modo_prueba),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
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
                        .heightIn(min = 56.dp)
                ) {
                    Text(stringResource(R.string.foto_repetir))
                }
                Button(
                    onClick = onUsar,
                    enabled = !ocupado && resultado == ResultadoFoto.Aprobada,
                    modifier = Modifier
                        .weight(2f)
                        .heightIn(min = 56.dp)
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

/**
 * "Sacar foto" con la forma de un Button de Material, pero que además acepta mantenerlo
 * apretado (Button no lo permite): así se activa el modo prueba en desarrollo.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BotonSacar(enabled: Boolean, sacando: Boolean, onClick: () -> Unit, onLongClick: (() -> Unit)?) {
    val colores = MaterialTheme.colorScheme
    Surface(
        color = if (enabled) colores.primary else colores.onSurface.copy(alpha = 0.12f),
        contentColor = if (enabled) colores.onPrimary else colores.onSurface.copy(alpha = 0.38f),
        shape = CircleShape,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clip(CircleShape)
            .combinedClickable(
                enabled = enabled,
                onClickLabel = stringResource(R.string.foto_sacar),
                onLongClickLabel = onLongClick?.let { stringResource(R.string.foto_modo_prueba_accion) },
                onClick = onClick,
                onLongClick = onLongClick
            )
            .semantics { role = Role.Button }
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (sacando) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = colores.onPrimary)
            } else {
                Text(stringResource(R.string.foto_sacar), style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

/** Qué encontró ML Kit en la foto, siempre con texto y no solo con color (RNF06). */
@Composable
private fun AvisoVerificacion(verificando: Boolean, resultado: ResultadoFoto?, esInicio: Boolean) {
    val colores = MaterialTheme.colorScheme
    val (fondo, texto) = when {
        verificando || resultado == null -> colores.surfaceContainer to colores.onSurface
        resultado == ResultadoFoto.Aprobada -> colores.secondaryContainer to colores.onSecondaryContainer
        else -> colores.errorContainer to colores.onErrorContainer
    }
    val mensaje = when {
        verificando || resultado == null -> stringResource(R.string.foto_verificando)
        resultado == ResultadoFoto.Aprobada -> stringResource(R.string.foto_verificada)
        resultado is ResultadoFoto.Reintentar -> pluralStringResource(
            R.plurals.foto_reintentar, resultado.intentosRestantes, resultado.intentosRestantes
        )
        esInicio -> stringResource(R.string.foto_revision_mensaje_inicio)
        else -> stringResource(R.string.foto_revision_mensaje_fin)
    }
    Surface(color = fondo, contentColor = texto, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            if (verificando) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            Text(text = mensaje, style = MaterialTheme.typography.bodyMedium)
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
            onSeguirEnRevision = {},
            onPedirPermiso = {},
            onVolver = {}
        )
    }
}
