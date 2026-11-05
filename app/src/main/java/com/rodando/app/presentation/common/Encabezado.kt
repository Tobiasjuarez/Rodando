package com.rodando.app.presentation.common

import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics

/**
 * Marca un texto como título de la pantalla. TalkBack lo anuncia como encabezado y
 * permite saltar entre encabezados, así se recorre la pantalla más rápido (RNF06).
 */
fun Modifier.encabezado(): Modifier = semantics { heading() }
