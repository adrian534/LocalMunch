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
import mx.edu.utng.localmunch.viewmodel.LugaresViewModel
import mx.edu.utng.localmunch.viewmodel.MisReseñasViewModel
import mx.edu.utng.localmunch.models.Reseña
import mx.edu.utng.localmunch.models.Lugar // Necesitas importar el modelo Lugar

// Anotación requerida para usar la API experimental de Material 3.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
        /**
         * @function PantallaMisResenas
         * @brief Muestra una lista de todas las reseñas que el usuario logueado ha escrito.
         *
         * * Permite la eliminación de las reseñas y muestra el nombre del lugar asociado.
         *
         * @param onNavigateBack Callback para regresar.
         * @param misReseñasViewModel ViewModel específico para manejar las reseñas del usuario.
         * @param lugaresViewModel ViewModel compartido para obtener los nombres de los lugares.
         */
fun PantallaMisResenas(
    onNavigateBack: () -> Unit,
    misReseñasViewModel: MisReseñasViewModel = viewModel(),
    lugaresViewModel: LugaresViewModel = viewModel()
) {
    // 1. Obtener las reseñas del usuario logueado en tiempo real
    // Observa el Flow de reseñas del usuario.
    val misReseñas by misReseñasViewModel.misReseñas.collectAsState()

    // 2. Obtener la lista completa de lugares (para buscar el nombre)
    // Observa la lista completa de lugares para mapear el `lugarId` al `nombre`.
    val lugares by lugaresViewModel.lugares.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Reseñas") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        if (misReseñas.isEmpty()) {
            // Manejo de Lista Vacía o Carga
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                if (lugares.isEmpty()) {
                    // Muestra un indicador si la lista de lugares aún no ha cargado.
                    CircularProgressIndicator()
                } else {
                    Text(
                        "Aún no has escrito ninguna reseña.",
                        fontSize = 18.sp,
                        color = Color.Gray
                    )
                }
            }
        } else {
            // 3. Lista de Reseñas (LazyColumn)
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(misReseñas, key = { it.id ?: "" }) { reseña ->
                    // Busca el objeto Lugar asociado a esta reseña usando el lugarId.
                    val lugarAsociado = lugares.find { it.id == reseña.lugarId }

                    ResenaUsuarioCard(
                        reseña = reseña,
                        // Muestra el nombre si se encuentra, o "Lugar Desconocido" si no.
                        lugarNombre = lugarAsociado?.nombre ?: "Lugar Desconocido",
                        onDelete = {
                            // ⭐️ Implementación de la eliminación y recálculo
                            if (reseña.id != null && reseña.lugarId != null) {
                                // Llama al ViewModel para eliminar la reseña, lo cual también recalcula la calificación del lugar.
                                misReseñasViewModel.deleteReseña(reseña.id, reseña.lugarId)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
        /**
         * @function ResenaUsuarioCard
         * @brief Tarjeta reutilizable para mostrar una reseña específica del usuario.
         *
         * @param reseña El objeto Reseña a mostrar.
         * @param lugarNombre El nombre del lugar asociado (obtenido del LugaresViewModel).
         * @param onDelete Callback para manejar la eliminación de esta reseña.
         */
fun ResenaUsuarioCard(
    reseña: Reseña,
    lugarNombre: String,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Nombre del Lugar
            Text(
                lugarNombre,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))

            // Calificación (Barra de estrellas)
            RatingBar(calificacion = reseña.calificacion.toDouble())

            Text(
                "Comentario: ${reseña.comentario}",
                modifier = Modifier.padding(vertical = 8.dp),
                fontSize = 14.sp
            )
            Text(
                "Fecha: ${reseña.fecha}",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(Modifier.height(12.dp))

            // Botón Eliminar
            TextButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar", modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(4.dp))
                Text("Eliminar Reseña")
            }
        }
    }
}

// Helper para mostrar estrellas (debes definirla en algún lugar)
@Composable
        /**
         * @function RatingBar
         * @brief Muestra una barra de 5 estrellas basada en una calificación numérica.
         *
         * @param calificacion La calificación a mostrar (Double o Int).
         */
fun RatingBar(calificacion: Double) {
    Row {
        // Itera 5 veces para dibujar las estrellas.
        repeat(5) { index ->
            Icon(
                Icons.Default.Star,
                contentDescription = null,
                // Si el índice es menor que la calificación, la estrella está rellena (amarilla).
                tint = if (index < calificacion.toInt()) Color(0xFFFFC107) else Color.LightGray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
