package mx.edu.utng.localmunch.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
// ⬇️ NUEVAS IMPORTACIONES REQUERIDAS
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// ⬇️ IMPORTACIONES DE COIL PARA LA CACHÉ
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.CachePolicy
// 🔑 Importaciones de ViewModel/Datos
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import mx.edu.utng.localmunch.R
import mx.edu.utng.localmunch.viewmodel.LugaresViewModel
// Se elimina la importación duplicada de viewModel

@Composable
        /**
         * @function PantallaInicio
         * @brief Pantalla principal que muestra contenido destacado (LazyRow) y un listado de lugares populares.
         *
         * * Observa y filtra la lista de lugares en tiempo real desde el ViewModel.
         *
         * @param onNavigateToDetalle Callback para navegar a la vista de detalle de un lugar (con ID).
         * @param onNavigateToCategorias Callback para navegar a la pantalla de Categorías.
         * @param viewModel ViewModel compartido para la gestión de datos de lugares.
         */
fun PantallaInicio(
    onNavigateToDetalle: (String) -> Unit,
    onNavigateToCategorias: () -> Unit,
    viewModel: LugaresViewModel = viewModel()
) {
    // Obtenemos el contexto para usarlo en el ImageRequest de Coil (carga de imágenes).
    val context = LocalContext.current

    // Estado del texto ingresado en el campo de búsqueda (actualmente sin TextField visible).
    var searchQuery by remember { mutableStateOf("") }

    // OBSERVAMOS LOS DATOS DE FIRESTORE (Lista completa de lugares)
    val lugares by viewModel.lugares.collectAsState()

    // Lógica para determinar los lugares destacados (los 3 primeros).
    val lugaresDestacados = lugares.take(3)

    // Lógica de Filtrado (Se usa `remember` para recalcular solo cuando cambian las dependencias)
    val lugaresFiltrados = remember(searchQuery, lugares) {
        if (searchQuery.isEmpty()) {
            // Si la búsqueda está vacía, muestra todos los lugares que no están ocultos.
            lugares.filter { !it.esOculto }
        } else {
            // Filtra por nombre o categoría (sin distinción de mayúsculas/minúsculas).
            lugares.filter {
                it.nombre.contains(searchQuery, ignoreCase = true) ||
                        it.categoria.contains(searchQuery, ignoreCase = true)
            }
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            // Permite el desplazamiento vertical de toda la pantalla.
            .verticalScroll(rememberScrollState())
    ) {
        // --- 1. BARRA DE BÚSQUEDA (Espacio reservado) ---
        // (Nota: El TextField de búsqueda no está implementado en este código, solo el estado).

        Spacer(Modifier.height(24.dp))

        // --- 2. RECOMENDACIONES (LazyRow - Desplazamiento Horizontal) ---
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Recomendaciones", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            // Botón para navegar a la lista completa de categorías.
            TextButton(onClick = onNavigateToCategorias) {
                Text("Ver todas")
            }
        }

        Spacer(Modifier.height(8.dp))

        // Manejo del estado de carga inicial.
        if (lugares.isEmpty()) {
            Text("Cargando lugares o añade datos en Firestore...", Modifier.padding(16.dp))
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Itera sobre los lugares destacados. Se usa `key` para optimizar el LazyRow.
                items(lugaresDestacados, key = { it.id.orEmpty() }) { lugar ->
                    Card(
                        modifier = Modifier
                            .width(200.dp)
                            // Al hacer clic, navega a la vista de detalle.
                            .clickable { lugar.id?.let(onNavigateToDetalle) },
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column {
                            // 📸 Carga de Imagen: AsyncImage (Coil)
                            AsyncImage(
                                model = ImageRequest.Builder(context) // Usa el contexto
                                    .data(lugar.imagenUrl)
                                    // Deshabilita la caché de Coil para debugging o recarga frecuente.
                                    .memoryCachePolicy(CachePolicy.DISABLED)
                                    .diskCachePolicy(CachePolicy.DISABLED)
                                    .build(),

                                contentDescription = lugar.nombre,
                                modifier = Modifier.fillMaxWidth().height(120.dp),
                                contentScale = ContentScale.Crop,
                                // Placeholders y errores usan recursos locales.
                                placeholder = painterResource(id = R.drawable.placeholder_comida),
                                error = painterResource(id = R.drawable.placeholder_comida)
                            )
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(lugar.nombre, fontWeight = FontWeight.SemiBold, maxLines = 1)
                                Text(lugar.categoria, fontSize = 12.sp, color = Color.Gray)
                                // Calificación de estrellas.
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                                    Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFFFFC107))
                                    Text(" ${lugar.calificacion}", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        // --- 3. LUGARES POPULARES (Listado Vertical) ---
        Text("Lugares populares", fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Spacer(Modifier.height(16.dp))

        // Se usa Column + forEach para un listado simple dentro del Scroll.
        Column(modifier = Modifier.fillMaxWidth()) {
            lugaresFiltrados.forEach { lugar ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        // Hace que la tarjeta sea clickeable.
                        .clickable { lugar.id?.let(onNavigateToDetalle) },
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 📸 Carga de Imagen (versión miniatura)
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(lugar.imagenUrl)
                                .memoryCachePolicy(CachePolicy.DISABLED)
                                .diskCachePolicy(CachePolicy.DISABLED)
                                .build(),

                            contentDescription = lugar.nombre,
                            modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop,
                            placeholder = painterResource(id = R.drawable.placeholder_comida),
                            error = painterResource(id = R.drawable.placeholder_comida)
                        )
                        Spacer(Modifier.width(12.dp))

                        // Información textual
                        Column(modifier = Modifier.weight(1f)) {
                            Text(lugar.nombre, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                            Text(lugar.categoria, fontSize = 14.sp, color = Color.Gray)
                            Text(lugar.direccion, fontSize = 12.sp, color = Color.Gray)

                            // Calificación y precio promedio.
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = Color(0xFFFFC107)
                                )
                                Text(
                                    " ${lugar.calificacion} • ${lugar.precioPromedio}",
                                    fontSize = 12.sp
                                )
                            }
                        }
                        // Ícono de navegación de detalle.
                        Icon(Icons.Default.ArrowForward, contentDescription = "Ver detalles", tint = Color.Gray)
                    }
                }
            }
        }
        Spacer(Modifier.height(32.dp))
    }
}
