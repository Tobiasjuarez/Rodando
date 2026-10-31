package com.rodando.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.rodando.app.presentation.navegacion.RodandoApp
import com.rodando.app.presentation.theme.RodandoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RodandoTheme {
                RodandoApp()
            }
        }
    }
}
