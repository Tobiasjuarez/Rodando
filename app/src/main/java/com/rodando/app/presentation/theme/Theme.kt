package com.rodando.app.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val EsquemaClaro = lightColorScheme(
    primary = VerdePrimario,
    onPrimary = SobrePrimario,
    primaryContainer = ContenedorPrimario,
    onPrimaryContainer = SobreContenedorPrimario,
    secondaryContainer = ContenedorSecundario,
    onSecondaryContainer = SobreContenedorSecundario,
    tertiaryContainer = ContenedorTerciario,
    onTertiaryContainer = SobreContenedorTerciario,
    background = Superficie,
    onBackground = SobreSuperficie,
    surface = Superficie,
    onSurface = SobreSuperficie,
    onSurfaceVariant = SobreSuperficieVariante,
    surfaceContainer = ContenedorSuperficie,
    surfaceContainerHigh = ContenedorSuperficieAlto,
    surfaceContainerLowest = ContenedorSuperficieMinimo,
    outline = Contorno,
    outlineVariant = ContornoVariante,
    error = ErrorClaro,
    errorContainer = ContenedorError,
    onErrorContainer = SobreContenedorError
)

private val EsquemaOscuro = darkColorScheme(
    primary = VerdePrimarioOscuro,
    onPrimary = SobrePrimarioOscuro,
    primaryContainer = ContenedorPrimarioOscuro,
    onPrimaryContainer = SobreContenedorPrimarioOscuro,
    secondaryContainer = ContenedorSecundarioOscuro,
    onSecondaryContainer = SobreContenedorSecundarioOscuro,
    tertiaryContainer = ContenedorTerciarioOscuro,
    onTertiaryContainer = SobreContenedorTerciarioOscuro,
    background = SuperficieOscura,
    onBackground = SobreSuperficieOscura,
    surface = SuperficieOscura,
    onSurface = SobreSuperficieOscura,
    onSurfaceVariant = SobreSuperficieVarianteOscura,
    surfaceContainer = ContenedorSuperficieOscuro,
    surfaceContainerHigh = ContenedorSuperficieAltoOscuro,
    surfaceContainerLowest = ContenedorSuperficieMinimoOscuro,
    outline = ContornoOscuro,
    outlineVariant = ContornoVarianteOscuro,
    error = ErrorOscuro,
    errorContainer = ContenedorErrorOscuro,
    onErrorContainer = SobreContenedorErrorOscuro
)

/**
 * Tema de Rodando. Usa siempre la paleta de la marca (sin colores dinámicos del sistema)
 * para que la app se vea igual en todos los teléfonos y mantenga el contraste AA (RNF06).
 */
@Composable
fun RodandoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) EsquemaOscuro else EsquemaClaro,
        typography = Typography,
        content = content
    )
}
