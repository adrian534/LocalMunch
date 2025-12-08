package mx.edu.utng.localmunch.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.channels.awaitClose
import mx.edu.utng.localmunch.models.Reseña
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * @class ReseñasRepository
 * @brief Maneja todas las operaciones de datos relacionadas con las reseñas (CRUD) en Firestore.
 *
 * * Es responsable de leer, escribir, eliminar reseñas y mantener actualizada
 * la calificación promedio de los 'lugares'.
 */
class ReseñasRepository {

    // Instancia de la base de datos de Firestore.
    private val db = FirebaseFirestore.getInstance()
    // Referencia a la colección principal de reseñas.
    private val reseñasCollection = db.collection("reseñas")
    // Referencia a la colección de lugares (necesaria para actualizar la calificación promedio).
    private val lugaresCollection = db.collection("lugares")

    /**
     * @brief Obtiene una lista de reseñas en tiempo real para un lugar específico.
     *
     * * Utiliza 'callbackFlow' para exponer el SnapshotListener de Firestore como un Kotlin Flow.
     * @param lugarId El ID del lugar cuyas reseñas se desean obtener.
     * @returns Flow<List<Reseña>> Un flujo de la lista de reseñas que se actualiza automáticamente.
     */
    fun getReseñasByLugarId(lugarId: String): Flow<List<Reseña>> = callbackFlow {

        // Crea un listener que observa los cambios en la base de datos.
        val subscription = reseñasCollection
            .whereEqualTo("lugarId", lugarId) // Filtra las reseñas por el ID del lugar.
            .addSnapshotListener { snapshot, e ->
                // Manejo de errores
                if (e != null) {
                    close(e) // Si hay un error, cierra el Flow y lo propaga.
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    // Mapeo: Convierte los Documentos de Firestore a objetos Reseña de Kotlin.
                    val reseñas = snapshot.documents.mapNotNull { document ->
                        // Es vital COPIAR el ID de Firestore al objeto Kotlin si el modelo lo requiere.
                        document.toObject(Reseña::class.java)?.copy(id = document.id)
                    }
                    // Envía la nueva lista de reseñas al Flow.
                    trySend(reseñas).isSuccess
                } else {
                    // Si el snapshot es nulo (aunque raro), envía una lista vacía.
                    trySend(emptyList()).isSuccess
                }
            }
        // Función de limpieza: se ejecuta cuando el Flow ya no se está recolectando (ej. la Activity muere).
        awaitClose {
            subscription.remove() // Detiene el listener de Firestore para evitar fugas de memoria.
        }
    }

    /**
     * @brief Añade una nueva reseña a la colección de Firestore.
     *
     * * Es una función suspendida porque utiliza 'await()' para esperar el resultado de la operación asíncrona.
     * @param reseña El objeto Reseña a guardar (sin ID asignado por el cliente).
     * @returns Boolean True si la operación fue exitosa, False en caso de error.
     */
    suspend fun addReseña(reseña: Reseña): Boolean {
        return try {
            // Asegura que la reseña tenga la fecha actual formateada antes de guardarla.
            val reseñaConFecha = reseña.copy(
                fecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            )
            // Añade el documento y espera a que la operación termine.
            reseñasCollection.add(reseñaConFecha).await()

            // Lógica de negocio crítica: Recalcula la calificación promedio del lugar afectado.
            recalculateAverageRating(reseña.lugarId)
            true
        } catch (e: Exception) {
            // Manejo de error
            false
        }
    }

    /**
     * @brief Borra una reseña específica de la colección y recalcula el promedio del lugar.
     *
     * @param reseñaId El ID del documento de reseña a borrar.
     * @param lugarId El ID del lugar para recalcular su promedio después del borrado.
     * @returns Boolean True si se borró y se recalculó exitosamente.
     */
    suspend fun deleteReseña(reseñaId: String, lugarId: String): Boolean {
        return try {
            // Borra el documento en Firestore.
            reseñasCollection.document(reseñaId).delete().await()
            // Recalcula y actualiza el promedio del lugar.
            recalculateAverageRating(lugarId)
            true
        } catch (e: Exception) {
            println("Error al eliminar reseña en el repositorio: $e")
            false
        }
    }

    /**
     * @brief Obtiene una lista de reseñas en tiempo real escritas por un usuario específico.
     *
     * * Similar a 'getReseñasByLugarId', pero filtra por 'usuarioId'.
     * @param userId El ID del usuario cuyas reseñas se desean obtener (para su perfil).
     * @returns Flow<List<Reseña>> Un flujo de la lista de reseñas del usuario.
     */
    fun getReseñasByUserId(userId: String): Flow<List<Reseña>> = callbackFlow {
        val subscription = reseñasCollection
            .whereEqualTo("usuarioId", userId) // Filtra por el usuario creador.
            .addSnapshotListener { snapshot, e ->

                if (e != null) {
                    close(e) // Maneja el error de Firebase
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val reseñas = snapshot.documents.mapNotNull { document ->
                        // Mapea a Reseña y COPIA EL ID DEL DOCUMENTO.
                        document.toObject(Reseña::class.java)?.copy(id = document.id)
                    }
                    trySend(reseñas).isSuccess // Envía la lista actualizada al Flow
                } else {
                    trySend(emptyList()).isSuccess
                }
            }
        awaitClose { subscription.remove() } // Cierra el listener al terminar
    }


    /**
     * @brief Recalcula la calificación promedio de un lugar después de añadir/borrar una reseña.
     *
     * * Esta función lee TODAS las reseñas de un lugar, calcula la media y actualiza el documento del lugar.
     * @param lugarId El ID del lugar cuya calificación promedio debe ser actualizada.
     */
    private suspend fun recalculateAverageRating(lugarId: String) {
        try {
            // Obtiene todas las reseñas actuales del lugar de forma asíncrona.
            val snapshot = reseñasCollection
                .whereEqualTo("lugarId", lugarId)
                .get()
                .await()

            if (snapshot.documents.isNotEmpty()) {
                // Suma todas las calificaciones. Se usa getDouble por si el campo es de tipo numérico.
                val totalCalificaciones = snapshot.documents.sumOf {
                    // Si 'calificacion' es un Long o Int en Firestore, se convierte a Double para la suma.
                    it.getLong("calificacion")?.toDouble() ?: 0.0
                }

                val numReseñas = snapshot.documents.size.toDouble()

                // Cálculo del promedio, evitando división por cero.
                val nuevoPromedio = if (numReseñas > 0) totalCalificaciones / numReseñas else 0.0

                // 3. Actualizar el campo 'calificacion' en el documento del lugar.
                // Formateo a un decimal y conversión a Double para almacenar un valor limpio.
                lugaresCollection.document(lugarId).update(
                    "calificacion", String.format(Locale.US, "%.1f", nuevoPromedio).toDouble()
                ).await()
            } else {
                // Si ya no hay reseñas, restablece la calificación promedio a 0.0.
                lugaresCollection.document(lugarId).update("calificacion", 0.0).await()
            }
        } catch (e: Exception) {
            // Manejo de error en el proceso de recálculo (puede ser un log en un proyecto real).
        }
    }
}
