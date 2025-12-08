package mx.edu.utng.localmunch.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import mx.edu.utng.localmunch.viewmodel.LugaresViewModel

/**
 * @function MainScreen
 * @brief La pantalla contenedora principal de la aplicación, que implementa el Scaffold con la
 * Barra de Navegación Inferior (Bottom Navigation Bar).
 *
 * * Define el NavHost interno para las pestañas de la barra inferior (Inicio, Categorías, Mapa, Perfil).
 *
 * @param onNavigateToDetalle Callback para navegar a la pantalla de detalle de un lugar.
 * @param onNavigateToContacto Callback para navegar a la pantalla de contacto.
 * @param onNavigateToAdmin Callback para iniciar sesión de administrador.
 * @param onLogout Callback para cerrar la sesión del usuario.
 * @param onCategoriaSelected Callback para navegar al listado filtrado por categoría.
 * // Parámetros adicionales para las opciones del menú de perfil...
 */
@Composable
fun MainScreen(
    onNavigateToDetalle: (String) -> Unit,
    onNavigateToContacto: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToMisFavoritos: () -> Unit = {},
    onNavigateToMisResenas: () -> Unit = {},
    onNavigateToNotificaciones: () -> Unit = {},
    onNavigateToAcercaDe: () -> Unit = {},
    onCategoriaSelected: (String) -> Unit = {}
) {
    // 1. Configuración de Navegación (Interna)
    // NavController para gestionar las transiciones *dentro* de la barra inferior.
    val navController = rememberNavController()
    // Observa la entrada actual de la pila de navegación para saber en qué ruta estamos.
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // 2. ViewModel
    // Obtiene o crea la instancia del ViewModel de Lugares.
    val viewModel: LugaresViewModel = viewModel()

    // 3. Estructura de la UI
    Scaffold(
        // Define la barra de navegación inferior.
        bottomBar = {
            NavigationBar {
                // Item de "Inicio"
                NavigationBarItem(
                    selected = currentRoute == "inicio",
                    onClick = {
                        // Navega a la ruta 'inicio'.
                        navController.navigate("inicio") {
                            // popUpTo: Al navegar a 'inicio', limpia la pila *sobre* 'inicio'.
                            popUpTo("inicio") { inclusive = false }
                        }
                    },
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("Inicio") }
                )

                // Item de "Categorías"
                NavigationBarItem(
                    selected = currentRoute == "categorias",
                    onClick = {
                        navController.navigate("categorias") {
                            // popUpTo: Regresa a la ruta 'inicio' pero guarda el estado (ej. scroll position).
                            popUpTo("inicio") { saveState = true }
                            // launchSingleTop: Si ya estamos en 'categorias', no crea una nueva instancia.
                            launchSingleTop = true
                            // restoreState: Restaura el estado de la pantalla si existía.
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Default.Category, contentDescription = null) },
                    label = { Text("Categorías") }
                )

                // Item de "Mapa"
                NavigationBarItem(
                    selected = currentRoute == "mapa",
                    onClick = {
                        navController.navigate("mapa") {
                            popUpTo("inicio") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Default.Map, contentDescription = null) },
                    label = { Text("Mapa") }
                )

                // Item de "Perfil"
                NavigationBarItem(
                    selected = currentRoute == "perfil",
                    onClick = {
                        navController.navigate("perfil") {
                            popUpTo("inicio") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    label = { Text("Perfil") }
                )
            }
        }
    ) { paddingValues ->
        // 4. NavHost (Contenido de las Pestañas)
        // El NavHost interno gestiona qué pantalla se muestra según la ruta seleccionada en la barra inferior.
        NavHost(
            navController = navController,
            startDestination = "inicio",
            // Aplica el padding de la Scaffold (espacio para la TopBar y BottomBar).
            modifier = Modifier.padding(paddingValues)
        ) {
            // Ruta: "inicio"
            composable("inicio") {
                PantallaInicio(
                    onNavigateToDetalle = onNavigateToDetalle,
                    onNavigateToCategorias = {
                        navController.navigate("categorias") // Navegación interna (dentro del BottomNav).
                    },
                    viewModel = viewModel // Pasa el ViewModel.
                )
            }

            // Ruta: "categorias"
            composable("categorias") {
                PantallaCategorias(
                    // ⭐️ Importante: onCategoriaSelected es un callback de nivel superior
                    // que navega *fuera* del NavHost del BottomNav (definido en NavGraph.kt).
                    onNavigateToCategoria = onCategoriaSelected,
                    viewModel = viewModel
                )
            }

            // Ruta: "mapa"
            composable("mapa") {
                PantallaMapa(
                    onNavigateToDetalle = onNavigateToDetalle,
                    viewModel = viewModel
                )
            }

            // Ruta: "perfil"
            composable("perfil") {
                PantallaPerfilUsuario(
                    userEmail = "usuario@localmunch.com", // Email hardcodeado para ejemplo.
                    onNavigateToAdminLogin = onNavigateToAdmin, // Navegación de nivel superior.
                    onNavigateToContacto = onNavigateToContacto, // Navegación de nivel superior.
                    onLogout = onLogout, // Navegación de nivel superior.
                    // Navegación a las pantallas de menú internas del perfil.
                    onNavigateToMisFavoritos = onNavigateToMisFavoritos,
                    onNavigateToMisResenas = onNavigateToMisResenas,
                    onNavigateToNotificaciones = onNavigateToNotificaciones,
                    onNavigateToAcercaDe = onNavigateToAcercaDe
                )
            }
        }
    }
}
