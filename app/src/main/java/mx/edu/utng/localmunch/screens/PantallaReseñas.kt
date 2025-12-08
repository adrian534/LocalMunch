package mx.edu.utng.localmunch.screens

import mx.edu.utng.localmunch.components.EstrellasSeleccionables
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
import androidx.compose.runtime.collectAsState
import com.google.firebase.auth.FirebaseAuth
import mx.edu.utng.localmunch.viewmodel.LugaresViewModel
import mx.edu.utng.localmunch.viewmodel.ReseñasViewModel
import mx.edu.utng.localmunch.viewmodel.ReseñasViewModelFactory
import mx.edu.utng.localmunch.models.Reseña

// Componente que contiene el diseño y la lógica de borrado.
@Composable
        /**
         * @function ReseñaItem
         * @brief Componente reutilizable para mostrar una reseña individual en la lista.
         *
         * * Muestra el contenido y permite la eliminación si la reseña pertenece al usuario logueado.
         *
         * @param reseña El objeto Reseña a mostrar.
         * @param onDelete Callback para iniciar la eliminación de la reseña (recibe el ID).
         */
fun ReseñaItem(
    reseña: Reseña,
    onDelete: (String) -> Unit // Callback para la eliminación
) {
    // 1. Obtener el ID del usuario logueado
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Cabecera: Nombre de Usuario y Fecha
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Bloque izquierdo (Usuario, Fecha)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.AccountCircle,
                        contentDescription = "Avatar de usuario",
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            reseña.usuarioNombre, // Usamos el nombre del modelo
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            reseña.fecha,
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }

                // Bloque derecho (Estrellas Fijas)
                Row {
                    repeat(5) { index ->
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            // Determina si la estrella está rellena (amarilla) o vacía (gris).
                            tint = if (index < reseña.calificacion) Color(0xFFFFC107) else Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))

            // Comentario
            Text(
                reseña.comentario,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )

            // 2. LÓGICA DE BORRADO CONDICIONAL
            // Solo muestra el botón si el usuario logueado es el autor de la reseña.
            if (currentUserId != null && currentUserId == reseña.usuarioId) {
                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    // 3. Mostrar el botón de borrar
                    TextButton(
                        onClick = { onDelete(reseña.id!!) }, // Llama al callback con el ID.
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Eliminar Reseña",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Eliminar")
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
        /**
         * @function PantallaReseñas
         * @brief Pantalla principal para ver y agregar reseñas de un lugar específico.
         *
         * @param lugarId ID del lugar cuyas reseñas se están mostrando.
         * @param onNavigateBack Callback para regresar.
         * @param lugaresViewModel ViewModel compartido para obtener los datos del lugar.
         */
fun PantallaReseñas(
    lugarId: String,
    onNavigateBack: () -> Unit,
    lugaresViewModel: LugaresViewModel = viewModel()
) {
    // 1. Inicialización Específica del ViewModel
    // Se usa una Factory para crear una instancia del ReseñasViewModel que ya esté
    // filtrada por el lugarId.
    val reseñasViewModel: ReseñasViewModel = viewModel(
        factory = ReseñasViewModelFactory(lugarId)
    )

    // 2. Observación de Estados
    val lugares by lugaresViewModel.lugares.collectAsState() // Lista global de lugares.
    val reseñas by reseñasViewModel.reseñas.collectAsState() // Lista de reseñas para este lugar.

    val lugar = lugares.find { it.id == lugarId } // Encuentra el lugar actual.

    // 3. Estados del Formulario de Nueva Reseña
    var nuevaReseña by remember { mutableStateOf("") }
    var calificacionSeleccionada by remember { mutableIntStateOf(5) }
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reseñas") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->

        // Manejo de Lugar No Encontrado
        if (lugar == null && lugares.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Lugar de reseña no encontrado.")
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 4. Header con Promedio de Calificación
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        lugar?.nombre ?: "Lugar Cargando...",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        // Calificación promedio (actualizada por el Repositorio).
                        Text(
                            lugar?.calificacion.toString(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 32.sp
                        )
                    }
                    // Contador total de reseñas.
                    Text(
                        "${reseñas.size} reseñas",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }

            // 5. Lista de reseñas existentes
            LazyColumn(
                modifier = Modifier.weight(1f), // Ocupa el espacio restante.
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Muestra las reseñas en orden inverso (la más reciente primero).
                items(reseñas.reversed()) { reseña ->
                    // 🔑 USO DE RESEÑA ITEM CON LÓGICA DE BORRADO
                    ReseñaItem(
                        reseña = reseña,
                        onDelete = { reseñaId ->
                            // Llama al ViewModel para eliminar la reseña (que maneja el recálculo).
                            reseñasViewModel.deleteReseña(reseñaId)
                        }
                    )
                }
            }

            // 6. Formulario para agregar una nueva reseña
            Card(
                modifier = Modifier.fillMaxWidth(),
                // Ajuste de forma para fusionarse con el borde inferior si es posible.
                shape = MaterialTheme.shapes.large.copy(
                    bottomStart = MaterialTheme.shapes.extraSmall.bottomStart,
                    bottomEnd = MaterialTheme.shapes.extraSmall.bottomEnd
                ),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Agregar tu reseña",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(Modifier.height(12.dp))

                    // Selector de estrellas (llama al componente EstrellasSeleccionables).
                    EstrellasSeleccionables(
                        calificacionActual = calificacionSeleccionada,
                        onCalificacionChange = { calificacionSeleccionada = it }
                    )
                    Spacer(Modifier.height(12.dp))

                    // Campo de texto para el comentario.
                    OutlinedTextField(
                        value = nuevaReseña,
                        onValueChange = { nuevaReseña = it },
                        label = { Text("Escribe tu opinión") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5
                    )

                    Spacer(Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (nuevaReseña.isNotEmpty() && lugarId.isNotEmpty()) {
                                // Llama al ViewModel para añadir la nueva reseña.
                                reseñasViewModel.addReseña(
                                    comentario = nuevaReseña,
                                    calificacion = calificacionSeleccionada
                                )
                                // Limpia el formulario después del envío.
                                nuevaReseña = ""
                                calificacionSeleccionada = 5
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = nuevaReseña.isNotEmpty() // Solo se habilita si hay comentario.
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Enviar")
                        Spacer(Modifier.width(8.dp))
                        Text("Enviar reseña")
                    }
                }
            }
        }
    }
}
