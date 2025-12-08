package mx.edu.utng.localmunch.screens

import coil.compose.AsyncImage
import androidx.compose.foundation.*
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// 🔑 Importaciones de ViewModel/Datos
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import mx.edu.utng.localmunch.viewmodel.LugaresViewModel
import mx.edu.utng.localmunch.models.Lugar // Importar el modelo para el tipado

// Nota: Se elimina la importación del DataSource obsoleto.

@OptIn(ExperimentalMaterial3Api::class)
@Composable
        /**
         * @function PantallaLugaresOcultos
         * @brief Muestra lugares marcados con la bandera 'esOculto = true', considerados "tesoros locales".
         *
         * * Estos lugares provienen de la lista completa de Firestore, filtrados por el ViewModel.
         *
         * @param onNavigateToDetalle Callback para navegar al detalle del lugar, recibiendo el ID (String).
         * @param viewModel ViewModel compartido para la gestión de datos de lugares.
         */
fun PantallaLugaresOcultos(
    onNavigateToDetalle: (String) -> Unit, // Usa String (ID de Firebase)
    viewModel: LugaresViewModel = viewModel()
) {
    // 1. OBSERVACIÓN DE DATOS Y FILTRADO
    // Observa el estado reactivo de todos los lugares cargados desde el ViewModel.
    val lugares by viewModel.lugares.collectAsState()
    // Filtra la lista para obtener solo los lugares marcados como ocultos.
    val lugaresOcultos = lugares.filter { it.esOculto }

    // 2. ESTADOS DE FILTRADO Y ORDENAMIENTO
    var ordenamiento by remember { mutableStateOf("calificacion") }
    var mostrarFiltros by remember { mutableStateOf(false) }

    // Aplica el ordenamiento a la lista de lugares ocultos.
    val lugaresOrdenados = remember(ordenamiento, lugaresOcultos) {
        when (ordenamiento) {
            "calificacion" -> lugaresOcultos.sortedByDescending { it.calificacion }
            "nombre" -> lugaresOcultos.sortedBy { it.nombre }
            // Nota: Ordenar por precio requiere convertir precioPromedio a un valor numérico si es posible,
            // pero aquí se ordena por String.
            "precio" -> lugaresOcultos.sortedBy { it.precioPromedio }
            else -> lugaresOcultos
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // 3. HEADER Y OPCIONES DE FILTRO
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            "Lugares Ocultos",
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        )
                        Text(
                            "Tesoros locales no registrados",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = Color(0xFFFFC107) // Estrella amarilla
                    )
                }

                Spacer(Modifier.height(12.dp))

                // Chips principales de Filtro/Ordenamiento
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Chip para mostrar/ocultar filtros adicionales.
                    AssistChip(
                        onClick = { mostrarFiltros = !mostrarFiltros },
                        label = { Text("Filtros") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.FilterList,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                    // Chip para ordenar por calificación (opción por defecto).
                    AssistChip(
                        onClick = { ordenamiento = "calificacion" },
                        label = { Text("Mejor calificados") },
                        leadingIcon = {
                            if (ordenamiento == "calificacion") {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    )
                }

                // Filtros adicionales (se muestran condicionalmente)
                if (mostrarFiltros) {
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = ordenamiento == "nombre",
                            onClick = { ordenamiento = "nombre" },
                            label = { Text("A-Z") }
                        )
                        FilterChip(
                            selected = ordenamiento == "precio",
                            onClick = { ordenamiento = "precio" },
                            label = { Text("Precio") }
                        )
                    }
                }
            }
        }

        // 4. LISTA DE LUGARES
        if (lugaresOrdenados.isEmpty()) {
            // Muestra mensaje si la lista de lugares ocultos está vacía.
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.SearchOff,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = Color.Gray
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "No hay lugares ocultos disponibles",
                        color = Color.Gray,
                        fontSize = 16.sp
                    )
                }
            }
        } else {
            // LazyColumn para renderizar la lista de lugares ordenados.
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Mensaje informativo sobre la naturaleza de la lista.
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                "Estos lugares son recomendados por la comunidad local y no aparecen en aplicaciones comerciales.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }

                // Elementos de la lista (tarjetas de lugar).
                items(lugaresOrdenados) { lugar ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            // Al hacer clic, navega a los detalles.
                            .clickable { onNavigateToDetalle(lugar.id!!) },
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column {
                            Box {
                                // 5. Carga Asíncrona de Imagen
                                AsyncImage(
                                    model = lugar.imagenUrl, // URL de la imagen.
                                    contentDescription = lugar.nombre,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp),
                                    contentScale = ContentScale.Crop,
                                    // Uso de recursos locales para placeholders (es necesario el paquete completo).
                                    placeholder = painterResource(id = mx.edu.utng.localmunch.R.drawable.placeholder_comida),
                                    error = painterResource(id = mx.edu.utng.localmunch.R.drawable.placeholder_comida)
                                )

                                // Overlay de Calificación
                                Surface(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(8.dp),
                                    color = Color.Black.copy(alpha = 0.7f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(
                                            horizontal = 8.dp,
                                            vertical = 4.dp
                                        ),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.Star,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = Color(0xFFFFC107)
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            lugar.calificacion.toString(),
                                            color = Color.White,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            // 6. DETALLES DEL LUGAR EN LA TARJETA
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        lugar.nombre,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                    Icon(
                                        Icons.Default.Star, // Icono redundante, pero presente en el código.
                                        contentDescription = null,
                                        tint = Color(0xFFFFC107),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Spacer(Modifier.height(4.dp))
                                Text(
                                    lugar.categoria,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Spacer(Modifier.height(8.dp))
                                Text(
                                    lugar.descripcion,
                                    fontSize = 14.sp,
                                    color = Color.Gray,
                                    maxLines = 2
                                )

                                Spacer(Modifier.height(12.dp))
                                Divider()
                                Spacer(Modifier.height(12.dp))

                                // Información de Ubicación
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Place,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = Color.Gray
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            lugar.direccion,
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }

                                Spacer(Modifier.height(8.dp))

                                // Información de Horario y Precio
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.AccessTime,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = Color.Gray
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            lugar.horario,
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )
                                    }
                                    Text(
                                        lugar.precioPromedio,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
