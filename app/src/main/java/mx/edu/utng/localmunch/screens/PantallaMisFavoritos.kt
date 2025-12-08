package mx.edu.utng.localmunch.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import mx.edu.utng.localmunch.viewmodel.LugaresViewModel
import mx.edu.utng.localmunch.viewmodel.FavoritosViewModel

// Anotación requerida para usar la API experimental de Material 3.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
        /**
         * @function PantallaMisFavoritos
         * @brief Muestra una lista de lugares que el usuario ha marcado como favoritos.
         *
         * * Combina datos de lugares (LugaresViewModel) y la lista de IDs de favoritos (FavoritosViewModel).
         *
         * @param onNavigateBack Callback para regresar.
         * @param onNavigateToDetalle Callback para ir a la vista de detalle del lugar.
         * @param lugaresViewModel ViewModel compartido que contiene todos los lugares.
         * @param favoritosViewModel ViewModel que gestiona la lista de IDs de favoritos del usuario.
         */
fun PantallaMisFavoritos(
    onNavigateBack: () -> Unit,
    onNavigateToDetalle: (String) -> Unit,
    lugaresViewModel: LugaresViewModel = viewModel(),
    favoritosViewModel: FavoritosViewModel = viewModel()
) {
    val context = LocalContext.current
    // 1. Observación de Estados
    val lugares by lugaresViewModel.lugares.collectAsState() // Lista completa de lugares.
    val favoritosIds by favoritosViewModel.favoritos.collectAsState() // Lista de IDs de lugares favoritos.

    // 2. Carga Inicial de Favoritos (se ejecuta solo una vez al iniciar la pantalla)
    LaunchedEffect(Unit) {
        favoritosViewModel.cargarFavoritos(context)
    }

    // 3. Filtrado Reactivo de Lugares
    // Filtra la lista completa de lugares, incluyendo solo aquellos cuyo ID está en la lista de favoritos.
    val lugaresFavoritos = lugares.filter { lugar ->
        favoritosIds.contains(lugar.id)
    }

    // 4. Estructura del Scaffold
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Favoritos") },
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
        if (lugaresFavoritos.isEmpty()) {
            // 5. Pantalla Vacía (No hay favoritos)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.FavoriteBorder,
                        contentDescription = "Corazón vacío",
                        modifier = Modifier.size(80.dp),
                        tint = Color.Gray
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "No tienes favoritos aún",
                        fontSize = 18.sp,
                        color = Color.Gray
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Toca el ❤️ en los lugares que te gusten",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
        } else {
            // 6. Lista de Favoritos (LazyColumn)
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Itera sobre la lista filtrada de lugares favoritos.
                items(lugaresFavoritos, key = { it.id ?: "" }) { lugar ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            // Toda la tarjeta es clickeable para ir al detalle.
                            .clickable { lugar.id?.let { onNavigateToDetalle(it) } },
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Imagen (AsyncImage para URLs)
                            AsyncImage(
                                model = lugar.imagenUrl,
                                contentDescription = lugar.nombre,
                                modifier = Modifier
                                    .size(80.dp)
                                    .padding(end = 12.dp)
                            )

                            // Información de Texto
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    lugar.nombre,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    lugar.categoria,
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                                // Calificación
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Star,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = Color(0xFFFFC107)
                                    )
                                    Text(
                                        " ${lugar.calificacion}",
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            // Botón eliminar de favoritos (Ícono de corazón relleno)
                            IconButton(
                                onClick = {
                                    lugar.id?.let {
                                        // Llama a la función para eliminar de favoritos.
                                        favoritosViewModel.eliminarFavorito(context, it)
                                    }
                                }
                            ) {
                                Icon(
                                    Icons.Default.Favorite, // Corazón relleno.
                                    contentDescription = "Eliminar de favoritos",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
