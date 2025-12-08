package mx.edu.utng.localmunch.screens


import androidx.compose.foundation.background
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.android.gms.tasks.Task

// 1. INSTANCIAS GLOBALES DE FIREBASE
// Obtiene la instancia de Firebase Authentication.
private val auth: FirebaseAuth = Firebase.auth
// Obtiene la instancia de Firebase Firestore (aunque no se usa directamente en el login, es una convención).
private val db: FirebaseFirestore = Firebase.firestore

/**
 * @function signInUser
 * @brief Función utilitaria para autenticar al usuario usando correo electrónico y contraseña en Firebase Auth.
 *
 * @param email Correo electrónico del usuario.
 * @param password Contraseña del usuario.
 * @param callback Función de retorno que recibe un Result<Unit> para manejar el éxito o el fallo.
 */
fun signInUser(
    email: String,
    password: String,
    callback: (Result<Unit>) -> Unit
) {
    FirebaseAuth.getInstance()
        .signInWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Si el inicio de sesión es exitoso, llama al callback con éxito.
                callback(Result.success(Unit))
            } else {
                // Si falla, llama al callback con la excepción.
                callback(Result.failure(task.exception ?: Exception("Error desconocido")))
            }
        }
}

@Composable
        /**
         * @function PantallaLogin
         * @brief Pantalla principal de inicio de sesión para los usuarios de LocalMunch.
         *
         * * Permite al usuario ingresar credenciales y autenticarse con Firebase.
         *
         * @param onLoginSuccess Callback que se llama al autenticar exitosamente, pasando el email.
         * @param onGoToRegister Callback para navegar a la pantalla de registro.
         */
fun PantallaLogin(onLoginSuccess: (String) -> Unit, onGoToRegister: () -> Unit) {
    // 2. ESTADOS DE LA UI
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    // Mensaje de estado (éxito o error).
    var message by remember { mutableStateOf("") }
    // Estado para alternar la visibilidad de la contraseña.
    var passwordVisible by remember { mutableStateOf(false) }
    // Estado para deshabilitar el botón y mostrar el indicador de carga.
    var isLoading by remember { mutableStateOf(false) }

    // 3. ESTRUCTURA Y DISEÑO
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .background(Color(0xFFF8F8F8)), // Color de fondo suave.
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center // Centra el contenido verticalmente.
    ) {

        // Icono de perfil de bienvenida.
        Card(
            modifier = Modifier
                .padding(16.dp)
                .size(95.dp),
            shape = RoundedCornerShape(50),
            elevation = CardDefaults.cardElevation(8.dp),
            colors = CardDefaults.cardColors(Color.White)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.AccountCircle,
                    contentDescription = null,
                    modifier = Modifier.size(70.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Text("Iniciar Sesión", fontWeight = FontWeight.Bold, fontSize = 28.sp)
        Text("Bienvenido a LM Guía Local", color = Color.Gray, fontSize = 14.sp)

        Spacer(Modifier.height(30.dp))

        // Campo Correo electrónico
        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                message = "" // Limpia el mensaje de error/éxito al empezar a escribir.
            },
            label = { Text("Correo electrónico") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp)),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFEF6C00), // Color de enfoque personalizado.
                unfocusedBorderColor = Color.Gray
            )
        )

        Spacer(Modifier.height(14.dp))

        // Campo Contraseña
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                message = "" // Limpia el mensaje de estado.
            },
            label = { Text("Contraseña") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            trailingIcon = {
                // Botón para alternar la visibilidad.
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = null
                    )
                }
            },
            // Aplica la transformación visual condicionalmente.
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp)),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFEF6C00),
                unfocusedBorderColor = Color.Gray
            )
        )

        Spacer(Modifier.height(24.dp))

        // Botón de Inicio de Sesión
        Button(
            onClick = {
                isLoading = true // Inicia la carga.
                message = ""
                // Llama a la función de autenticación de Firebase.
                signInUser(email, password) { result ->
                    isLoading = false // Detiene la carga al recibir respuesta.
                    if (result.isSuccess) {
                        message = "Bienvenido ${auth.currentUser?.email}"
                        // Llama al callback para navegar tras el éxito.
                        onLoginSuccess(email)
                    } else {
                        // Maneja el error y muestra el mensaje.
                        message = result.exceptionOrNull()?.message ?: "Error desconocido al iniciar sesión."
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(12.dp)),
            // Deshabilita si está cargando o si los campos están vacíos.
            enabled = !isLoading && email.isNotEmpty() && password.isNotEmpty(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF6C00))
        ) {
            if (isLoading) {
                // Muestra un spinner si está cargando.
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White
                )
            } else {
                Text("Entrar", fontSize = 16.sp, color = Color.White)
            }
        }

        Spacer(Modifier.height(16.dp))

        // 4. DISPLAY DEL MENSAJE DE ESTADO
        if (message.isNotEmpty()) {
            Card(
                colors = CardDefaults.cardColors(
                    // Colorea el fondo según el éxito o error (contiene "Bienvenido" si es éxito).
                    containerColor = if (message.contains("Bienvenido"))
                        MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.errorContainer
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Text(
                    message,
                    modifier = Modifier.padding(12.dp),
                    // Colorea el texto según el éxito o error.
                    color = if (message.contains("Bienvenido"))
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        Spacer(Modifier.height(18.dp))

        // Botón para navegar al Registro
        TextButton(onClick = onGoToRegister) {
            Text("¿No tienes cuenta? Regístrate", fontSize = 14.sp)
        }


        Spacer(Modifier.height(32.dp))
        Divider()
        Spacer(Modifier.height(16.dp))

        Text(
            "Para iniciar sesión, usa tu correo y contraseña registrados.",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            modifier = Modifier.padding(horizontal = 16.dp),
            textAlign = TextAlign.Center
        )
    }
}
