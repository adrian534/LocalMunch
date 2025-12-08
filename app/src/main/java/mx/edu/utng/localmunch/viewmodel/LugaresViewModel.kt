package mx.edu.utng.localmunch.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import mx.edu.utng.localmunch.models.Lugar // Asegúrate de tener este modelo importado

/**
 * @class LugaresViewModel
 * @brief ViewModel principal para gestionar el estado y la lógica de negocio de los Lugares.
 *
 * * Utiliza Kotlin Flow y Firebase SnapshotListeners para proveer una lista de lugares
 * que se actualiza en tiempo real (Realtime Data).
 */
class LugaresViewModel : ViewModel() {

    // 1. REFERENCIAS A FIRESTORE
    private val db = FirebaseFirestore.getInstance()
    private val lugaresCollection = db.collection("lugares")

    // 2. GESTIÓN DE ESTADO REACTIVO (LUGARES)
    // MutableStateFlow privado para la lista de todos los lugares.
    private val _lugares = MutableStateFlow<List<Lugar>>(emptyList())
    // StateFlow público para que la UI observe la lista de lugares.
    val lugares: StateFlow<List<Lugar>> = _lugares.asStateFlow()

    // Estado para indicar si los datos iniciales están cargando.
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // 3. GESTIÓN DE ESTADO REACTIVO (FAVORITOS LOCALES)
    // Mantener la lógica de favoritos separada de Firebase por ahora
    private val _lugaresFavoritos = MutableStateFlow<List<Lugar>>(emptyList())
    val lugaresFavoritos: StateFlow<List<Lugar>> = _lugaresFavoritos.asStateFlow()
    // Conjunto de IDs de favoritos (gestionado internamente por el ViewModel/App localmente).
    private val _favoritosIds = MutableStateFlow<MutableSet<String>>(mutableSetOf())

    init {
        // ⭐️ Inicializa la carga en tiempo real al crear el ViewModel.
        loadLugaresRealtime()
    }

    // -----------------------------------------------------------------
    //          CARGAR LUGARES DESDE FIREBASE (TIEMPO REAL)
    // -----------------------------------------------------------------
    /**
     * @function loadLugaresRealtime
     * @brief Lanza una corrutina para recolectar el Flow de lugares de Firebase.
     */
    private fun loadLugaresRealtime() {
        viewModelScope.launch {
            _isLoading.value = true
            // Recolecta los cambios del listener de Firestore.
            lugaresFlow().collect { lista ->
                _lugares.value = lista // Actualiza el StateFlow de la lista principal.
                actualizarListaFavoritos() // Actualiza los favoritos si la lista principal cambia.
                _isLoading.value = false
            }
        }
    }

    /**
     * @function lugaresFlow
     * @brief Crea un Kotlin Flow a partir del `addSnapshotListener` de Firestore.
     *
     * * Es el mecanismo que proporciona actualizaciones de datos en tiempo real.
     * @returns Flow<List<Lugar>> Un flujo de la lista de lugares.
     */
    private fun lugaresFlow(): Flow<List<Lugar>> = callbackFlow {
        // Crea el listener de Firestore.
        val subscription = lugaresCollection.addSnapshotListener { snapshot, e ->
            if (e != null) {
                close(e) // Cierra el Flow si hay un error en Firebase.
                return@addSnapshotListener
            }

            if (snapshot != null) {
                // Mapeo de documentos de Firebase a objetos Lugar.
                val lista = snapshot.documents.mapNotNull { doc ->
                    try {
                        Lugar(
                            id = doc.id,
                            nombre = doc.getString("nombre") ?: "",
                            categoria = doc.getString("categoria") ?: "",
                            descripcion = doc.getString("descripcion") ?: "",
                            direccion = doc.getString("direccion") ?: "",
                            telefono = doc.getString("telefono") ?: "",
                            horario = doc.getString("horario") ?: "",
                            precioPromedio = doc.getString("precioPromedio") ?: "",
                            // ⭐ Conversión segura de tipos de Firestore a Kotlin.
                            calificacion = doc.getDouble("calificacion") ?: 0.0,
                            latitud = doc.getDouble("latitud") ?: 0.0,
                            longitud = doc.getDouble("longitud") ?: 0.0,
                            imagenUrl = doc.getString("imagenUrl") ?: "",
                            esOculto = doc.getBoolean("esOculto") ?: false
                        )
                    } catch (e: Exception) {
                        Log.e("LugaresViewModel", "Error al mapear Lugar: $e")
                        null
                    }
                }
                trySend(lista).isSuccess // Envía la lista actualizada al Flow.
            } else {
                trySend(emptyList()).isSuccess
            }
        }
        // Función de limpieza: se ejecuta cuando el Flow se detiene.
        awaitClose { subscription.remove() }
    }

