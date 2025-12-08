package mx.edu.utng.localmunch.screens



import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType // Necesario para KeyboardType.Email

import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


// Anotación requerida para usar la API experimental de Material 3 (ej: TopAppBar).
@OptIn(ExperimentalMaterial3Api::class)
@Composable
        /**
         * @function PantallaAdminLogin
         * @brief Pantalla de inicio de sesión para el acceso al panel de administración.
         *
         * * Utiliza validación de credenciales hardcodeadas (locales) por simplicidad.
         *
         * @param onLoginSuccess Callback que se ejecuta al autenticar las credenciales.
         * @param onNavigateBack Callback para regresar a la pantalla anterior (BottomNav).
         */
fun PantallaAdminLogin(
    onLoginSuccess: () -> Unit,
    onNavigateBack: () -> Unit
) {
    // 1. Estados Mutables
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    // Estado para mostrar mensajes de error al usuario.
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // 2. Credenciales de Administrador (Hardcodeadas)
    // En un entorno de producción, esto debería estar centralizado en Firebase Authentication o una API.
    val adminUsers = mapOf(
        "admin1@localmunch.com" to "password123",
        "gestor@localmunch.com" to "mypassword"
    )

    // 3. Estructura del Scaffold
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Acceso Administrador") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        // 4. Contenido Principal (Centrado)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center // Centra el contenido verticalmente.
        ) {
            Text(
                "Iniciar Sesión como Administrador",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )
            Spacer(Modifier.height(32.dp))

            // Campo de Correo Electrónico
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo Electrónico") },
                // Configura el teclado para sugerir el formato de email.
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))

            // Campo de Contraseña
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                // Oculta el texto ingresado con puntos.
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))

            // 5. Mensaje de Error
            errorMessage?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error, // Muestra el texto en color rojo.
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))
            }

            // 6. Botón de Login
            Button(
                onClick = {
                    val cleanEmail = email.trim()

                    // Lógica de Validación Local
                    if (adminUsers.containsKey(cleanEmail) && adminUsers[cleanEmail] == password) {
                        // Credenciales correctas: llama al callback de éxito.
                        onLoginSuccess()
                    } else {
                        // Credenciales incorrectas: actualiza el mensaje de error.
                        errorMessage = "Correo Electrónico o contraseña incorrectos"
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                // El botón solo se habilita si ambos campos tienen contenido.
                enabled = email.isNotEmpty() && password.isNotEmpty()
            ) {
                Text("Iniciar Sesión")
            }
        }
    }
}
