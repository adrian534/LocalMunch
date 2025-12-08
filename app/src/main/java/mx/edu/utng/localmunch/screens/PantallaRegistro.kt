package mx.edu.utng.localmunch.screens

import androidx.compose.foundation.clickable
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
import com.google.firebase.Timestamp

// Instancias de Firebase Auth y Firestore (deben estar fuera del Composable)
private val auth: FirebaseAuth = Firebase.auth
private val db: FirebaseFirestore = Firebase.firestore

@Composable
fun PantallaRegistro(
    onRegistrationSuccess: (String) -> Unit, // 🔑 callback 1
    onGoToLogin: () -> Unit // 🔑 callback 2
){
    // Variables de estado para los campos del formulario
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordConfirm by remember { mutableStateOf("") }

    // Variables de estado para UI
    var message by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(Modifier.height(32.dp))
        Text("Crear Cuenta", fontWeight = FontWeight.Bold, fontSize = 32.sp)
        Text("Únete a LM Guía Local", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(32.dp))

        // 1. Campo Nombre
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nombre Completo") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(Modifier.height(12.dp))

        // 2. Campo Correo
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo electrónico") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(Modifier.height(12.dp))

        // 3. Campo Contraseña
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña (mín. 6 caracteres)") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(Modifier.height(12.dp))

        // 4. Campo Confirmar Contraseña
        OutlinedTextField(
            value = passwordConfirm,
            onValueChange = { passwordConfirm = it },
            label = { Text("Confirmar Contraseña") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = password.isNotEmpty() && passwordConfirm.isNotEmpty() && password != passwordConfirm
        )

        Spacer(Modifier.height(24.dp))

        // Botón de REGISTRO
        Button(
            onClick = {
                isLoading = true
                message = ""
                handleRegistration(name, email, password, passwordConfirm) { result ->
                    isLoading = false
                    if (result.isSuccess) {
                        message = "¡Registro exitoso! Bienvenido ${auth.currentUser?.email}"
                        onRegistrationSuccess(email)
                    } else {
                        message = result.exceptionOrNull()?.message ?: "Error desconocido al registrarse."
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            enabled = !isLoading && name.isNotEmpty() && email.isNotEmpty() && password.length >= 6 && password == passwordConfirm
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Registrarme", fontSize = 16.sp)
            }
        }

        Spacer(Modifier.height(16.dp))

        // ... (MENSAJE DE ESTADO)
        if (message.isNotEmpty()) { /* ... */ }
        Spacer(Modifier.height(24.dp))

        // Link para ir a Iniciar Sesión
        Row {
            Text("¿Ya tienes una cuenta? ")
            Text(
                "Inicia Sesión",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable(onClick = onGoToLogin)
            )
        }
    }
}

// --------------------------------------------------------------------------------------
// FUNCIONES DE REGISTRO Y FIRESTORE (DEBEN ESTAR FUERA DEL COMPOSABLE)
// --------------------------------------------------------------------------------------
fun handleRegistration(
    name: String, email: String, password: String, passwordConfirm: String, onComplete: (Result<Unit>) -> Unit
) {
    if (name.isBlank() || email.isBlank() || password.isBlank() || passwordConfirm.isBlank()) {
        onComplete(Result.failure(Exception("Todos los campos son obligatorios."))); return
    }
    if (password != passwordConfirm) { onComplete(Result.failure(Exception("Las contraseñas no coinciden."))); return }
    if (password.length < 6) { onComplete(Result.failure(Exception("La contraseña debe tener al menos 6 caracteres."))); return }

    auth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener { authTask ->
            if (authTask.isSuccessful) {
                val uid = auth.currentUser?.uid
                if (uid != null) {
                    saveUserProfileToFirestore(uid, name, email) { firestoreResult ->
                        if (firestoreResult.isSuccess) { onComplete(Result.success(Unit)) }
                        else { onComplete(Result.failure(firestoreResult.exceptionOrNull() ?: Exception("Registro exitoso, pero falló al guardar perfil."))) }
                    }
                } else { onComplete(Result.failure(Exception("Error interno: UID no disponible."))) }
            } else { onComplete(Result.failure(authTask.exception ?: Exception("Error en la autenticación."))) }
        }
}


fun saveUserProfileToFirestore(
    userId: String, name: String, email: String, onComplete: (Result<Unit>) -> Unit
) {
    val userProfile = hashMapOf(
        "uid" to userId, "email" to email, "nombre" to name, "rol" to "cliente", "fechaCreacion" to Timestamp.now()
    )
    db.collection("usuarios").document(userId).set(userProfile)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) { onComplete(Result.success(Unit)) }
            else { onComplete(Result.failure(task.exception ?: Exception("Error al guardar perfil en Firestore."))) }
        }
}
