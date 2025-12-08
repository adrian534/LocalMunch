package mx.edu.utng.localmunch.Navigation


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * @sealed class BottomNavItem
 * @brief Clase sellada (Sealed Class) que define las posibles rutas de navegación de la barra inferior.
 *
 * * Proporciona un conjunto fijo de opciones, asegurando que solo se puedan crear las instancias
 * definidas aquí (Home, Mapa, Perfil), lo que es ideal para la navegación.
 *
 * @param route La ruta (String) usada por el NavController para navegar.
 * @param icon El icono (ImageVector) a mostrar en la barra inferior.
 * @param title El texto a mostrar debajo del icono.
 */
sealed class BottomNavItem(val route: String, val icon: ImageVector, val title: String) {
    // 1. Elemento para la pantalla principal de exploración de lugares.
    object Home : BottomNavItem("home", Icons.Default.Home, "Inicio")
    // 2. Elemento para la pantalla del mapa, mostrando la ubicación de los lugares.
    object Mapa : BottomNavItem("mapa", Icons.Default.Map, "Mapa")
    // 3. Elemento para la pantalla del perfil de usuario y sus reseñas.
    object Perfil : BottomNavItem("perfil", Icons.Default.Person, "Perfil")
}

// ⚠️ CAMBIO CLAVE: No se especifica el tipo List<BottomNavItem> explícitamente,
// lo cual es una práctica segura para evitar ambigüedades de referencia.

/**
 * @val bottomNavItems
 * @brief Lista que contiene todos los elementos que deben ser renderizados en la barra de navegación inferior.
 *
 * * Esta lista se itera en el componente Composable de la Bottom Navigation Bar.
 */
val bottomNavItems = listOf(
    BottomNavItem.Home,
    BottomNavItem.Mapa,
    BottomNavItem.Perfil
)
