# 🎓 Learning Engine API

API REST para gestión de catálogo de cursos sincronizado con WordPress/WooCommerce, inscripciones con estados, módulos y lecciones con control de acceso.

## 🛠️ Tecnologías

- Java 21 + Spring Boot 3.5.x
- MySQL 8.0
- Redis 7 (caché de catálogo)
- RabbitMQ 3 (eventos de inscripción y módulos)
- WordPress + WooCommerce
- Swagger / OpenAPI

---

## 📋 Requisitos

- Java 21
- Docker Desktop
- IntelliJ IDEA
- Insomnia o Postman

---

## 🚀 Instalación

### 1. Clonar el repositorio

```bash
git clone https://github.com/tu-usuario/learning-engine.git
cd learning-engine
```

### 2. Levantar contenedores Docker

```bash
docker-compose up -d
```

Verifica que estén corriendo:
```bash
docker ps
```

Deben aparecer:
- `learning-mysql`
- `learning-redis`
- `learning-rabbitmq`
- `learning-wordpress`

### 3. Configurar WordPress

#### 3.1 Habilitar Application Passwords

```bash
docker exec -it learning-wordpress bash
apt-get update && apt-get install nano
nano /var/www/html/wp-config.php
```

Agregar antes de `/* That's all, stop editing! */`:

```php
define('WP_ENVIRONMENT_TYPE', 'local');
define('WOOCOMMERCE_FORCE_SSL_ADMIN', false);
define('FORCE_SSL_ADMIN', false);
$_SERVER['HTTPS'] = 'on';
```

Guardar: `Ctrl+O` → `Enter` → `Ctrl+X`

```bash
exit
docker restart learning-wordpress
```

#### 3.2 Instalar WooCommerce

```
http://localhost:8080/wp-admin
→ Plugins → Agregar nuevo
→ Buscar "WooCommerce" → Instalar → Activar
```

#### 3.3 Generar Application Password (WordPress)

```
http://localhost:8080/wp-admin
→ Usuarios → Tu perfil
→ Contraseñas de aplicación
→ Nombre: "springboot-api"
→ Añadir nueva contraseña → Copiar
```

#### 3.4 Generar API Keys (WooCommerce)

```
http://localhost:8080/wp-admin
→ WooCommerce → Ajustes → Avanzado → REST API
→ Agregar clave
→ Usuario: admin | Permisos: Lectura/Escritura
→ Generar clave API → Copiar Consumer Key y Consumer Secret
```

### 4. Configurar variables de entorno en IntelliJ

```
Run → Edit Configurations → Environment Variables
```

```
MYSQL_URL=jdbc:mysql://localhost:3307/learning_engine
MYSQL_USERNAME=springboot
MYSQL_PASSWORD=springboot123
REDIS_HOST=localhost
REDIS_PORT=6379
RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=guest
RABBITMQ_PASSWORD=guest
WORDPRESS_URL=http://localhost:8080
WORDPRESS_USERNAME=admin
WORDPRESS_PASSWORD=<tu application password>
WOO_CONSUMER_KEY=<tu consumer key>
WOO_CONSUMER_SECRET=<tu consumer secret>
```

### 5. Correr la aplicación

```
Run → LearningEngineApplication
```

O via Gradle:
```bash
./gradlew bootRun -x test
```

---

## 📖 Documentación API

```
http://localhost:8081/swagger-ui/index.html
```

---

## 🔗 Endpoints principales

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/courses?page=0&size=12` | Catálogo paginado |
| GET | `/api/courses?category=slug` | Filtrar por categoría |
| POST | `/api/courses/sync` | Sincronizar desde WordPress |
| GET | `/api/categories` | Listar categorías |
| POST | `/api/students` | Registrar estudiante |
| POST | `/api/enrollments` | Crear inscripción |
| GET | `/api/my-courses?studentId=1` | Cursos del estudiante |
| GET | `/api/courses/{id}/modules?studentId=1` | Módulos del curso |
| POST | `/api/lessons/{id}/complete` | Marcar lección completada |
| POST | `/api/woocommerce/webhook` | Webhook de pago |

---

## 🐳 Paneles de administración

| Servicio | URL | Usuario | Contraseña |
|----------|-----|---------|------------|
| WordPress Admin | http://localhost:8080/wp-admin | admin | (la que configures) |
| RabbitMQ Panel | http://localhost:15672 | guest | guest |
| API Swagger | http://localhost:8081/swagger-ui/index.html | — | — |

---

## 🗃️ Estructura del proyecto

```
src/
├── config/          # AppConfig, RabbitMQConfig
├── controller/      # REST Controllers
├── dto/
│   ├── request/     # Records de entrada
│   └── response/    # Records de salida
├── entity/          # Entidades JPA
├── enums/           # EnrollmentStatus
├── exception/       # GlobalExceptionHandler
├── repository/      # Spring Data JPA
└── service/
    └── impl/        # Implementaciones
```

---

## 📊 Diagrama de entidades

```
Category (1)
    └── (N) Course (1)
                ├── (N) CourseModule (1)
                │           └── (N) Lesson (1)
                │                       └── (N) LessonProgress
                └── (N) Enrollment (N) ──► Student
```
