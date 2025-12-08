package mx.edu.utng.localmunch.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Anotación requerida para usar la API experimental de Material 3.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
        /**
         * @function PantallaPerfilUsuario
         * @brief Pantalla principal del perfil de usuario, actuando como el menú central de la aplicación.
         *
         * * Muestra la información básica del usuario y proporciona accesos a la configuración y funciones clave.
         *
         * @param userEmail El correo electrónico del usuario actual.
         * @param onNavigateToAdminLogin Callback para ir al login del administrador.
         * @param onNavigateToContacto Callback para ir a la pantalla de contacto.
         * @param onLogout Callback para cerrar la sesión.
         * @param onNavigateToMisFavoritos Callback para ir a la lista de favoritos.
         * @param onNavigateToMisResenas Callback para ir a la lista de reseñas del usuario.
         * @param onNavigateToNotificaciones Callback para ir a la lista de notificaciones.
         * @param onNavigateToAcercaDe Callback para ir a la pantalla "Acerca de".
         */
fun PantallaPerfilUsuario(
    userEmail: String,
    onNavigateToAdminLogin: () -> Unit,
    onNavigateToContacto: () -> Unit,
    onLogout: () -> Unit = {},
    // ✅ PARÁMETROS DE NAVEGACIÓN
    onNavigateToMisFavoritos: () -> Unit = {},
    onNavigateToMisResenas: () -> Unit = {},
    onNavigateToNotificaciones: () -> Unit = {},
    onNavigateToAcercaDe: () -> Unit = {}
) {
    // 1. Estructura del Scaffold (sin TopBar para dejar espacio completo al contenido)
    Scaffold(

    ) { paddingValues ->
        // 2. Contenido Principal del Perfil
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                // Permite el desplazamiento vertical.
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar y Nombre/Email del usuario.
            Icon(
                Icons.Default.AccountCircle,
                contentDescription = "Avatar",
                modifier = Modifier.size(120.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(16.dp))

            Text(
                userEmail,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                "Usuario de LocalMunch",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(Modifier.height(32.dp))
            Divider()
            Spacer(Modifier.height(16.dp))

            // 3. SECCIÓN: CONFIGURACIÓN / ACTIVIDAD
            Text(
                "Configuración",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            // Opción: Mis Favoritos
            MenuOption(
                icon = Icons.Default.Favorite,
                title = "Mis Favoritos",
                description = "Lugares que te gustan",
                onClick = onNavigateToMisFavoritos // Callback de navegación.
            )

            // ⭐️ Opción: Mis Reseñas
            MenuOption(
                icon = Icons.Default.Star,
                title = "Mis Reseñas",
                description = "Reseñas que has escrito",
                onClick = onNavigateToMisResenas // Callback de navegación.
            )

            // ⭐️ Opción: Notificaciones
            MenuOption(
                icon = Icons.Default.Notifications,
                title = "Notificaciones",
                description = "Gestiona tus notificaciones",
                onClick = onNavigateToNotificaciones // Callback de navegación.
            )

            Spacer(Modifier.height(16.dp))
            Divider()
            Spacer(Modifier.height(16.dp))

            // 4. SECCIÓN: SOPORTE Y LEGAL
            Text(
                "Soporte y Legal",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            MenuOption(
                icon = Icons.Default.Email,
                title = "Contáctanos",
                description = "Envíanos tus sugerencias",
                onClick = onNavigateToContacto // Callback de navegación.
            )

            MenuOption(
                icon = Icons.Default.Info,
                title = "Acerca de LocalMunch",
                description = "Versión 1.0.0",
                onClick = onNavigateToAcercaDe // Callback de navegación.
            )

            Spacer(Modifier.height(16.dp))
            Divider()
            Spacer(Modifier.height(16.dp))

            // Botón de Acceso Administrador (OutlinedButton)
            OutlinedButton(
                onClick = onNavigateToAdminLogin, // Callback para iniciar sesión de administrador.
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(Icons.Default.AdminPanelSettings, contentDescription = "Panel Administrador")
                Spacer(Modifier.width(8.dp))
                Text("Acceso Administrador")
            }

            Spacer(Modifier.height(16.dp))

            // Botón de Cerrar Sesión (Destacado en color de error)
            Button(
                onClick = onLogout, // Callback para cerrar sesión.
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(Icons.Default.Logout, contentDescription = "Cerrar sesión")
                Spacer(Modifier.width(8.dp))
                Text("Cerrar Sesión")
            }

            Spacer(Modifier.height(32.dp))

            // Footer
            Text(
                "LocalMunch © 2025",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
/**
 * @function MenuOption
 * @brief Componente reutilizable para un elemento de menú navegable con icono, título y descripción.
 *
 * @param icon El icono a mostrar.
 * @param title El título principal.
 * @param description La descripción secundaria.
 * @param onClick La acción al hacer clic (navegación).
 */
private fun MenuOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onClick, // El clic ejecuta el callback.
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary // Icono coloreado con el primario.
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    description,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            // Flecha de navegación a la derecha.
            Icon(
                Icons.Default.ArrowForward,
                contentDescription = "Ir",
                tint = Color.Gray
            )
        }
    }
}
