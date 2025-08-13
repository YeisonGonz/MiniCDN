# Documentacion de MiniCDN

### 1. Introducción

Breve descripción del propósito del módulo:
Este proyecto implementa un servicio CDN (Content Delivery Network) básico para almacenar, servir y gestionar imágenes.
Permite:

- Subir fotos a un almacenamiento local.

- Consultarlas mediante una URL pública.

- Establecer fechas de expiración.

- Integración con Redis para control de caché.

- Consultar todas las fotos en formato JSON o HTML.

### 2. Arquitectura General

- Spring Boot como framework principal.

- JPA / Hibernate para persistencia en base de datos (tabla cdnUrl).

- Redis como almacenamiento en memoria para control de sesiones o expiraciones.

- Almacenamiento local para guardar físicamente las imágenes.

Componentes principales:

- PhotoController → Controlador REST que expone la API.

- PhotoStorageService → Gestiona el directorio físico de almacenamiento.

- DomainService → Obtiene el dominio base para generar URLs públicas.

- CdnUrlRepository → Interfaz de acceso a la base de datos.

- RedisService → Controla el almacenamiento temporal en Redis.

- CdnUrl → Entidad que representa los metadatos de la imagen.

### 3. Entidad CdnUrl

Representa la información asociada a cada imagen.

| Campo            | Tipo          | Descripción                               |
| ---------------- | ------------- | ----------------------------------------- |
| `uuid`           | UUID          | Identificador único del archivo.          |
| `name`           | String        | Nombre original del archivo.              |
| `url`            | String        | URL pública para acceder al archivo.      |
| `filePath`       | String        | Ruta relativa en el almacenamiento local. |
| `upDate`         | LocalDateTime | Fecha y hora de subida.                   |
| `expireDateTime` | LocalDateTime | Fecha de expiración (opcional).           |

## 4. Repositorio `CdnUrlRepository`
Extiende `JpaRepository<CdnUrl, UUID>` e incluye:
- `findByName(String name)` → Busca una imagen por nombre.

---

## 5. Servicios

### 5.1 PhotoStorageService
- Inicializa el directorio raíz de almacenamiento (`cdnapp.storage.path` en `application.properties`).
- Método `getRootLocation()` → Devuelve la ruta raíz de almacenamiento.

### 5.2 DomainService
- Obtiene el dominio público desde la configuración (`cdnapp.domain`).
- Método `getDomain()` → Devuelve el dominio.

### 5.3 RedisService
- `set(key, value)` → Guarda un valor en Redis.
- `get(key)` → Obtiene un valor por clave.
- `del(key)` → Elimina un valor.

---

## 6. Controlador `PhotoController`

### 6.1 Subida de imagen
**POST** `/photo`

**Parámetros:**
- `file` (MultipartFile) → Imagen a subir.
- `expireAt` (ISO date-time, opcional) → Fecha de expiración.

**Flujo:**
1. Genera un UUID único.
2. Crea un directorio con ese UUID.
3. Copia el archivo al almacenamiento.
4. Construye una URL pública usando `DomainService`.
5. Guarda metadatos en la base de datos (`CdnUrl`).
6. Devuelve la URL pública.

**Ejemplo `curl`:**
```bash
curl -F "file=@./image.jpg" -F "expireAt=2025-08-15T12:00:00" -H "x-api-key: clave-secreta-super-segura" http://localhost:8080/photo
```

### 6.2 Obtener imagen por UUID
**GET** `/photo/{uuid}`

**Flujo:**
1. Busca el `CdnUrl` por UUID.
2. Si tiene fecha de expiración y está vencida:
    - Elimina de Redis.
    - Devuelve `423 Locked`.
3. Si existe físicamente, devuelve el archivo con su `Content-Type` correcto.

**Códigos de estado:**
- `200 OK` → Imagen encontrada.
- `404 Not Found` → No existe en BD o en disco.
- `423 Locked` → Expirada.

---

### 6.3 Obtener todas las fotos (JSON)
**GET** `/photoall`  
Devuelve un arreglo JSON con todos los registros `CdnUrl`.

---

### 6.4 Obtener todas las fotos (HTML)
**GET** `/photoall2`  
Devuelve una tabla HTML con los registros de la base de datos.