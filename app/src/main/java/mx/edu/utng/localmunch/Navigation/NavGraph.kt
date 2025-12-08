package mx.edu.utng.localmunch.Navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import mx.edu.utng.localmunch.screens.*
import mx.edu.utng.localmunch.viewmodel.LugaresViewModel
import androidx.compose.runtime.collectAsState

/**
 * @function NavGraph
 * @brief Componente composable central que define el grafo de navegación de la aplicación LocalMunch.
 *
 * * Gestiona la transición entre pantallas, el paso de argumentos y el manejo del estado global (ViewModel).
 *
 * @param navController Controlador principal de navegación.
 * @param startDestination La ruta inicial de la aplicación (ej: 'login' o 'bottom_nav').
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String
) {
    // 🔑 ViewModel Compartido: Se crea una única instancia del ViewModel que se compartirá
    // a través de todas las rutas que lo necesiten para manejar el estado de los lugares.
    val sharedViewModel: LugaresViewModel = viewModel()

    // NavHost: Contenedor donde se alojan todos los destinos navegables (pantallas).
    NavHost(
        navController = navController,
        startDestination = startDestination // Ruta de inicio definida (ej. basada en el estado de autenticación)
    ) {
        // --- SECCIÓN 1: AUTENTICACIÓN ---
        // Maneja las pantallas de Login y Registro.

        composable(Screen.Login.route) {
            PantallaLogin(
                onLoginSuccess = { userEmail ->
                    // Navega a la navegación principal (BottomNav) y elimina la pila de rutas
                    // previas (Login) para que el usuario no pueda regresar con el botón 'atrás'.
                    navController.navigate(Screen.BottomNav.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onGoToRegister = {
                    navController.navigate(Screen.Registro.route)
                }
            )
        }

        composable(Screen.Registro.route) {
            PantallaRegistro(
                onRegistrationSuccess = { userEmail ->
                    // Navega a la navegación principal tras el registro exitoso.
                    navController.navigate(Screen.BottomNav.route) {
                        popUpTo(Screen.Registro.route) { inclusive = true }
                    }
                },
                onGoToLogin = {
                    // Simplemente regresa a la pantalla anterior (Login).
                    navController.popBackStack()
                }
            )
        }

        // --- SECCIÓN 2: PRINCIPAL (USUARIO AUTENTICADO) ---
        // Contiene la barra de navegación inferior (Home, Mapa, Perfil).

        composable(Screen.BottomNav.route) {
            MainScreen(
                // Callback para navegar a la pantalla de detalle, pasando el ID del lugar como argumento.
                onNavigateToDetalle = { lugarId ->
                    navController.navigate(Screen.DetalleLugar.createRoute(lugarId))
                },
                onNavigateToContacto = {
                    navController.navigate(Screen.Contacto.route)
                },
                onNavigateToAdmin = {
                    navController.navigate(Screen.AdminLogin.route)
                },
                onLogout = {
                    // Cierra sesión, navega a Login y borra la pila de navegación principal.
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.BottomNav.route) { inclusive = true }
                    }
                },
                // Navegación a las pantallas del menú de perfil (rutas directas).
                onNavigateToMisFavoritos = { navController.navigate("mis_favoritos") },
                onNavigateToMisResenas = { navController.navigate("mis_resenas") },
                onNavigateToNotificaciones = { navController.navigate("notificaciones") },
                onNavigateToAcercaDe = { navController.navigate("acerca_de") },
                // Navegación para mostrar lugares filtrados por una categoría.
                onCategoriaSelected = { categoria ->
                    navController.navigate("categoria/$categoria")
                }
            )
        }

        // --- SECCIÓN 3: ADMINISTRADOR ---
        // Rutas para la gestión de la aplicación (CRUD).

        composable(Screen.AdminLogin.route) {
            PantallaAdminLogin(
                onLoginSuccess = {
                    // Navega al Dashboard de Admin. No borra BottomNav, solo se pone encima (false).
                    navController.navigate(Screen.AdminDashboard.route) {
                        popUpTo(Screen.BottomNav.route) { inclusive = false }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AdminDashboard.route) {
            PantallaAdminDashboard(
                onNavigateBack = { navController.popBackStack() },
                onLogout = {
                    // Cierra sesión de Admin y vuelve a la pantalla de Login general.
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.BottomNav.route) { inclusive = true }
                    }
                },
                onNavigateToAddEditLugar = { lugarId ->
                    // Navegación condicional: 'add' si lugarId es nulo, 'edit' si tiene ID.
                    if (lugarId != null) {
                        navController.navigate("admin/edit/$lugarId")
                    } else {
                        navController.navigate("admin/add")
                    }
                },
                viewModel = sharedViewModel // Se pasa el ViewModel compartido.
            )
        }

        // Pantalla para añadir un nuevo lugar.
        composable("admin/add") {
            PantallaAddEditLugar(
                lugarId = null, // LugarId nulo indica modo "Añadir".
                onNavigateBack = { navController.popBackStack() },
                viewModel = sharedViewModel
            )
        }

        // Pantalla para editar un lugar existente.
        composable(
            route = "admin/edit/{lugarId}", // La ruta incluye un argumento dinámico.
            arguments = listOf(navArgument("lugarId") { type = NavType.StringType }) // Definición del tipo de argumento.
        ) { backStackEntry ->
            // Extracción del argumento de la URL.
            val lugarId = backStackEntry.arguments?.getString("lugarId")
            PantallaAddEditLugar(
                lugarId = lugarId, // Se pasa el ID para cargar el lugar a editar.
                onNavigateBack = { navController.popBackStack() },
                viewModel = sharedViewModel
            )
        }

        // --- SECCIÓN 4: DETALLES Y RESEÑAS ---
        // Pantallas que requieren argumentos específicos (ID del lugar).

        composable(
            route = "detalle/{lugarId}",
            arguments = listOf(navArgument(name = "lugarId") { type = NavType.StringType })
        ) { backStackEntry ->
            val lugarId = backStackEntry.arguments?.getString("lugarId") ?: ""
            PantallaDetalleLugar(
                lugarId = lugarId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToReseñas = { id ->
                    // Navegación a la lista de reseñas, también requiere el ID.
                    navController.navigate(route = "reseñas/$id")
                },
                viewModel = sharedViewModel // Se pasa el ViewModel para acceder a los datos.
            )
        }

        composable(
            route = "reseñas/{lugarId}",
            arguments = listOf(navArgument("lugarId") { type = NavType.StringType })
        ) { backStackEntry ->
            val lugarId = backStackEntry.arguments?.getString("lugarId") ?: ""
            PantallaReseñas(
                lugarId = lugarId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // --- SECCIÓN 5: EXTRAS (Perfil/Menú Lateral) ---
        // Pantallas secundarias con navegación simple.

        composable("mis_favoritos") {
            PantallaMisFavoritos(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetalle = { lugarId ->
                    navController.navigate("detalle/$lugarId")
                },
                lugaresViewModel = sharedViewModel // Requiere acceso a los datos de lugares.
            )
        }

        composable("mis_resenas") {
            PantallaMisResenas(
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable("notificaciones") {
            PantallaNotificaciones(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("acerca_de") {
            PantallaAcercaDe(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Contacto.route) {
            PantallaContacto(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // --- SECCIÓN 6: FILTRADO POR CATEGORÍA ---
        // Muestra un listado de lugares filtrado dinámicamente.
        composable(
            route = "categoria/{categoria}",
            arguments = listOf(navArgument("categoria") { type = NavType.StringType })
        ) { backStackEntry ->

            val categoria = backStackEntry.arguments?.getString("categoria") ?: ""
            // Obtiene la lista completa de lugares del ViewModel compartido.
            val lugares = sharedViewModel.lugares.collectAsState().value

            // Lógica de filtrado: solo se muestran los lugares cuya categoría coincide
            // y que no están marcados como ocultos.
            val lugaresFiltrados = lugares.filter { lugar ->
                lugar.categoria.equals(categoria, ignoreCase = true) &&
                        !lugar.esOculto
            }

            PantallaListadoCategoria(
                categoria = categoria,
                lista = lugaresFiltrados, // Pasa la lista ya filtrada a la pantalla.
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetalle = { lugarId ->
                    navController.navigate("detalle/$lugarId")
                }
            )
        }

    }
}
