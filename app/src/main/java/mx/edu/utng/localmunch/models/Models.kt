package mx.edu.utng.localmunch.models

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude

/**
 * @data class Lugar
 * @brief Modelo principal para representar un lugar de comida local en la aplicación.
 *
 * * Estos campos corresponden a los documentos almacenados en la colección 'lugares' de Firestore.
 */
data class Lugar(
    // Anotación especial para asignar automáticamente el ID del documento de Firestore a esta propiedad.
    @DocumentId var id: String? = null,

    val nombre: String = "", // Nombre del restaurante.
    val descripcion: String = "", // Descripción general o slogan.
    val categoria: String = "", // Categoría principal (e.g., "Tacos", "Cafetería").
    val direccion: String = "", // Dirección física completa.
    val telefono: String = "", // Número de contacto.
    val horario: String = "", // Horario de atención (e.g., "Lun-Vie 9am-8pm").
    val precioPromedio: String = "", // Rango de precios (e.g., "$$", "$$$").
    // Calificación promedio calculada por el repositorio (ReseñasRepository).
    val calificacion: Double = 0.0,

    // 🔑 URL de la imagen principal del lugar.
    val imagenUrl: String? = null,

    val latitud: Double = 0.0, // Coordenada Latitud para mapas.
    val longitud: Double = 0.0, // Coordenada Longitud para mapas.
    // Bandera para indicar si el lugar debe ser visible para el usuario final.
    val esOculto: Boolean = false
)

// --- 2. MODELO RESEÑA ---
/**
 * @data class Reseña
 * @brief Modelo para las reseñas (calificaciones y comentarios) de los lugares.
 * Contiene el ID del usuario que la creó (usuarioId) para permitir el borrado condicional.
 */
data class Reseña(
    // ID del documento de la reseña en Firestore.
    @DocumentId val id: String? = null,
    val lugarId: String = "", // ID del lugar al que pertenece esta reseña (clave de unión).
    // 🔑 ID del usuario logueado. Necesario para mostrar solo las reseñas propias o permitir el borrado.
    val usuarioId: String = "",
    val usuarioNombre: String = "", // Nombre del usuario que escribió la reseña (para visualización).
    val comentario: String = "", // Texto del comentario.
    val calificacion: Int = 0, // Calificación numérica dada (1 a 5).
    val fecha: String = "" // Fecha en formato String (ej: "yyyy-MM-dd").
)


// --- 3. MODELO CATEGORIA ---
/**
 * @data class Categoria
 * @brief Modelo para definir las categorías de lugares (e.g., Tacos, Pizzas, Desayunos).
 */
data class Categoria(
    val id: Int, // Identificador único de la categoría.
    val nombre: String, // Nombre de la categoría (e.g., "Tacos").
    val icono: Int, // Referencia (Drawable ID) al icono que representa la categoría en la UI.
    val descripcion: String // Descripción breve de la categoría.
)


// --- 4. DATA SOURCE (Para compatibilidad con código antiguo y listas) ---
/**
 * @object DataSource
 * @brief Objeto para mantener listas de datos estáticos o de prueba, o inicialización de datos.
 *
 * * En aplicaciones modernas, esto se suele reemplazar por Repositorios que cargan datos dinámicos.
 */
object DataSource {

    // Listas vacías usando los modelos, listas de datos de ejemplo o pre-cargados.
    val lugares = listOf<Lugar>()
    val reseñas = mutableListOf<Reseña>()
    val categorias = listOf<Categoria>()
}
