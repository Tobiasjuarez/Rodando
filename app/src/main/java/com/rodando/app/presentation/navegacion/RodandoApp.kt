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
import com.rodando.app.presentation.encurso.ViajeEnCursoScreen
import com.rodando.app.presentation.encurso.ViajeEnCursoViewModel
import com.rodando.app.presentation.foto.FotoViniloScreen
import com.rodando.app.presentation.foto.FotoViniloViewModel
import com.rodando.app.presentation.foto.MomentoFoto
import com.rodando.app.presentation.ganancias.GananciasScreen
import com.rodando.app.presentation.ganancias.GananciasViewModel
import com.rodando.app.presentation.guardado.ViajeGuardadoScreen
import com.rodando.app.presentation.guardado.ViajeGuardadoViewModel
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
            // La barra solo se ve en las tres pestañas; el resto de las pantallas ocupa toda la pantalla.
            val enPestania = destinoActual == null || Destino.entries.any { it.ruta == destinoActual.route }
            if (enPestania) BarraInferior(navController, destinoActual)
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
                InicioScreen(
                    viewModel = viewModel,
                    onIniciarViaje = { navController.navigate(Rutas.FOTO_INICIO) },
                    onVerViajeEnCurso = { navController.navigate(Rutas.VIAJE_EN_CURSO) { launchSingleTop = true } }
                )
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

            // RF01 y RF02: foto inicial -> viaje en curso -> foto final -> viaje guardado.
            composable(Rutas.FOTO_INICIO) {
                val viewModel: FotoViniloViewModel = viewModel(
                    factory = FotoViniloViewModel.fabrica(
                        MomentoFoto.INICIO, null, contenedor.iniciarViaje, contenedor.finalizarViaje
                    )
                )
                FotoViniloScreen(
                    viewModel = viewModel,
                    onListo = {
                        navController.navigate(Rutas.VIAJE_EN_CURSO) {
                            popUpTo(Rutas.FOTO_INICIO) { inclusive = true }
                        }
                    },
                    onVolver = { navController.popBackStack() }
                )
            }
            composable(Rutas.VIAJE_EN_CURSO) {
                val viewModel: ViajeEnCursoViewModel =
                    viewModel(factory = ViajeEnCursoViewModel.fabrica(contenedor.viajeRepository))
                ViajeEnCursoScreen(
                    viewModel = viewModel,
                    onFinalizar = { id -> navController.navigate(Rutas.fotoFin(id)) },
                    onVolver = { navController.popBackStack() }
                )
            }
            composable(
                route = Rutas.FOTO_FIN,
                arguments = listOf(navArgument(Rutas.ARG_VIAJE_ID) { type = NavType.StringType })
            ) { entrada ->
                val viajeId = entrada.arguments?.getString(Rutas.ARG_VIAJE_ID).orEmpty()
                val viewModel: FotoViniloViewModel = viewModel(
                    factory = FotoViniloViewModel.fabrica(
                        MomentoFoto.FIN, viajeId, contenedor.iniciarViaje, contenedor.finalizarViaje
                    )
                )
                FotoViniloScreen(
                    viewModel = viewModel,
                    onListo = { id ->
                        // Al guardar, se sacan de la pila el viaje en curso y las fotos.
                        navController.navigate(Rutas.viajeGuardado(id)) { popUpTo(Destino.INICIO.ruta) }
                    },
                    onVolver = { navController.popBackStack() }
                )
            }
            composable(
                route = Rutas.VIAJE_GUARDADO,
                arguments = listOf(navArgument(Rutas.ARG_VIAJE_ID) { type = NavType.StringType })
            ) { entrada ->
                val viajeId = entrada.arguments?.getString(Rutas.ARG_VIAJE_ID).orEmpty()
                val viewModel: ViajeGuardadoViewModel = viewModel(
                    factory = ViajeGuardadoViewModel.fabrica(
                        contenedor.viajeRepository, contenedor.viniloRepository, viajeId
                    )
                )
                ViajeGuardadoScreen(
                    viewModel = viewModel,
                    onVolverAlInicio = { navController.popBackStack(Destino.INICIO.ruta, inclusive = false) },
                    onVerDetalle = { id ->
                        navController.navigate(Rutas.detalleViaje(id)) { popUpTo(Destino.INICIO.ruta) }
                    }
                )
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
