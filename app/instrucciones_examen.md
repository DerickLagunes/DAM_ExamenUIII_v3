# Examen Práctico: Aplicación Avistamientos de Ovnis

> **Objetivo:** Completar todas las secciones marcadas con comentarios `TODO` en el código fuente para resolver los errores de compilación y hacer que la aplicación **Avistamientos** funcione correctamente tanto en modo offline como online.

---

## Resumen de Secciones a Completar (`TODO`)

A continuación se detallan los 8 archivos del proyecto que contienen comentarios `TODO` y lo que debes completar en cada uno:

---

### 1. `app/src/main/AndroidManifest.xml`
**Objetivo:** Declarar el uso del sensor Acelerometro, los permisos requeridos y arreglar el provedoor de archivos.

---

### 2. `data/local/entity/AvistamientoEntity.kt`
**Objetivo:** Agregar los campos faltantes de la tabla `avistamientos` en la entidad Room.

- **Campos a agregar:**
  ```kotlin
  latitud: Double,
  longitud: Double,
  id: Int (agregar autogeneración de la llave primaria)
  ```

---

### 3. `data/local/dao/AvistamientoDao.kt`
**Objetivo:** Escribe la consulta (observeAvistamientoById) y (updateAvistamiento) con Room SQL para obtener un avistamiento por id pasado como parametro.

- **observeAvistamientoById**
- **updateAvistamiento**

---

### 4. `data/remote/ApiService.kt`
**Objetivo:** Agregar la etiqueta para manejar datos multiparte en el envio de datos del formulario. Posteriormente agrega las partes del formulario considerando:

```kotlin
  user_matricula
  titulo
  lugar
  latitud
  longitud
  descripcion
  forma_ovni(opcional)
  timestamp
```

(todos los datos son de tipo RequestBody)


---

### 5. `data/remote/RetrofitClient.kt`
**Objetivo:** Configurar la dirección IP correcta del servidor backend de Flask.

- **IP Base URL, segun lo que mencione el profesor**

---

### 6. `data/repository/AvistamientoRepository.kt`
**Objetivo:** Inserta en la base de datos local la Entidad de avistamiento registrada y recupera su "localID" para buscarlo posteriormente en linea val savedEntity = ...


---

### 7. `ui/components/AvistamientoCard.kt`
**Objetivo:** Renderizar el icono de estado desincronizado (`CloudOff`) en Compose cuando `isSynced` sea `false`.


---

### 8. `ui/components/ShakeDetector.kt`
**Objetivo:** Completa el algoritmo accediendo a los datos del acelerometrico segun el sentido de la variable: x, y o z. (nota solo reemplaza los simbolos de "?"


---

## Pasos para la Verificación del Examen

**Prueba de Flujo Principal:**
   - Registra/Inicia sesión con una matrícula de prueba.
   - Crea un avistamiento de Ovni especificando Título, Forma del Ovni, Lugar y Descripción.
   - Agita el dispositivo (en emulador usa `Extended Controls > Sensors > Accelerator`) para verificar la actualización automática mediante el acelerómetro.