    /**
     * @function getLugarFlowById
     * @brief Proporciona un Flow de un solo lugar, ideal para pantallas de detalle que necesitan
     * actualizarse si solo cambia el rating, por ejemplo.
     *
     * @param lugarId ID del lugar a observar.
     * @returns Flow<Lugar?> Un flujo de objeto Lugar (o nulo).
     */
    fun getLugarFlowById(lugarId: String): Flow<Lugar?> = callbackFlow {
        val lugarRef = lugaresCollection.document(lugarId)

        val subscription = lugarRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                close(e)
                return@addSnapshotListener
            }
            // Mapea el snapshot del documento a un objeto Lugar, copiando el ID.
            val lugar = snapshot?.toObject(Lugar::class.java)?.copy(id = snapshot.id)
            trySend(lugar).isSuccess
        }
        awaitClose { subscription.remove() }
    }


    // -----------------------------------------------------------------
    //                 REFRESH EXTERNO (ADMIN)
    // -----------------------------------------------------------------
    /**
     * @function refreshLugares
     * @brief Función de actualización manual (principalmente para fines de prueba).
     */
    fun refreshLugares() {
        loadLugaresRealtime()
    }

    // -----------------------------------------------------------------
    //                     AGREGAR / EDITAR LUGAR (CRUD)
    // -----------------------------------------------------------------
    /**
     * @function saveLugar
     * @brief Guarda o actualiza un lugar en Firestore.
     *
     * @param lugar El objeto Lugar con los datos.
     * @param imageUri URI de la imagen (actualmente no utilizada en esta función, se asume URL directa).
     * @returns String? El URL de la imagen (o nulo en caso de error).
     */
    suspend fun saveLugar(lugar: Lugar, imageUri: Uri?): String? {
        return try {
            // Crea el mapa de datos a enviar a Firestore.
            val lugarData = hashMapOf(
                "nombre" to lugar.nombre,
                "categoria" to lugar.categoria,
                "descripcion" to lugar.descripcion,
                "direccion" to lugar.direccion,
                "telefono" to lugar.telefono,
                "horario" to lugar.horario,
                "precioPromedio" to lugar.precioPromedio,
                "calificacion" to lugar.calificacion,
                "latitud" to lugar.latitud,
                "longitud" to lugar.longitud,
                "imagenUrl" to (lugar.imagenUrl ?: ""),
                "esOculto" to lugar.esOculto
            )

            // Lógica de Edición vs. Adición
            if (!lugar.id.isNullOrBlank()) {
                // Si tiene ID, actualiza el documento existente.
                db.collection("lugares").document(lugar.id!!).set(lugarData).await()
            } else {
                // Si no tiene ID, añade un nuevo documento.
                db.collection("lugares").add(lugarData).await()
            }

            // Los cambios se reflejarán automáticamente gracias al SnapshotListener.
            lugar.imagenUrl

        } catch (e: Exception) {
            Log.e("LugaresViewModel", "Error al guardar", e)
            throw e
        }
    }

    // -----------------------------------------------------------------
    //                      ELIMINAR LUGAR (ADMIN) (CRUD)
    // -----------------------------------------------------------------
    /**
     * @function deleteLugar
     * @brief Elimina un lugar de Firestore.
     *
     * @param lugarId ID del documento a eliminar.
     */
    fun deleteLugar(lugarId: String) {
        viewModelScope.launch {
            try {
                db.collection("lugares").document(lugarId).delete().await()
                // El listener de Flow se encargará de actualizar la UI.
            } catch (e: Exception) {
                Log.e("LugaresViewModel", "Error al eliminar", e)
            }
        }
    }

    // -----------------------------------------------------------------
    //                      FAVORITOS (LÓGICA LOCAL)
    // -----------------------------------------------------------------
    /**
     * @function toggleFavorito
     * @brief Alterna el estado de favorito (solo gestiona el estado local de IDs).
     */
    fun toggleFavorito(lugarId: String) {
        val set = _favoritosIds.value.toMutableSet()

        if (set.contains(lugarId)) set.remove(lugarId)
        else set.add(lugarId)

        _favoritosIds.value = set
        actualizarListaFavoritos()
    }

    /**
     * @function actualizarListaFavoritos
     * @brief Filtra la lista principal de lugares basándose en los IDs de favoritos actuales.
     */
    private fun actualizarListaFavoritos() {
        val favoritos = _lugares.value.filter { it.id in _favoritosIds.value }
        _lugaresFavoritos.value = favoritos
    }
}
