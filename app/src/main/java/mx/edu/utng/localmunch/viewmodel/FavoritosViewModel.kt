package mx.edu.utng.localmunch.viewmodel


import android.content.Context
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * @class FavoritosViewModel
 * @brief ViewModel para gestionar el estado de los lugares favoritos del usuario
 * utilizando SharedPreferences para la persistencia local.
 *
 * * Expone el estado de favoritos como un Kotlin StateFlow, haciéndolo reactivo.
 */
class FavoritosViewModel : ViewModel() {

    // 1. GESTIÓN DE ESTADO REACTIVO (FLOW)
    // MutableStateFlow privado para actualizar la lista de IDs de lugares favoritos.
    private val _favoritos = MutableStateFlow<Set<String>>(emptySet())
    // StateFlow público (inmutable) para que la UI pueda observar los cambios.
    val favoritos: StateFlow<Set<String>> = _favoritos.asStateFlow()

    // Constantes para SharedPreferences
    private val PREFS_NAME = "LocalMunchFavoritos"
    private val KEY_FAVORITOS = "favoritos"

    /**
     * @function cargarFavoritos
     * @brief Carga los IDs de favoritos desde SharedPreferences al iniciar la aplicación.
     *
     * @param context Contexto de la aplicación requerido para acceder a SharedPreferences.
     */
    fun cargarFavoritos(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        // Lee el conjunto de Strings de SharedPreferences (usa un Set vacío por defecto).
        val favoritosString = prefs.getStringSet(KEY_FAVORITOS, emptySet()) ?: emptySet()
        // Actualiza el StateFlow, lo que notifica a la UI.
        _favoritos.value = favoritosString
    }

    /**
     * @function agregarFavorito
     * @brief Agrega un Lugar al conjunto de favoritos y persiste el cambio.
     *
     * @param context Contexto de la aplicación.
     * @param lugarId ID del lugar a añadir.
     */
    fun agregarFavorito(context: Context, lugarId: String) {
        // Se crea una copia mutable para la modificación.
        val nuevosFavoritos = _favoritos.value.toMutableSet()
        nuevosFavoritos.add(lugarId)
        _favoritos.value = nuevosFavoritos
        guardarFavoritos(context, nuevosFavoritos) // Persistencia inmediata.
    }

    /**
     * @function eliminarFavorito
     * @brief Elimina un Lugar del conjunto de favoritos y persiste el cambio.
     *
     * @param context Contexto de la aplicación.
     * @param lugarId ID del lugar a eliminar.
     */
    fun eliminarFavorito(context: Context, lugarId: String) {
        val nuevosFavoritos = _favoritos.value.toMutableSet()
        nuevosFavoritos.remove(lugarId)
        _favoritos.value = nuevosFavoritos
        guardarFavoritos(context, nuevosFavoritos) // Persistencia inmediata.
    }

    /**
     * @function toggleFavorito
     * @brief Alterna el estado de un lugar: si es favorito, lo elimina; si no lo es, lo agrega.
     *
     * @param context Contexto de la aplicación.
     * @param lugarId ID del lugar.
     */
    fun toggleFavorito(context: Context, lugarId: String) {
        if (esFavorito(lugarId)) {
            eliminarFavorito(context, lugarId)
        } else {
            agregarFavorito(context, lugarId)
        }
    }

    /**
     * @function esFavorito
     * @brief Verifica si un lugar ya existe en el conjunto de favoritos.
     *
     * @param lugarId ID del lugar a verificar.
     * @returns Boolean True si está en la lista, False si no lo está.
     */
    fun esFavorito(lugarId: String): Boolean {
        return _favoritos.value.contains(lugarId)
    }

    /**
     * @function guardarFavoritos
     * @brief Función privada que escribe el conjunto actualizado de IDs en SharedPreferences.
     *
     * @param context Contexto de la aplicación.
     * @param favoritos El conjunto de IDs a guardar.
     */
    private fun guardarFavoritos(context: Context, favoritos: Set<String>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        // Edita las preferencias y usa apply() para la escritura asíncrona.
        prefs.edit().putStringSet(KEY_FAVORITOS, favoritos).apply()
    }
}
