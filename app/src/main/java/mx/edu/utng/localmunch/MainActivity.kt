package mx.edu.utng.localmunch

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import mx.edu.utng.localmunch.Navigation.NavGraph
import mx.edu.utng.localmunch.Navigation.Screen

import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

/**
 * @class MainActivity
 * @brief Actividad principal de la aplicación LocalMunch.
 *
 * * Se encarga de la inicialización de Firebase y de determinar la ruta de navegación inicial
 * basándose en el estado de autenticación del usuario.
 */
class MainActivity : ComponentActivity() {

    // Instancia de Firebase Authentication.
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializa la configuración de Firebase para la aplicación.
        FirebaseApp.initializeApp(this)
        auth = FirebaseAuth.getInstance()

        // 1. Verificación Estricta de la Sesión
        val currentUser = auth.currentUser

        // 🔑 Lógica de Navegación Inicial:
        // Salta el login solo si el usuario existe Y NO ES ANÓNIMO.
        val shouldSkipLogin = currentUser != null && !currentUser!!.isAnonymous

        val startDestination = if (shouldSkipLogin) {
            // Usuario registrado: va directo a la navegación principal.
            Screen.BottomNav.route
        } else {
            // Usuario nuevo, anónimo o deslogueado: va a la pantalla de Login/Registro.
            Screen.Login.route
        }

        // Si no hay usuario, forzamos la autenticación anónima para que Firestore
        // pueda aplicar las reglas de seguridad basadas en `request.auth != null`.
        if (currentUser == null) {
            authenticateUserAnonymously()
        } else {
            Log.d("MainActivity", "✅ Sesión activa. ¿Anónima?: ${currentUser.isAnonymous}")
        }

        // 2. Configuración de la UI (Jetpack Compose)
        setContent {
            val navController = rememberNavController()
            // Pasa la ruta de inicio condicional al NavGraph para arrancar la navegación.
            NavGraph(
                navController = navController,
                startDestination = startDestination
            )
        }
    }

    /**
     * @function authenticateUserAnonymously
     * @brief Asegura la existencia de un usuario anónimo de Firebase.
     *
     * * Es vital para que la aplicación acceda a datos de solo lectura sin login explícito.
     */
    private fun authenticateUserAnonymously() {
        Log.d("MainActivity", "🔄 Iniciando autenticación anónima...")
        auth.signInAnonymously()
            .addOnSuccessListener {
                Log.d("MainActivity", "✅ ¡Autenticación anónima exitosa!")
            }
            .addOnFailureListener { exception ->
                Log.e("MainActivity", "❌ ERROR en autenticación anónima: ${exception.message}")
            }
    }

    override fun onStart() {
        super.onStart()
        // Loguea el estado del usuario al inicio del ciclo de vida de la actividad.
        Log.d("MainActivity", "🟢 Usuario activo: ${auth.currentUser?.uid ?: "NINGUNO"}")
    }
}
