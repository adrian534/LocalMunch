package mx.edu.utng.localmunch.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.collectAsState
import mx.edu.utng.localmunch.viewmodel.LugaresViewModel

// Anotación requerida para usar la API experimental de Material 3.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
        /**
         * @function PantallaCategorias
         * @brief Muestra una lista dinámica de categorías únicas extraídas de los lugares disponibles.
         *
         * * Permite al usuario seleccionar una categoría para ver los lugares filtrados.
         *
         * @param onNavigateToCategoria Callback que se ejecuta al hacer clic en una categoría, pasando el nombre.
         * @param viewModel ViewModel compartido para acceder a la lista de lugares.
         */
fun PantallaCategorias(
    onNavigateToCategoria: (String) -> Unit = {},
    viewModel: LugaresViewModel = viewModel()
) {
    // 1. Datos y Estados
    // Observa la lista de lugares en tiempo real desde el ViewModel.
    val lugares by viewModel.lugares.collectAsState()
    // Observa el estado de carga para mostrar un indicador.
    val isLoading by viewModel.isLoading.collectAsState()

    // 2. Lógica de Extracción de Categorías Únicas
    val categoriasUnicas = remember(lugares) {
        // Se recalcula la lista solo si 'lugares' cambia.
        lugares
            .filter { !it.esOculto } // 1. Filtra los lugares que no están ocultos.
            .map { it.categoria } // 2. Mapea la lista solo a los nombres de las categorías.
            .distinct() // 3. Obtiene solo las categorías únicas.
            .sorted() // 4. Las ordena alfabéticamente.
    }

    // 3. Estructura de la UI
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Explorar Categorías") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Manejo de Estados de Carga/Vacío
            if (lugares.isEmpty() && isLoading) {
                // Muestra un indicador de carga mientras se obtienen los datos.
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (categoriasUnicas.isEmpty()) {
                // Muestra mensaje si no se encontró ninguna categoría.
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay categorías disponibles.")
                }
            } else {
                // Lista de Categorías
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp) // Espacio entre ítems
                ) {
                    // Título de la sección
                    item {
                        Text(
                            "Descubre nuevos sabores cerca de ti:",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp
                        )
                        Divider(Modifier.padding(top = 8.dp, bottom = 10.dp))
                    }

                    // Itera sobre las categorías únicas y crea un elemento para cada una.
                    items(categoriasUnicas) { categoria ->
                        // Cuenta cuántos lugares visibles existen en esta categoría.
                        val cantidadLugares = lugares.count { it.categoria == categoria && !it.esOculto }

                        // ⭐️ Diseño de la Tarjeta de Categoría
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(75.dp)
                                .clickable {
                                    // Navega al listado de lugares filtrados.
                                    onNavigateToCategoria(categoria)
                                },
                            shape = RoundedCornerShape(12.dp), // Bordes redondeados
                            colors = CardDefaults.cardColors(
                                // Usa un color variante de superficie con poca opacidad.
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // ⭐️ Ícono distintivo genérico para indicar tipo de lugar.
                                Icon(
                                    Icons.Default.Restaurant,
                                    contentDescription = null,
                                    modifier = Modifier.size(32.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.width(16.dp))

                                // Columna de Título y Subtítulo (contador)
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        categoria,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp
                                    )
                                    Text(
                                        "$cantidadLugares lugares",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.DarkGray
                                    )
                                }
                                // Ícono de flecha indicando que es navegable.
                                Icon(
                                    Icons.Default.KeyboardArrowRight,
                                    contentDescription = "Ver categoría",
                                    tint = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
