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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import mx.edu.utng.localmunch.viewmodel.LugaresViewModel
import mx.edu.utng.localmunch.models.Lugar
import mx.edu.utng.localmunch.viewmodel.AdminViewModel
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.Text

// Anotación requerida para usar la API experimental de Material 3 (ej: TopAppBar).
@OptIn(ExperimentalMaterial3Api::class)
@Composable
        /**
         * @function PantallaAdminDashboard
         * @brief Pantalla principal del panel de administración. Permite al admin buscar, añadir,
         * editar y eliminar lugares.
         *
         * @param onNavigateBack Callback para regresar (ej. al BottomNav).
         * @param onLogout Callback para cerrar sesión y navegar al Login general.
         * @param onNavigateToAddEditLugar Callback para ir a la pantalla de edición/adición.
         * @param viewModel ViewModel compartido que contiene el listado de lugares.
         * @param adminViewModel ViewModel para manejar la lógica de la sesión de administrador.
         */
fun PantallaAdminDashboard(
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToAddEditLugar: (String?) -> Unit,
    viewModel: LugaresViewModel = viewModel(),
    adminViewModel: AdminViewModel = viewModel() // View Model para cerrar sesión
) {
    // 1. Estados y Datos
    // Observa la lista de lugares en tiempo real desde el ViewModel.
    val lugares by viewModel.lugares.collectAsState()
    // Estado del texto ingresado en el campo de búsqueda.
    var searchQuery by remember { mutableStateOf("") }
    // Estado para guardar temporalmente el lugar a eliminar (activa el diálogo).
    var lugarToDelete by remember { mutableStateOf<Lugar?>(null) }

    // El ViewModel ya inicia la carga de datos.
    LaunchedEffect(Unit) {
        // No es necesario llamar a refreshLugares() aquí si el ViewModel lo hace en init.
    }

    // 2. Lógica de Filtrado y Búsqueda
    val filteredLugares = remember(lugares, searchQuery) {
        if (searchQuery.isEmpty()) {
            lugares // Muestra todos si la búsqueda está vacía.
        } else {
            // Filtra los lugares cuyo nombre, categoría o dirección contenga el texto de búsqueda.
            lugares.filter { lugar ->
                lugar.nombre.contains(searchQuery, ignoreCase = true) ||
                        lugar.categoria.contains(searchQuery, ignoreCase = true) ||
                        lugar.direccion.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    // 3. Estructura del Scaffold
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel de Administración") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    // 🔑 Implementación del Cierre de Sesión del Administrador.
                    IconButton(onClick = {
                        adminViewModel.cerrarSesion() // 1. 🔥 Cierra la sesión de Firestore (si aplica)
                        onLogout() // 2. 🚪 Navega al Login General (manejo de pila en NavGraph.kt)
                    }) {
                        Icon(Icons.Filled.Logout, contentDescription = "Cerrar Sesión")
                    }
                }
            )
        },
        floatingActionButton = {
            // Botón flotante para añadir un nuevo lugar. Pasa 'null' para indicar modo "Añadir".
            FloatingActionButton(onClick = { onNavigateToAddEditLugar(null) }) {
                Icon(Icons.Filled.Add, contentDescription = "Añadir Lugar")
            }
        }
    ) { paddingValues ->
        // 4. Contenido Principal
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 16.dp)) {

            Spacer(Modifier.height(8.dp))

            // Campo de búsqueda interactivo
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Buscar Lugar") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            // Lista de Lugares (muestra los filtrados)
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(filteredLugares) { lugar ->
                    LugarAdminItem(
                        lugar = lugar,
                        // Navega a la pantalla de edición, pasando el ID del lugar.
                        onEdit = { onNavigateToAddEditLugar(lugar.id) },
                        // Pone el lugar en el estado para activar el diálogo de confirmación.
                        onDelete = { lugarToDelete = lugar }
                    )
                    Divider()
                }
            }
        }
    }

    // 5. Diálogo de Confirmación para Eliminar
    lugarToDelete?.let { lugar ->
        AlertDialog(
            onDismissRequest = { lugarToDelete = null }, // Se cierra si se pulsa fuera.
            title = { Text("Confirmar Eliminación") },
            text = { Text("¿Estás seguro de que quieres eliminar el lugar: ${lugar.nombre}?") },
            confirmButton = {
                Button(onClick = {
                    // 🔑 Lógica de Eliminación directa en Firestore
                    // Se asume que el ID (lugar.id) no es nulo aquí ('!!').
                    com.google.firebase.firestore.FirebaseFirestore.getInstance()
                        .collection("lugares")
                        .document(lugar.id!!)
                        .delete()
                    lugarToDelete = null // Cierra el diálogo tras la operación.
                }) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { lugarToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

// 6. Composable para cada elemento de la lista
@Composable
        /**
         * @function LugarAdminItem
         * @brief Elemento de lista interactivo para la vista de administración.
         *
         * @param lugar El objeto Lugar a mostrar.
         * @param onEdit Callback para iniciar la edición de este lugar.
         * @param onDelete Callback para iniciar el proceso de eliminación (activar el diálogo).
         */
fun LugarAdminItem(
    lugar: Lugar,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit) // Toda la fila es clickeable para editar.
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Columna principal con nombre y categoría
        Column(modifier = Modifier.weight(1f)) {
            Text(lugar.nombre, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text("Categoría: ${lugar.categoria}", style = MaterialTheme.typography.bodySmall)
        }

        // Botón de Edición (icono de lápiz)
        IconButton(onClick = onEdit) {
            Icon(Icons.Filled.Edit, contentDescription = "Editar")
        }

        // Botón de Eliminación (icono de bote de basura)
        IconButton(onClick = onDelete) {
            Icon(Icons.Filled.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
        }
    }
}
