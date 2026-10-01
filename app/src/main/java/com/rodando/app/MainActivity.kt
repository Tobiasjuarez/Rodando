package com.rodando.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.rodando.app.presentation.inicio.InicioScreen
import com.rodando.app.presentation.theme.RodandoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RodandoTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Por ahora la app abre directo en Inicio.
                    // Navigation Compose se suma cuando existan las demás pantallas.
                    InicioScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}
