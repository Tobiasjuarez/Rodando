package com.rodando.app.presentation.navegacion

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.rodando.app.RodandoApplication
import com.rodando.app.presentation.ganancias.GananciasScreen
import com.rodando.app.presentation.historial.HistorialScreen
import com.rodando.app.presentation.historial.HistorialViewModel
import com.rodando.app.presentation.inicio.InicioScreen

/**
 * Raíz de la app: Scaffold con la barra de navegación inferior (Inicio, Historial, Ganancias)
 * y el NavHost con las pantallas.
 */
@Composable
fun RodandoApp(navController: NavHostController = rememberNavController()) {
    val entradaActual by navController.currentBackStackEntryAsState()
    val destinoActual = entradaActual?.destination
    val contenedor = (LocalContext.current.applicationContext as RodandoApplication).container

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                Destino.entries.forEach { destino ->
                    NavigationBarItem(
                        selected = destinoActual?.hierarchy?.any { it.route == destino.ruta } == true,
                        onClick = {
                            navController.navigate(destino.ruta) {
                                // Evita acumular pantallas al tocar varias veces la barra
                                // y conserva el estado de cada pestaña.
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            // La etiqueta ya describe el destino para TalkBack.
                            Icon(painterResource(destino.icono), contentDescription = null)
                        },
                        label = { Text(stringResource(destino.etiqueta)) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Destino.inicial.ruta,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Destino.INICIO.ruta) { InicioScreen() }
            composable(Destino.HISTORIAL.ruta) {
                val viewModel: HistorialViewModel =
                    viewModel(factory = HistorialViewModel.fabrica(contenedor.viajeRepository))
                HistorialScreen(viewModel = viewModel, onAbrirViaje = { /* detalle: próximo commit */ })
            }
            composable(Destino.GANANCIAS.ruta) { GananciasScreen() }
        }
    }
}
