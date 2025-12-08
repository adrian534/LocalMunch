package mx.edu.utng.localmunch.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage // Necesario para la imagen
import mx.edu.utng.localmunch.models.Lugar
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.layout.ContentScale


// Anotación requerida para usar la API experimental de Material 3.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
        /**
         * @function PantallaListadoCategoria
         * @brief Muestra los lugares de comida filtrados por una categoría específica.
         *
         * @param onNavigateBack Callback para regresar a la pantalla anterior.
         * @param onNavigateToDetalle Callback para ir a los detalles de un lugar seleccionado.
         * @param categoria El nombre de la categoría a mostrar (ej: "Tacos").
         * @param lista La lista de objetos Lugar que ya están filtrados.
         */
fun PantallaListadoCategoria(
    onNavigateBack: () -> Unit,
    onNavigateToDetalle: (String) -> Unit,
    categoria: String,
    lista: List<Lugar>
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Categoría: $categoria") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->

        if (lista.isEmpty()) {
            // Muestra un mensaje si la lista filtrada está vacía.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No hay locales de '$categoria' disponibles.",
                    color = Color.Gray,
                    fontSize = 16.sp
                )
            }
        } else {
            // ⭐️ CAMBIO CLAVE: Usar LazyColumn
            // LazyColumn es eficiente para manejar listas potencialmente grandes.
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp) // Espacio entre cada tarjeta.
            ) {
                // Itera sobre la lista de lugares filtrados.
                items(lista, key = { it.id ?: it.nombre }) { lugar ->
                    // ⭐️ USO DEL NUEVO COMPONENTE DE TARJETA
                    LugarListCard(
                        lugar = lugar,
                        // Pasa el ID del lugar al callback de navegación de detalle.
                        onClick = { onNavigateToDetalle(lugar.id ?: "") }
                    )
                }
            }
        }
    }
}

// ⭐️ NUEVO COMPONENTE: Tarjeta detallada para el listado
@Composable
        /**
         * @function LugarListCard
         * @brief Componente reutilizable para mostrar un Lugar en un formato de tarjeta compacta para listas.
         *
         * @param lugar El objeto Lugar a mostrar.
         * @param onClick La acción a ejecutar al hacer clic en la tarjeta (navegación a detalle).
         */
fun LugarListCard(lugar: Lugar, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick), // Habilita la navegación al hacer clic.
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagen a la izquierda (Carga Asíncrona)
            AsyncImage(
                model = lugar.imagenUrl,
                contentDescription = lugar.nombre,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp)), // Bordes redondeados para la imagen.
                contentScale = ContentScale.Crop // Asegura que la imagen llene el espacio.
            )

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Nombre
                Text(
                    lugar.nombre,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    maxLines = 1,
                    // ⭐️ Evita que el texto del nombre se desborde, reemplazándolo con puntos suspensivos.
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(4.dp))

                // Categoría
                Text(
                    lugar.categoria,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(Modifier.height(4.dp))

                // Calificación
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = "Calificación",
                        tint = Color(0xFFFFC107), // Color amarillo
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        lugar.calificacion.toString(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
