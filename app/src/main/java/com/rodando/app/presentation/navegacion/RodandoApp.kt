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
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.rodando.app.RodandoApplication
import com.rodando.app.presentation.detalle.DetalleViajeScreen
import com.rodando.app.presentation.detalle.DetalleViajeViewModel
import com.rodando.app.presentation.ganancias.GananciasScreen
import com.rodando.app.presentation.ganancias.GananciasViewModel
import com.rodando.app.presentation.historial.HistorialScreen
import com.rodando.app.presentation.historial.HistorialViewModel
import com.rodando.app.presentation.inicio.InicioScreen
import com.rodando.app.presentation.inicio.InicioViewModel

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
            // El detalle de un viaje ocupa toda la pantalla, sin barra inferior.
            if (destinoActual?.route != Rutas.DETALLE_VIAJE) BarraInferior(navController, destinoActual)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Destino.inicial.ruta,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Destino.INICIO.ruta) {
                val viewModel: InicioViewModel = viewModel(
                    factory = InicioViewModel.fabrica(
                        contenedor.campaniaRepository,
                        contenedor.viniloRepository,
                        contenedor.viajeRepository
                    )
                )
                // El botón "Iniciar viaje" se habilita cuando exista el flujo de cámara de RF01.
                InicioScreen(viewModel = viewModel, onIniciarViaje = null)
            }
            composable(Destino.HISTORIAL.ruta) {
                val viewModel: HistorialViewModel =
                    viewModel(factory = HistorialViewModel.fabrica(contenedor.viajeRepository))
                HistorialScreen(
                    viewModel = viewModel,
                    onAbrirViaje = { id -> navController.navigate(Rutas.detalleViaje(id)) }
                )
            }
            composable(Destino.GANANCIAS.ruta) {
                val viewModel: GananciasViewModel = viewModel(
                    factory = GananciasViewModel.fabrica(contenedor.viajeRepository, contenedor.viniloRepository)
                )
                GananciasScreen(
                    viewModel = viewModel,
                    onVerViajes = { irAPestania(navController, Destino.HISTORIAL) }
                )
            }
            composable(
                route = Rutas.DETALLE_VIAJE,
                arguments = listOf(navArgument(Rutas.ARG_VIAJE_ID) { type = NavType.StringType })
            ) { entrada ->
                val viajeId = entrada.arguments?.getString(Rutas.ARG_VIAJE_ID).orEmpty()
                val viewModel: DetalleViajeViewModel =
                    viewModel(factory = DetalleViajeViewModel.fabrica(contenedor.viajeRepository, viajeId))
                DetalleViajeScreen(viewModel = viewModel, onVolver = { navController.popBackStack() })
            }
        }
    }
}

@Composable
private fun BarraInferior(navController: NavHostController, destinoActual: NavDestination?) {
    NavigationBar {
        Destino.entries.forEach { destino ->
            NavigationBarItem(
                selected = destinoActual?.hierarchy?.any { it.route == destino.ruta } == true,
                onClick = { irAPestania(navController, destino) },
                icon = {
                    // La etiqueta ya describe el destino para TalkBack.
                    Icon(painterResource(destino.icono), contentDescription = null)
                },
                label = { Text(stringResource(destino.etiqueta)) }
            )
        }
    }
}

/** Cambia de pestaña sin acumular pantallas y conservando el estado de cada una. */
private fun irAPestania(navController: NavHostController, destino: Destino) {
    navController.navigate(destino.ruta) {
        popUpTo(navController.graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}
