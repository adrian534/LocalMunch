package mx.edu.utng.localmunch.screens

import androidx.compose.ui.graphics.Color
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter // Importación de Coil
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import mx.edu.utng.localmunch.models.Lugar
import mx.edu.utng.localmunch.viewmodel.LugaresViewModel

@Composable
        /**
         * @function PantallaMapa
         * @brief Muestra los lugares de comida en un mapa interactivo de Google Maps.
         *
         * * Gestiona permisos de ubicación y permite interactuar con los marcadores.
         *
         * @param onNavigateToDetalle Callback para ir a la vista de detalle de un lugar (con ID).
         * @param viewModel ViewModel compartido para la gestión de datos de lugares.
         */
fun PantallaMapa(
    onNavigateToDetalle: (String) -> Unit,
    viewModel: LugaresViewModel = viewModel()
) {
    val context = LocalContext.current
    // Observa la lista de lugares en tiempo real.
    val lugares by viewModel.lugares.collectAsState()
    // Estado para guardar el lugar seleccionado al hacer clic en un marcador.
    var ubicacionSeleccionada by remember { mutableStateOf<Lugar?>(null) }
    var searchQuery by remember { mutableStateOf("") } // Estado para la búsqueda (si se implementa)

    // --- Lógica de permisos de ubicación ---
    var hasLocationPermission by remember {
        mutableStateOf(
            // Verifica el estado inicial del permiso de ubicación precisa.
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Launcher para solicitar los permisos de forma reactiva.
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Actualiza el estado si se concedió cualquiera de los dos permisos de ubicación.
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    // Solicitar permisos al iniciar
    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            // Lanza la solicitud de permisos al entrar a la pantalla si no los tiene.
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // --- Lógica de filtrado ---
    val lugaresFiltrados = remember(lugares, searchQuery) {
        // Filtra los lugares según la búsqueda y excluye los ocultos.
        if (searchQuery.isEmpty()) {
            lugares.filter { !it.esOculto }
        } else {
            lugares.filter {
                (it.nombre.contains(searchQuery, ignoreCase = true) ||
                        it.categoria.contains(searchQuery, ignoreCase = true)) &&
                        !it.esOculto
            }
        }
    }

    // --- Lógica de cámara y centro inicial ---
    val centerLocation = remember(lugares) {
        // Calcula el centro promedio de todos los lugares cargados.
        if (lugares.isNotEmpty()) {
            val avgLat = lugares.map { it.latitud }.average()
            val avgLng = lugares.map { it.longitud }.average()
            LatLng(avgLat, avgLng)
        } else {
            // Coordenadas de ejemplo (San Luis Potosí) si la lista está vacía.
            LatLng(22.1565, -100.9855)
        }
    }

    // Estado de la cámara: define la posición inicial y el zoom.
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(centerLocation, 13f)
    }

    // --- ESTRUCTURA PRINCIPAL: MAPA A PANTALLA COMPLETA ---
    Column(modifier = Modifier.fillMaxSize()) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f) // Hace que el mapa ocupe todo el espacio disponible.
        ) {
            if (!hasLocationPermission) {
                // Mostrar UI para pedir permiso si no está concedido
                SolicitarPermisoUbicacion {
                    // Si el usuario presiona el botón, lanza nuevamente la solicitud de permiso.
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            } else {
                // 2. Mapa de Google
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(
                        // Habilita la capa de "Mi ubicación" si el permiso está concedido.
                        isMyLocationEnabled = hasLocationPermission,
                        mapStyleOptions = null // Permite estilos de mapa por defecto.
                    ),
                    uiSettings = MapUiSettings(
                        zoomControlsEnabled = true,
                        myLocationButtonEnabled = hasLocationPermission, // Habilita el botón de ubicación
                        mapToolbarEnabled = true
                    )
                ) {
                    // Marcadores (Markers)
                    lugaresFiltrados.forEach { lugar ->
                        Marker(
                            state = MarkerState(position = LatLng(lugar.latitud, lugar.longitud)),
                            title = lugar.nombre,
                            snippet = "${lugar.categoria} - ⭐ ${lugar.calificacion}",
                            onClick = {
                                // Al hacer clic en el marcador, guarda el lugar y abre el diálogo.
                                ubicacionSeleccionada = lugar
                                true
                            }
                        )
                    }
                }
            }
        }

        // 3. Diálogo de detalles MODIFICADO con la imagen
        ubicacionSeleccionada?.let { lugar ->
            AlertDialog(
                onDismissRequest = { ubicacionSeleccionada = null }, // Se cierra al pulsar fuera.
                title = { Text(lugar.nombre) },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {

                        // Carga la imagen desde la URL usando Coil
                        val painter = rememberAsyncImagePainter(model = lugar.imagenUrl)
                        Image(
                            painter = painter,
                            contentDescription = "Imagen de ${lugar.nombre}",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )

                        Spacer(Modifier.height(16.dp))
                        Text("Categoría: ${lugar.categoria}\nCalificación: ${lugar.calificacion}")
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        ubicacionSeleccionada = null
                        // Navega a la pantalla de detalle pasando el ID
                        onNavigateToDetalle(lugar.id!!)
                    }) {
                        Text("Ver detalles")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { ubicacionSeleccionada = null }) {
                        Text("Cerrar")
                    }
                }
            )
        }
    }
}

// Función de Composable para solicitar permiso de ubicación
@Composable
/**
 * @function SolicitarPermisoUbicacion
 * @brief Muestra un mensaje de UI cuando los permisos de ubicación no están concedidos.
 *
 * @param onRequestPermission Callback para lanzar la solicitud de permisos.
 */
internal fun SolicitarPermisoUbicacion(onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.LocationOff,
            contentDescription = "Ubicación requerida",
            modifier = Modifier.size(64.dp),
            tint = Color.Gray
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "Necesitamos tu ubicación para mostrar los locales cercanos.",
            color = Color.DarkGray
        )
        Spacer(Modifier.height(8.dp))
        Button(onClick = onRequestPermission) {
            Text("Permitir Ubicación")
        }
    }
}
