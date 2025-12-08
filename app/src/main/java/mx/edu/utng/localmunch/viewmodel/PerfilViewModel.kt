package mx.edu.utng.localmunch.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * @data class PerfilState
 * @brief Define la estructura de datos que representa el estado del perfil de autenticación
 * y que será observada por la UI.
 */
data class PerfilState(
    val email: String? = "Cargando...", // Correo del usuario.
    val estaLogueado: Boolean = false, // Indica si hay un usuario autenticado.
    val esAnonimo: Boolean = true // Indica si es una sesión anónima (si aplica).
)

/**
 * @class PerfilViewModel
 * @brief ViewModel que gestiona el estado de autenticación del usuario, observando a Firebase Auth
 * y exponiendo el estado a la UI a través de StateFlow.
 */
class PerfilViewModel : ViewModel() {

    // 🔑 Instancia de Firebase Auth
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // 🔄 Estado mutable (privado): Es el que el ViewModel modifica.
    private val _state = MutableStateFlow(PerfilState())

    // 🔒 Estado inmutable (público): La UI se suscribe a este StateFlow.
    val state: StateFlow<PerfilState> = _state.asStateFlow()

    init {
        // Inicializar el estado al cargar el ViewModel
        verificarEstadoAutenticacion()
    }

    /**
     * @function verificarEstadoAutenticacion
     * @brief Revisa el usuario actual de Firebase y actualiza el StateFlow con su información.
     */
    private fun verificarEstadoAutenticacion() {
        val user = auth.currentUser
        // Usa `update` para modificar el valor del StateFlow de forma atómica.
        _state.update { currentState ->
            currentState.copy(
                email = user?.email ?: if (user?.isAnonymous == true) "Anónimo" else null,
                estaLogueado = user != null,
                esAnonimo = user?.isAnonymous == true
            )
        }
    }

    /**
     * @function cerrarSesion
     * @brief Cierra la sesión del usuario en Firebase y actualiza el StateFlow.
     */
    fun cerrarSesion() {
        // Ejecuta la operación de cierre de sesión.
        auth.signOut()
        viewModelScope.launch {
            // Después de cerrar sesión, actualiza el estado a no logueado.
            _state.update {
                PerfilState(
                    email = null,
                    estaLogueado = false,
                    esAnonimo = false // Asumimos que el cierre de sesión saca al usuario.
                )
            }
            // NOTA: La navegación de la app a la pantalla de Login debe ocurrir en el Composable.
        }
    }
}
