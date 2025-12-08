# 🍔 LocalMunch: Guía Local de Lugares de Comida



Aplicación móvil nativa desarrollada en **Android (Kotlin)** usando **Jetpack Compose** para conectar a los usuarios con los mejores lugares de comida locales en Dolores Hidalgo (o la región definida). Permite explorar, calificar y guardar establecimientos favoritos.

---

## 🚀 Características Principales

* **Exploración de Lugares:** Navegación por categorías y listado de lugares populares.
* **Detalles y Reseñas:** Vista detallada de cada local con información de contacto y ubicación.
* **Calificaciones y Reseñas (CRUD):** Los usuarios pueden añadir, ver y eliminar sus propias reseñas y calificaciones.
* **Gestión de Favoritos:** Almacenamiento local de lugares favoritos usando `SharedPreferences`.
* **Mapa Interactivo:** Visualización de la ubicación de los locales usando Google Maps Compose.
* **Panel de Administración:** Acceso protegido para gestionar (CRUD) los datos de los lugares en Firestore.
* **Autenticación:** Gestión de usuarios mediante Firebase Authentication.

---

## 🛠️ Tecnologías Utilizadas

* **Lenguaje:** Kotlin
* **Framework UI:** Jetpack Compose
* **Plataforma:** Android Nativo
* **Base de Datos y Backend:** Firebase
    * **Firestore:** Almacenamiento de datos de Lugares y Reseñas.
    * **Firebase Authentication:** Registro e inicio de sesión de usuarios.
    * **Firebase Cloud Messaging (FCM):** Servicio para notificaciones push.
* **Librerías Clave:**
    * **Kotlin Coroutines & Flow:** Manejo asíncrono y datos reactivos en tiempo real.
    * **Jetpack Navigation:** Gestión del grafo de navegación de la aplicación.
    * **Coil:** Carga eficiente de imágenes asíncronas (URLs).
    * **Google Maps Compose:** Integración de mapas de forma declarativa.

---

## ⚙️ Estructura del Proyecto

El proyecto sigue la arquitectura **MVVM (Model-View-ViewModel)** con el patrón **Repository** para la abstracción de la capa de datos.

* `data/repository/`: Lógica de acceso a datos (ej. `ReseñasRepository` para operaciones CRUD y recálculo de rating en Firestore).
* `models/`: Estructuras de datos (data classes) para Firestore (Lugar, Reseña).
* `viewmodel/`: Lógica de negocio y gestión de estados (ej. `LugaresViewModel`, `ReseñasViewModel`).
* `screens/`: Componentes Composable que representan las pantallas de la UI (la capa de "View").
* `Navigation/`: Definición de rutas y el grafo de navegación.

---

## 🚀 Cómo Ejecutar el Proyecto

Para correr LocalMunch en tu máquina local, necesitas:

1.  **Android Studio** (Versión Hedgehog o superior).
2.  **SDK de Android** (Mínimo API 24/Nought, Target API 34).
3.  **Cuenta de Firebase:** Necesitas configurar un proyecto en Firebase.

### Configuración de Firebase

1.  Crea un nuevo proyecto en la consola de Firebase.
2.  Agrega una aplicación Android al proyecto y sigue las instrucciones para obtener el archivo **`google-services.json`**.
3.  Coloca el archivo `google-services.json` dentro del directorio `app/`.
4.  Habilita los servicios de **Firestore** y **Authentication (Correo/Contraseña y Anónima)** en la consola.
5.  Asegúrate de configurar las reglas de seguridad de Firestore para permitir el acceso (lectura/escritura) necesario para las colecciones `lugares`, `reseñas` y `usuarios`.

### Credenciales de Administrador (Local)

Para acceder al Panel de Administración y gestionar los locales:

| Correo Electrónico | Contraseña |
| :--- | :--- |
| `admin1@localmunch.com` | `password123` |
| `gestor@localmunch.com` | `mypassword` |

---

## 🤝 Contribuciones

Si deseas contribuir, sigue los siguientes pasos:

1.  Haz un *Fork* del repositorio.
2.  Crea una nueva rama (`git checkout -b feature/nueva-funcionalidad`).
3.  Realiza tus cambios y haz *commit* (`git commit -am 'feat: Agrega nueva característica X'`).
4.  Sube la rama (`git push origin feature/nueva-funcionalidad`).
5.  Abre un *Pull Request*.

---

## 📧 Contacto

Desarrollado por: **[Tu Nombre o Equipo de Desarrollo]**

Si tienes preguntas o sugerencias: `localmunch30@gmail.com`

**LocalMunch © 2025**
