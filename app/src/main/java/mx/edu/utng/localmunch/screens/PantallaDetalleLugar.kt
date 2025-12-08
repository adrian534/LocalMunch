package mx.edu.utng.localmunch.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import mx.edu.utng.localmunch.R
import mx.edu.utng.localmunch.viewmodel.LugaresViewModel
import mx.edu.utng.localmunch.viewmodel.FavoritosViewModel
import mx.edu.utng.localmunch.viewmodel.ReseñasViewModel
import mx.edu.utng.localmunch.viewmodel.ReseñasViewModelFactory
import android.content.Intent
import android.net.Uri

// Anotación requerida para usar la API experimental de Material 3.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
        /**
         * @function PantallaDetalleLugar
         * @brief Muestra la información detallada de un lugar, incluyendo descripción, contacto,
         * calificación y funcionalidad para añadir/quitar favoritos.
         *
         * @param lugarId ID del lugar a mostrar (recibido como argumento de navegación).
         * @param onNavigateBack Callback para volver a la pantalla anterior.
         * @param onNavigateToReseñas Callback para ir a la lista de reseñas del lugar.
         * @param viewModel ViewModel compartido de Lugares.
         * @param favoritosViewModel ViewModel para gestionar el estado de favoritos.
         */
fun PantallaDetalleLugar(
    lugarId: String,
    onNavigateBack: () -> Unit,
    onNavigateToReseñas: (String) -> Unit,
    viewModel: LugaresViewModel = viewModel(),
    favoritosViewModel: FavoritosViewModel = viewModel()
) {
    val context = LocalContext.current

    // 1. Obtiene la lista completa y busca el lugar (reactivamente)
    // Observa todos los lugares y busca el que coincida con el ID.
    val lugares by viewModel.lugares.collectAsState()
    val lugar = lugares.find { it.id == lugarId }

    // ⭐️ CORRECCIÓN CRÍTICA: Validamos el ID del lugar para la inyección del ViewModel
    val idValido = lugar?.id

    // 2. CREACIÓN CONDICIONAL DEL VIEWMODEL DE RESEÑAS (Resuelve el crash)
    // Solo creamos el ReseñasViewModel si el ID del lugar es válido y no nulo.
    val reseñasViewModel: ReseñasViewModel? = if (idValido != null) {
        // Se usa una factory para inyectar el ID del lugar en el constructor del ViewModel.
        viewModel(
            factory = ReseñasViewModelFactory(idValido)
        )
    } else {
        null
    }

    // 3. SINCRONIZACIÓN DE RESEÑAS: Colectar el contador en tiempo real
    val contadorReseñas = if (reseñasViewModel != null) {
        // Obtenemos el tamaño de la lista de reseñas del lugar en tiempo real.
        // Se usa collectAsState para que la UI se actualice automáticamente.
        reseñasViewModel.getReseñasByLugarId(idValido!!).collectAsState(initial = emptyList()).value.size
    } else {
        0
    }

    // 4. Gestión de Favoritos
    // Observa los IDs de los lugares marcados como favoritos por el usuario.
    val favoritosIds by favoritosViewModel.favoritos.collectAsState()
    // Determina si el lugar actual está en la lista de IDs favoritos.
    val esFavorito = lugar?.id?.let { favoritosIds.contains(it) } ?: false

    val snackbarHostState = remember { SnackbarHostState() }

    // Cargar favoritos al iniciar (se ejecuta solo una vez)
    LaunchedEffect(Unit) {
        favoritosViewModel.cargarFavoritos(context)
    }

    // 5. Estructura del Scaffold
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalles") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Compartir */ }) {
                        Icon(Icons.Default.Share, contentDescription = "Compartir")
                    }
                    // BOTÓN DE FAVORITOS (Toggle)
                    IconButton(
                        onClick = {
                            lugar?.id?.let { id ->
                                // Llama a la función de toggle para añadir o quitar.
                                favoritosViewModel.toggleFavorito(context, id)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (esFavorito) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (esFavorito) "Quitar de favoritos" else "Agregar a favoritos",
                            // El color del icono refleja el estado actual (primario si es favorito).
                            tint = if (esFavorito) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        // Manejo del estado de carga/no encontrado.
        if (lugar == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                if (lugares.isEmpty()) {
                    CircularProgressIndicator()
                    Text("Cargando detalles...")
                } else {
                    // El lugar no se encontró, aunque la lista sí cargó.
                    Text("Lugar no encontrado (ID: $lugarId)")
                }
            }
            return@Scaffold
        }

        // --- Contenido del Detalle ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                // Habilita el desplazamiento vertical.
                .verticalScroll(rememberScrollState())
        ) {
            // Imagen principal (Carga Asíncrona con Coil)
            AsyncImage(
                model = lugar.imagenUrl,
                contentDescription = "Imagen lugar",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                contentScale = ContentScale.Crop,
                // Placeholders para cuando la imagen no carga o mientras está cargando.
                placeholder = painterResource(id = R.drawable.placeholder_comida),
                error = painterResource(id = R.drawable.placeholder_comida)
            )

            Column(modifier = Modifier.padding(16.dp)) {
                // Nombre y categoría
                Text(
                    lugar.nombre,
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp
                )
                Text(
                    "${lugar.categoria} • ${lugar.direccion}",
                    color = Color.Gray,
                    fontSize = 14.sp
                )

                Spacer(Modifier.height(16.dp))

                // Calificación y reseñas (Área Clickable)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        // Hace toda la fila clickeable para ir a la pantalla de reseñas.
                        .clickable { onNavigateToReseñas(lugar.id!!) },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFC107), // Color amarillo
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            lugar.calificacion.toString(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        // Muestra el contador de reseñas en tiempo real.
                        Text(
                            " ($contadorReseñas reseñas)",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                    // Icono de flecha para indicar navegación.
                    Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.Gray)
                }

                Spacer(Modifier.height(24.dp))
                Divider()
                Spacer(Modifier.height(24.dp))

                // Información de contacto (usando el Composable InfoRow)
                Text("Información", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(Modifier.height(12.dp))

                InfoRow(
                    icon = Icons.Default.Phone,
                    label = "Teléfono",
                    value = lugar.telefono
                )
                InfoRow(
                    icon = Icons.Default.AccessTime,
                    label = "Horario",
                    value = lugar.horario
                )
                InfoRow(
                    icon = Icons.Default.AttachMoney,
                    label = "Precio promedio",
                    value = lugar.precioPromedio
                )
                InfoRow(
                    icon = Icons.Default.Place,
                    label = "Dirección",
                    value = lugar.direccion
                )

                Spacer(Modifier.height(24.dp))
                Divider()
                Spacer(Modifier.height(24.dp))

                // Descripción
                Text("Acerca de", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    lugar.descripcion,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )

                Spacer(Modifier.height(32.dp))

                // ⭐️ BOTÓN DE ACCIÓN: IMPLEMENTACIÓN DE LLAMADA
                OutlinedButton(
                    onClick = {
                        // Crea un Intent (Intención) para abrir la aplicación de teléfono
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            // Especifica el número de teléfono con el esquema 'tel:'
                            data = Uri.parse("tel:${lugar.telefono}")
                        }
                        context.startActivity(intent) // Ejecuta la acción de llamada.
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Icon(Icons.Default.Phone, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Llamar al lugar", fontSize = 16.sp)
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
        /**
         * @function InfoRow
         * @brief Componente reutilizable para mostrar un par de icono/etiqueta/valor de información.
         */
fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.width(16.dp))
        Column {
            Text(
                label,
                fontSize = 12.sp,
                color = Color.Gray
            )
            Text(
                value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
