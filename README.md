# Mini CDN – Servicio de Gestión y Entrega de Imágenes


## 📖 Descripción
**CDNApp** es un servicio básico tipo **CDN (Content Delivery Network)** desarrollado en **Spring Boot** que permite:
- Subir imágenes y generar una URL pública para su acceso.
- Definir fechas de expiración opcionales para cada imagen.
- Servir las imágenes con el tipo de contenido adecuado.
- Consultar todas las imágenes en formato **JSON** o **HTML**.
- Integrarse con **Redis** para control de cacheo y gestión de recursos expirados.

Este proyecto es ideal como base para construir soluciones de **almacenamiento de medios** internas o sistemas de entrega de contenido.

---

## 🏗 Arquitectura
- **Spring Boot** → Framework principal.
- **Spring Data JPA / Hibernate** → Persistencia en base de datos.
- **Redis** → Cacheo y control de expiración.
- **Almacenamiento local** → Sistema de archivos para guardar imágenes.

**Componentes principales:**
- `PhotoController` → API REST para gestión de imágenes.
- `PhotoStorageService` → Control del directorio de almacenamiento.
- `DomainService` → Configuración del dominio para URLs públicas.
- `RedisService` → Interfaz con Redis.
- `CdnUrlRepository` → Acceso a la base de datos.
- `CdnUrl` → Entidad que almacena metadatos de imágenes.

---

## ⚙️ Requisitos previos
- **Java 17** o superior.
- **Maven** 3.x.
- **MySQL** o cualquier base de datos soportada por JPA.
- **Redis** instalado y en ejecución.

---

## 📦 Instalación y configuración
### 🐳 Configuración con Docker

Este proyecto puede ejecutarse fácilmente con **Docker Compose**, levantando los servicios necesarios para su funcionamiento:
- **PostgreSQL** → Base de datos para almacenar metadatos de las imágenes.
- **Redis** → Cacheo y gestión de expiraciones.
- **Spring Boot** → Aplicación principal.

### 📄 docker-compose.yml
```yaml
version: '3.8'

services:
  postgres:
    container_name: postgres_db_cdn
    image: postgres:latest
    restart: always
    ports:
      - "5432:5432"
    environment:
      POSTGRES_USER: defaultuser
      POSTGRES_PASSWORD: defaultpass
      POSTGRES_DB: cdndatabase
    volumes:
      - data:/var/lib/postgresql/data

  redis:
    container_name: redis_cdn
    image: redis:latest
    restart: always
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data

  springboot:
    container_name: springboot_cdn
    image: openjdk:17-jdk-slim
    restart: always
    depends_on:
      - postgres
      - redis
    ports:
      - "8080:8080"
    volumes:
      - ./build/libs/:/app
      - storage_data:/storage
    working_dir: /app
    command: ["java", "-jar", "cdn-1.0.0.jar"] # Cambiar por el nombre real del .jar generado
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/cdndatabase
      SPRING_DATASOURCE_USERNAME: defaultuser
      SPRING_DATASOURCE_PASSWORD: defaultpass
      SPRING_MAX_FILE_SIZE: 20MB
      SPRING_MAX_REQUEST_SIZE: 20MB
      SPRING_REDIS_HOST: redis
      SPRING_REDIS_PORT: 6379
      SECURITY_API_KEY: clave-secreta-super-segura
      CDNAPP_DOMAIN: http://cdn.local

volumes:
  data:
    driver: local
  redis_data:
    driver: local
  storage_data:
    driver: local
```
### 🛠 Compilar el proyecto

Antes de lanzar la aplicación, es necesario **compilar el proyecto** para generar el archivo `.jar` que se ejecutará en el contenedor de Spring Boot.

**Pasos:**
1. Asegúrate de que los contenedores de **PostgreSQL** y **Redis** estén en ejecución:
   ```bash
   docker compose up -d postgres redis
   ```
2. Ve a la carpeta raíz del proyecto.
3. Ejecuta el siguiente comando para compilar:
    ```./gradlew build```
4. Una vez finalizada la compilación, el archivo .jar se generará en la carpeta: ```build/libs/```
5. Con el .jar generado, ya puedes levantar la aplicación junto con el resto de servicios usando:
    ```docker compose up -d```

6. Ahora tu proyecto estará compilado y corriendo en contenedores Docker.

## 🚀 Endpoints principales
### 1️⃣ Subir una imagen

POST /photo
Parámetros:

- file → Archivo de imagen (multipart/form-data).

- expireAt → Fecha de expiración (opcional, formato ISO 8601).

Ejemplo:

```bash
curl -F "file=@./image.png" -F "expireAt=2025-08-15T12:00:00" -H "x-api-key: clave-secreta-super-segura" http://localhost:8080/photo
```

### 2️⃣ Obtener imagen por UUID

GET /photo/{uuid}

Códigos de estado:

- 200 OK → Imagen encontrada.

- 404 Not Found → No existe en BD o en disco.

- 423 Locked → Expirada.

### 3️⃣ Listar todas las imágenes (JSON)
GET /photoall
