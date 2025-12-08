package mx.edu.utng.localmunch.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.edu.utng.localmunch.data.repository.ReseñasRepository
import mx.edu.utng.localmunch.models.Reseña

/**
 * @class MisReseñasViewModel
 * @brief ViewModel que gestiona la lógica de negocio y el estado reactivo de las reseñas
 * escritas exclusivamente por el usuario actualmente logueado.
 *
 * @param repository Instancia del Repositorio de Reseñas para interactuar con Firestore.
 */
class MisReseñasViewModel(
    private val repository: ReseñasRepository = ReseñasRepository()
) : ViewModel() {

    // 1. OBTENER ID DEL USUARIO
    private val auth = FirebaseAuth.getInstance()
    // Obtiene el ID del usuario actual. Si es nulo, el usuario no está logueado.
    private val userId = auth.currentUser?.uid

    // 2. ESTADO REACTIVO DE LAS RESEÑAS
    val misReseñas: StateFlow<List<Reseña>> = if (userId != null) {
        // Si hay un ID de usuario: Obtiene un Flow de reseñas filtradas por ese ID desde el repositorio.
        repository.getReseñasByUserId(userId)
            .stateIn(
                scope = viewModelScope,
                // Inicia la recolección cuando la UI se suscribe y la detiene 5s después de la última suscripción.
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
                initialValue = emptyList()
            )
    } else {
        // Si no hay ID de usuario: Devuelve un StateFlow vacío, asegurando el tipo correcto.
        MutableStateFlow<List<Reseña>>(emptyList())
            .asStateFlow()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = emptyList()
            )
    }

    /**
     * @function deleteReseña
     * @brief Elimina una reseña específica del repositorio.
     *
     * * Esta acción debe llevarse a cabo en una corrutina y también dispara el recálculo
     * de la calificación promedio del lugar asociado (lógica dentro del Repository).
     *
     * @param reseñaId ID de la reseña a eliminar.
     * @param lugarId ID del lugar asociado (necesario para el recálculo del rating).
     */
    fun deleteReseña(reseñaId: String, lugarId: String) {
        viewModelScope.launch {
            repository.deleteReseña(reseñaId, lugarId)
        }
    }
}
