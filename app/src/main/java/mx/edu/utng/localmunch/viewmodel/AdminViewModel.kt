package mx.edu.utng.localmunch.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import android.util.Log

/**
 * @class AdminViewModel
 * @brief ViewModel dedicado a manejar la lógica de la sesión de administrador,
 * principalmente el cierre de sesión con Firebase Authentication.
 */
class AdminViewModel : ViewModel() {

    // 1. INSTANCIA DE FIREBASE AUTH
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    /**
     * @function cerrarSesion
     * @brief Cierra la sesión del usuario actualmente autenticado en Firebase.
     *
     * * Es una operación de una sola vía (no requiere corrutina).
     */
    fun cerrarSesion() {
        Log.d("AdminViewModel", "Cerrando sesión de administrador...")
        try {
            // Llama al método estándar de Firebase para cerrar la sesión.
            auth.signOut()
            Log.d("AdminViewModel", "✅ Sesión de Firebase cerrada exitosamente.")
        } catch (e: Exception) {
            // Captura cualquier error que pueda ocurrir durante el proceso (ej. inicialización fallida).
            Log.e("AdminViewModel", "❌ Error al cerrar sesión: ${e.message}")
        }
    }
}
