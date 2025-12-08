package mx.edu.utng.localmunch.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.CachePolicy
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import mx.edu.utng.localmunch.R
import mx.edu.utng.localmunch.models.Lugar
import mx.edu.utng.localmunch.viewmodel.LugaresViewModel

// Anotación requerida para usar la API experimental de Material 3 (ej: TopAppBar).
@OptIn(ExperimentalMaterial3Api::class)
@Composable
        /**
         * @function PantallaAddEditLugar
         * @brief Pantalla para la creación (Add) o modificación (Edit) de un lugar.
         *
         * * Utiliza el mismo formulario para ambas operaciones, diferenciadas por el lugarId.
         *
         * @param lugarId ID del lugar a editar (nulo o vacío si es un nuevo lugar).
         * @param onNavigateBack Callback para regresar a la pantalla anterior (Dashboard Admin).
         * @param viewModel ViewModel que maneja la lógica de negocio y persistencia de lugares.
         */
fun PantallaAddEditLugar(
    lugarId: String?,
    onNavigateBack: () -> Unit,
    viewModel: LugaresViewModel = viewModel()
) {
    // 1. Estados y Variables de Control
    // Estado de la lista completa de lugares (observado desde el ViewModel).
    val lugares by viewModel.lugares.collectAsState()
    // Determina si estamos en modo edición o creación.
    val isEditing = lugarId != null && lugarId.isNotBlank()
    // Ámbito de corrutinas para manejar operaciones asíncronas (guardar, mostrar SnackBar).
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // 2. Estados Mutables del Formulario
    // Se definen los estados para cada campo de entrada del modelo Lugar.
    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var precioPromedio by remember { mutableStateOf("") }
    // Los campos numéricos de Latitud y Longitud se manejan como String para facilitar la entrada.
    var latitudText by remember { mutableStateOf("") }
    var longitudText by remember { mutableStateOf("") }
    var calificacionText by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var horario by remember { mutableStateOf("") }
    // Estado del Switch para ocultar el lugar.
    var esOculto by remember { mutableStateOf(false) }

    var imageUrl by remember { mutableStateOf("") }

    // Estado del SnackBar para mostrar mensajes al usuario.
    val snackbarHostState = remember { SnackbarHostState() }

    // 3. Efecto de Carga de Datos (Modo Edición)
    LaunchedEffect(lugarId, lugares) {
        // Solo se ejecuta si estamos editando y la lista de lugares está cargada.
        if (isEditing) {
            val lugarToEdit = lugares.find { it.id == lugarId }
            // Si el lugar existe, se inicializan los estados del formulario con sus datos.
            lugarToEdit?.let {
                nombre = it.nombre
                descripcion = it.descripcion
                categoria = it.categoria
                direccion = it.direccion
                precioPromedio = it.precioPromedio
                // Conversión de Double a String para mostrar en el TextField.
                latitudText = it.latitud.toString()
                longitudText = it.longitud.toString()
                calificacionText = it.calificacion.toString()
                telefono = it.telefono
                horario = it.horario
                esOculto = it.esOculto
                imageUrl = it.imagenUrl ?: ""
            }
        }
    }

    // 4. Función de Guardado
    fun saveLugar() {
        // 4.1. Validaciones Básicas de Campos Requeridos
        if (nombre.isBlank()) {
            coroutineScope.launch { snackbarHostState.showSnackbar("El nombre es obligatorio") }
            return
        }

        // Validación y conversión de coordenadas.
        val lat = latitudText.toDoubleOrNull()
        val lon = longitudText.toDoubleOrNull()

        if (lat == null || lon == null) {
            coroutineScope.launch { snackbarHostState.showSnackbar("Las coordenadas deben ser números válidas") }
            return
        }

        // Conversión de calificación (usa 0.0 si el campo está vacío o es inválido).
        val cal = calificacionText.toDoubleOrNull() ?: 0.0

        // 4.2. Ejecución de la Operación de Guardado (Asíncrona)
        coroutineScope.launch {
            try {
                // Prepara la URL final (asegura que sea nula si está vacía).
                val finalImageUrl = if (imageUrl.isBlank()) null else imageUrl.trim()

                // Construye el objeto Lugar con los datos actuales del formulario.
                val lugarTemp = Lugar(
                    id = if (isEditing) lugarId else null, // Mantiene el ID si edita, nulo si crea.
                    nombre = nombre.trim(),
                    descripcion = descripcion.trim(),
                    categoria = categoria.trim(),
                    precioPromedio = precioPromedio.trim(),
                    direccion = direccion.trim(),
                    latitud = lat,
                    longitud = lon,
                    calificacion = cal,
                    imagenUrl = finalImageUrl,
                    telefono = telefono.trim(),
                    horario = horario.trim(),
                    esOculto = esOculto
                )

                // Llama al ViewModel para guardar o actualizar el lugar.
                viewModel.saveLugar(lugarTemp, null)

                // Muestra la confirmación.
                if (isEditing) {
                    snackbarHostState.showSnackbar("Lugar actualizado correctamente")
                } else {
                    snackbarHostState.showSnackbar("Lugar agregado correctamente")
                }

                // Espera brevemente y navega de vuelta.
                delay(1500)
                onNavigateBack()

            } catch (e: Exception) {
                // Manejo de errores de Firestore/ViewModel.
                Log.e("PantallaAddEditLugar", "Error al guardar lugar", e)
                coroutineScope.launch { snackbarHostState.showSnackbar("Error: ${e.message}") }
            }
        }
    }

    // 5. Estructura Principal de la UI (Scaffold)
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Editar Lugar" else "Añadir Nuevo Lugar") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }, // Contenedor para los mensajes de SnackBar
        bottomBar = {
            // Botón de acción principal (Guardar/Crear)
            Button(
                onClick = ::saveLugar, // Referencia a la función de guardado.
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                // El botón solo está habilitado si los campos clave están llenos.
                enabled = nombre.isNotBlank() && latitudText.isNotBlank() && longitudText.isNotBlank()
            ) {
                Text(if (isEditing) "Guardar Cambios" else "Crear Lugar")
            }
        }
    ) { paddingValues ->
        // 6. Contenido del Formulario
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                // Habilita el desplazamiento vertical para formularios largos.
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(Modifier.height(16.dp))

            // SECCIÓN DE IMAGEN MEJORADA CON PREVIEW
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Imagen del lugar",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(8.dp))

                // Campo para ingresar la URL de la imagen
                OutlinedTextField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    label = { Text("URL de Imagen") },
                    placeholder = { Text("https://i.imgur.com/ejemplo.jpg") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Link, contentDescription = null) },
                    supportingText = {
                        Text(
                            "Sube tu imagen a imgur.com y pega la URL directa aquí",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    singleLine = false,
                    maxLines = 3
                )

                Spacer(Modifier.height(12.dp))

                // 6.1. Preview en tiempo real
                if (imageUrl.isNotBlank() && imageUrl.startsWith("http")) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            // 🚀 Carga Asíncrona de la Imagen usando Coil
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(imageUrl)
                                    // Deshabilita la caché para forzar la recarga de la URL y ver el preview inmediatamente.
                                    .memoryCachePolicy(CachePolicy.DISABLED)
                                    .diskCachePolicy(CachePolicy.DISABLED)
                                    .build(),

                                contentDescription = "Preview de imagen",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )

                            // Badge "Preview" flotante
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp),
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    "PREVIEW",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Botón para limpiar URL.
                    TextButton(
                        onClick = { imageUrl = "" },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Limpiar URL")
                    }
                } else {
                    // 6.2. Mensaje de Placeholder cuando no hay URL
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.ImageNotSupported,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = Color.Gray
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "Pega una URL para ver preview",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Divider()
            Spacer(Modifier.height(16.dp))

            // 7. Campos de Entrada (TextFields)

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre del Establecimiento *") },
                modifier = Modifier.fillMaxWidth(),
                isError = nombre.isBlank() // Indica error si el campo requerido está vacío.
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción del Lugar") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = categoria,
                onValueChange = { categoria = it },
                label = { Text("Categoría (ej: Café, Restaurante)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = direccion,
                onValueChange = { direccion = it },
                label = { Text("Dirección completa") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = precioPromedio,
                onValueChange = { precioPromedio = it },
                label = { Text("Precio Promedio (ej: \$100 - \$300)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            // Fila para Latitud y Longitud
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = latitudText,
                    onValueChange = { latitudText = it },
                    label = { Text("Latitud *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), // Teclado numérico
                    modifier = Modifier.weight(1f),
                    // Muestra error si el texto no puede convertirse a Double y no está vacío.
                    isError = latitudText.toDoubleOrNull() == null && latitudText.isNotBlank()
                )
                OutlinedTextField(
                    value = longitudText,
                    onValueChange = { longitudText = it },
                    label = { Text("Longitud *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    isError = longitudText.toDoubleOrNull() == null && longitudText.isNotBlank()
                )
            }
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = calificacionText,
                onValueChange = {
                    // Filtra para permitir solo dígitos y el punto decimal.
                    calificacionText = it.filter { char -> char.isDigit() || char == '.' }.take(3)
                },
                label = { Text("Calificación (0.0 a 5.0)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = telefono,
                onValueChange = { telefono = it },
                label = { Text("Teléfono") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = horario,
                onValueChange = { horario = it },
                label = { Text("Horario") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))

            // 8. Opciones Adicionales (Switch)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Ocultar lugar de la lista principal", fontWeight = FontWeight.SemiBold)
                Switch(
                    checked = esOculto,
                    onCheckedChange = { esOculto = it } // Alterna el estado de oculto.
                )
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
