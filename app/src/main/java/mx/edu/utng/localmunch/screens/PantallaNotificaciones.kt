package mx.edu.utng.localmunch.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.lifecycle.viewmodel.compose.viewModel
// Importa tu futuro ViewModel de Notificaciones aquí
// import mx.edu.utng.localmunch.viewmodel.NotificacionesViewModel

// Usamos el NotificacionesViewModel por defecto para simular la inyección
// NOTA: Si no tienes NotificacionesViewModel, la aplicación fallará.
// Para que compile, debes crear un NotificacionesViewModel básico o usar el Listado estático temporalmente.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
        /**
         * @function PantallaNotificaciones
         * @brief Muestra un listado de notificaciones para el usuario.
         *
         * * Actualmente usa una lista estática de ejemplo.
         *
         * @param onNavigateBack Callback para regresar a la pantalla anterior.
         * // @param viewModel ViewModel para gestionar el estado de las notificaciones (futuro).
         */
fun PantallaNotificaciones(
    onNavigateBack: () -> Unit
    // viewModel: NotificacionesViewModel = viewModel() // ⭐️ Descomentar cuando crees el VM
) {
    // ⭐️ Lista de notificaciones de ejemplo (Temporal)
    val notificaciones = remember {
        listOf(
            NotificacionItem(
                icon = Icons.Default.Favorite,
                titulo = "Nuevas reseñas",
                descripcion = "Tus lugares favoritos tienen 3 nuevas reseñas",
                tiempo = "Hace 2 horas"
            ),
            NotificacionItem(
                icon = Icons.Default.Place,
                titulo = "Lugares cercanos",
                descripcion = "Se agregaron 2 nuevos restaurantes en tu zona",
                tiempo = "Hace 5 horas"
            ),
            NotificacionItem(
                icon = Icons.Default.Info,
                titulo = "Actualización",
                descripcion = "LocalMunch v1.0 ya está disponible",
                tiempo = "Ayer"
            ),
            NotificacionItem(
                icon = Icons.Default.Star,
                titulo = "¡Deja tu opinión!",
                descripcion = "¿Qué te pareció tu última visita? Comparte tu experiencia",
                tiempo = "Hace 2 días"
            )
        )
    }
    // ⚠️ Si el ViewModel ya existe, reemplaza la línea anterior por:
    // val notificaciones by viewModel.notificaciones.collectAsState()


    // 1. Estructura del Scaffold
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notificaciones") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        if (notificaciones.isEmpty()) {
            // 2. Pantalla de Estado Vacío
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.NotificationsNone,
                        contentDescription = "Sin notificaciones",
                        modifier = Modifier.size(80.dp),
                        tint = Color.Gray
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "No tienes notificaciones",
                        fontSize = 18.sp,
                        color = Color.Gray
                    )
                }
            }
        } else {
            // 3. Lista de Notificaciones (LazyColumn)
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                // Itera sobre la lista de notificaciones estáticas.
                items(notificaciones) { notif ->
                    NotificacionCard(notif)
                    // Añade un divisor entre cada notificación.
                    Divider()
                }
            }
        }
    }
}

/**
 * @data class NotificacionItem
 * @brief Modelo de datos para una notificación en la UI.
 */
data class NotificacionItem(
    val icon: androidx.compose.ui.graphics.vector.ImageVector, // Ícono de la notificación.
    val titulo: String, // Título principal.
    val descripcion: String, // Contenido de la notificación.
    val tiempo: String // Tiempo transcurrido (ej: "Hace 2 horas").
)

// Card individual de notificación
@Composable
        /**
         * @function NotificacionCard
         * @brief Componente reutilizable que muestra la información de una notificación individual.
         *
         * @param notificacion El objeto NotificacionItem a renderizar.
         */
fun NotificacionCard(notificacion: NotificacionItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Ícono (dentro de un Surface circular para darle estilo)
        Surface(
            modifier = Modifier.size(48.dp),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    notificacion.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        // Contenido de la Notificación
        Column(modifier = Modifier.weight(1f)) {
            Text(
                notificacion.titulo,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                notificacion.descripcion,
                fontSize = 14.sp,
                color = Color.Gray,
                lineHeight = 20.sp
            )
            Spacer(Modifier.height(6.dp))
            // Tiempo de la notificación.
            Text(
                notificacion.tiempo,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}
