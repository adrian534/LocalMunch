package mx.edu.utng.localmunch.Navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

// --- 1. RUTAS DE PANTALLA COMPLETA (Screen) ---
/**
 * @sealed class Screen
 * @brief Define todas las rutas únicas (paths) de navegación de nivel superior de la aplicación.
 *
 * * Se utiliza una clase sellada para garantizar un conjunto finito de rutas conocidas.
 * @param route El string único usado por el NavHost para identificar la pantalla.
 */
sealed class Screen(val route: String) {

    // RUTAS DE INICIO Y PRINCIPALES
    // Contenedor principal para la navegación de la barra inferior.
    object BottomNav : Screen(route = "bottom_nav")
    object Login : Screen(route = "login")
    object Registro : Screen(route = "registro")
    // Rutas específicas del módulo de administración.
    object AdminLogin : Screen(route = "adminLogin")
    object AdminDashboard : Screen(route = "adminDashboard")
    // Ruta simple para una pantalla que no está en la barra inferior principal.
    object Contacto : Screen(route = "contacto")

    // RUTAS SECUNDARIAS (Requieren Argumentos Dinámicos)

    /**
     * @object DetalleLugar
     * @brief Ruta para ver el detalle de un lugar específico. Requiere el ID del lugar.
     */
    object DetalleLugar : Screen(route = "detalle/{lugarId}") {
        // Función auxiliar para construir la ruta con el argumento dinámico (ej: "detalle/LUGAR_001").
        fun createRoute(lugarId: String) = "detalle/$lugarId"
    }

    /**
     * @object Reseñas
     * @brief Ruta para la lista de reseñas de un lugar. Requiere el ID del lugar.
     */
    object Reseñas : Screen(route = "reseñas/{lugarId}") {
        fun createRoute(lugarId: String) = "reseñas/$lugarId"
    }

    /**
     * @object AddEditLugar
     * @brief Ruta para el CRUD (Crear/Editar) de lugares. Puede recibir un ID o ser 'new'.
     */
    object AddEditLugar : Screen(route = "addEditLugar/{lugarId}") {
        fun createRoute(lugarId: String) = "addEditLugar/$lugarId"
        fun createNewRoute() = "addEditLugar/new" // Ruta simplificada para añadir uno nuevo.
    }
}

// ------------------------------------------------------------------

// --- 2. DEFINICIÓN DE PANTALLAS DE LA BARRA INFERIOR (BottomNavScreen) ---
/**
 * @sealed class BottomNavScreen
 * @brief Clase sellada que define los elementos que aparecen en la barra inferior (Bottom Navigation Bar).
 *
 * * Estos objetos contienen metadatos adicionales como el icono y el título de visualización.
 * @param route La ruta de navegación asociada (que debe mapearse dentro del BottomNav en el NavGraph).
 * @param title El texto que se muestra en el elemento de la barra inferior.
 * @param icon El icono de Compose Material a utilizar.
 */
sealed class BottomNavScreen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Inicio : BottomNavScreen(
        route = "inicio",
        title = "Inicio",
        icon = Icons.Default.Home
    )
    object Categorias : BottomNavScreen(
        route = "categorias",
        title = "Explorar",
        icon = Icons.Default.Search
    )
    object Mapa : BottomNavScreen(
        route = "mapa",
        title = "Mapa",
        icon = Icons.Default.Map
    )
    // Ruta de contacto separada, con icono específico para la barra inferior.
    object Contacto : BottomNavScreen(
        route = "contacto_external",
        title = "Contacto",
        icon = Icons.Default.Email
    )
    object Perfil : BottomNavScreen(
        route = "perfil",
        title = "Perfil",
        icon = Icons.Default.Person
    )
}

// 3. LISTA USADA POR EL BottomNavBar
/**
 * @val bottomNavScreen
 * @brief La lista ordenada de elementos que se utilizan para dibujar la barra de navegación inferior.
 */
val bottomNavScreen = listOf(
    BottomNavScreen.Inicio,
    BottomNavScreen.Categorias,
    BottomNavScreen.Mapa,
    BottomNavScreen.Contacto,
    BottomNavScreen.Perfil
)
