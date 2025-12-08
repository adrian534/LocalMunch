package mx.edu.utng.localmunch.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mx.edu.utng.localmunch.data.repository.ReseñasRepository
import mx.edu.utng.localmunch.models.Reseña

/**
 * @class ReseñasViewModel
 * @brief ViewModel que gestiona la lógica de negocio y el estado reactivo de las reseñas
 * para un único lugar (`lugarId`) específico.
 *
 * @param lugarId ID del lugar cuyas reseñas se están manejando.
 * @param repository Instancia del Repositorio de Reseñas.
 */
class ReseñasViewModel(
    private val lugarId: String,
    private val repository: ReseñasRepository
) : ViewModel() {

    // 1. INSTANCIAS
    private val auth = FirebaseAuth.getInstance()

    // 2. ESTADO REACTIVO DE RESEÑAS
    // Estado que mantiene la lista de reseñas para la pantalla de Reseñas
    private val _reseñas = MutableStateFlow<List<Reseña>>(emptyList())
    // StateFlow público que la UI observará.
    val reseñas: StateFlow<List<Reseña>> = _reseñas.asStateFlow()

    init {
        // Inicia la observación en tiempo real al crear el ViewModel.
        loadReseñas()
    }

    /**
     * @function loadReseñas
     * @brief Inicia la recolección del Flow de reseñas para el `lugarId` actual,
     * manteniendo la lista de reseñas actualizada en tiempo real.
     */
    private fun loadReseñas() {
        viewModelScope.launch {
            // Recolecta el Flow del repositorio. Cada cambio en Firebase actualiza `_reseñas`.
            repository.getReseñasByLugarId(lugarId).collect { nuevaLista ->
                _reseñas.update { nuevaLista }
            }
        }
    }

    /**
     * @function getReseñasByLugarId
     * @brief Expone el Flow del Repositorio directamente para que los componentes puedan
     * obtener el contador de reseñas en tiempo real.
     *
     * @param lugarId ID del lugar.
     * @returns Flow<List<Reseña>> El flujo de reseñas.
     */
    fun getReseñasByLugarId(lugarId: String): Flow<List<Reseña>> {
        return repository.getReseñasByLugarId(lugarId)
    }


    /**
     * @function addReseña
     * @brief Agrega una nueva reseña a Firestore con los datos del usuario logueado.
     *
     * @param comentario Texto del comentario.
     * @param calificacion Puntuación de 1 a 5.
     */
    fun addReseña(comentario: String, calificacion: Int) {
        viewModelScope.launch {
            val user = auth.currentUser
            // ⚠️ Verificación de autenticación.
            if (user == null || user.uid.isBlank()) {
                println("Error: El usuario no está logueado o no tiene UID.")
                return@launch
            }

            // Define el nombre a mostrar.
            val userName = if (user.displayName.isNullOrBlank()) {
                "Usuario Anónimo (${user.uid.take(4)})" // Placeholder si no hay display name.
            } else {
                user.displayName!!
            }

            // Crea el objeto Reseña.
            val nuevaReseña = Reseña(
                lugarId = lugarId,
                usuarioId = user.uid,
                usuarioNombre = userName,
                comentario = comentario,
                calificacion = calificacion
            )

            val exito = repository.addReseña(nuevaReseña)
            if (!exito) {
                // ⚠️ Manejar el error de Firestore.
                println("Error: No se pudo agregar la reseña a Firestore.")
            }
            // NOTA: No es necesario actualizar _reseñas.value aquí, ya que el loadReseñas (SnapshotListener)
            // lo actualizará automáticamente.
        }
    }

    /**
     * @function deleteReseña
     * @brief Elimina la reseña y delega el recálculo del promedio al Repositorio.
     *
     * @param reseñaId ID del documento de reseña a eliminar.
     */
    fun deleteReseña(reseñaId: String) {
        viewModelScope.launch {
            // Llama al repositorio para eliminar la reseña y recalcular el rating del lugar.
            repository.deleteReseña(reseñaId, lugarId)
        }
    }
}

// -------------------------------------------------------------------------------------------------
// ⭐️ FÁBRICA DE VIEWMODEL (ViewModelProvider.Factory)
// -------------------------------------------------------------------------------------------------
/**
 * @class ReseñasViewModelFactory
 * @brief Fábrica necesaria para crear el ReseñasViewModel, ya que requiere argumentos
 * en su constructor (lugarId).
 *
 * * Permite la inyección segura de dependencias (`lugarId` y `ReseñasRepository`).
 */
class ReseñasViewModelFactory(private val lugarId: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReseñasViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            // Crea el ViewModel con el lugarId y una instancia del Repository.
            return ReseñasViewModel(lugarId, ReseñasRepository()) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
