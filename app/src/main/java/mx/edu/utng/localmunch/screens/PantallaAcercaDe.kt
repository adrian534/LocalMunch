package mx.edu.utng.localmunch.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// Anotación necesaria para usar componentes de la API experimental de Material 3 (como TopAppBar).
@OptIn(ExperimentalMaterial3Api::class)
@Composable
        /**
         * @function PantallaAcercaDe
         * @brief Pantalla estática que muestra información básica sobre la aplicación LocalMunch.
         *
         * @param onNavigateBack Callback que se ejecuta cuando el usuario presiona el botón de retroceso.
         */
fun PantallaAcercaDe(
    onNavigateBack: () -> Unit
) {

    // Scaffold proporciona la estructura básica de la pantalla (TopBar, BottomBar, Contenido).
    Scaffold(
        // Define la barra de aplicación superior.
        topBar = {
            TopAppBar(
                title = { Text("Acerca de LocalMunch") }, // Título de la pantalla.
                navigationIcon = {
                    // Icono de navegación (generalmente para ir hacia atrás).
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack, // Icono de flecha hacia atrás.
                            contentDescription = "Volver atrás" // Descripción de accesibilidad.
                        )
                    }
                },
                // Personaliza los colores de la TopAppBar para que coincidan con el tema principal.
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary, // Fondo principal
                    titleContentColor = MaterialTheme.colorScheme.onPrimary // Color del texto (blanco/claro)
                )
            )
        }
    ) { padding ->
        // Contenido principal de la pantalla, anclado debajo de la TopAppBar gracias al padding.
        Column(
            modifier = Modifier
                .padding(padding) // Aplica el padding de la Scaffold para evitar superposición con la TopBar.
                .padding(16.dp) // Añade padding interno a todo el contenido.
        ) {
            // Muestra la versión de la aplicación.
            Text(
                "LocalMunch v1.0",
                style = MaterialTheme.typography.titleLarge // Estilo para un título prominente.
            )
            // Espacio vertical entre los elementos.
            Spacer(modifier = Modifier.height(8.dp))
            // Descripción de la aplicación.
            Text(
                "Aplicación desarrollada para explorar y descubrir los mejores lugares de comida.",
                style = MaterialTheme.typography.bodyMedium // Estilo de cuerpo de texto.
            )
        }
    }
}
