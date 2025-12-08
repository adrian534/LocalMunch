package mx.edu.utng.localmunch



import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * @class MyFirebaseMessagingService
 * @brief Servicio para manejar notificaciones push de Firebase Cloud Messaging (FCM).
 *
 * * Se encarga de recibir tokens y mensajes (notificaciones/datos) y crear la notificación visible.
 */
class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "🔔 Nuevo token FCM: $token")
        // Aquí podrías enviar el token a tu servidor si lo necesitas
        // Esto es esencial si quieres enviar notificaciones personalizadas a este dispositivo.
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d(TAG, "📨 Mensaje recibido de: ${remoteMessage.from}")

        // 1. Verificar si el mensaje contiene notificación (payload `notification`)
        remoteMessage.notification?.let {
            Log.d(TAG, "Título: ${it.title}")
            Log.d(TAG, "Cuerpo: ${it.body}")

            // Muestra la notificación usando los campos estándar.
            showNotification(
                title = it.title ?: "LocalMunch",
                body = it.body ?: "Nuevo contenido disponible"
            )
        }

        // 2. Verificar si el mensaje contiene datos (payload `data`)
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Datos del mensaje: ${remoteMessage.data}")
            handleDataMessage(remoteMessage.data) // Llama a la función de manejo de datos personalizados.
        }
    }

    /**
     * @function handleDataMessage
     * @brief Procesa el mapa de datos personalizados enviado a través de FCM.
     *
     * @param data Mapa de pares clave-valor enviados por el servidor.
     */
    private fun handleDataMessage(data: Map<String, String>) {
        // Manejar datos personalizados si los necesitas
        val type = data["type"]

        when (type) {
            "new_place" -> {
                val placeName = data["place_name"] ?: "nuevo lugar"
                showNotification(
                    title = "🆕 Nuevo lugar agregado",
                    body = "Descubre $placeName en LocalMunch"
                )
            }
            else -> {
                // Notificación genérica basada en los datos.
                showNotification(
                    title = data["title"] ?: "LocalMunch",
                    body = data["body"] ?: "Tienes una nueva notificación"
                )
            }
        }
    }

    /**
     * @function showNotification
     * @brief Crea y muestra la notificación visible al usuario.
     *
     * @param title Título de la notificación.
     * @param body Cuerpo (texto) de la notificación.
     */
    private fun showNotification(title: String, body: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Crear canal de notificación para Android 8.0+ (Oreo)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "LocalMunch Notificaciones",
                NotificationManager.IMPORTANCE_HIGH // Importancia alta para alertas.
            ).apply {
                description = "Notificaciones de nuevos lugares y actualizaciones"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Intent para abrir la MainActivity cuando se toca la notificación
        val intent = Intent(this, MainActivity::class.java).apply {
            // Flags para asegurar que la app se abra correctamente.
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            // FLAG_IMMUTABLE es requerido en Android 6.0+.
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Construir la notificación usando NotificationCompat (compatibilidad).
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Reemplaza con tu icono
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true) // Se cierra al tocarla.
            .setContentIntent(pendingIntent) // Acción al tocar.
            .build()

        // Muestra la notificación con un ID único.
        notificationManager.notify(NOTIFICATION_ID++, notification)
    }

    companion object {
        private const val TAG = "FCMService"
        private const val CHANNEL_ID = "localmunch_channel" // ID del canal para Android 8.0+
        private var NOTIFICATION_ID = 1000 // ID incremental para cada notificación.
    }
}
