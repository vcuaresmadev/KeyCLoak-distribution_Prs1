# KeyCLoak Distribution Microservice

Microservicio para gestión de distribuciones en el servicio de agua con autenticación Keycloak.

## 🚀 Características

- **Autenticación con Keycloak**: Integración completa con Keycloak para gestión de tokens JWT
- **CORS Configurado**: Soporte para aplicaciones frontend (Angular, React, etc.)
- **Docker Compose**: Configuración completa con Keycloak y MongoDB
- **Tokens de Larga Duración**: Configuración para tokens de 4 meses
- **APIs RESTful**: Endpoints para gestión de tarifas, horarios, rutas y programas

## 📋 Prerrequisitos

- Docker y Docker Compose
- Java 17+
- Maven 3.9+
- Node.js (opcional, para scripts de token)

## 🛠️ Instalación

### 1. Clonar el repositorio

```bash
git clone https://github.com/vcuaresmadev/KeyCLoak-distribution_Prs1.git
cd KeyCLoak-distribution_Prs1
```

### 2. Configurar variables de entorno

El proyecto incluye configuraciones por defecto, pero puedes personalizar:

```bash
# Variables de entorno para el backend
export SECURITY_ENABLED=true
export KEYCLOAK_ISSUER_URI=http://keycloak:8080/realms/master
export MONGO_USERNAME=sistemajass
export MONGO_PASSWORD=ZC7O1Ok40SwkfEje
export MONGO_DATABASE=JASS_DIGITAL
```

### 3. Ejecutar con Docker Compose

```bash
docker compose up --build -d
```

Esto iniciará:
- **Keycloak** en `http://localhost:8080`
- **Backend** en `http://localhost:8086`
- **MongoDB** (conectado a Atlas)

## 🔐 Configuración de Keycloak

### Cliente Backend

El proyecto incluye un cliente `backend` configurado en Keycloak con:
- **Client ID**: `backend`
- **Client Secret**: `your-secret-here`
- **Service Accounts**: Habilitado
- **Token Lifespan**: 4 meses (120 días)

### Generar Token para Angular

Para obtener un token válido para tu aplicación frontend:

```bash
./get-angular-token.sh
```

Este script generará un token con el issuer correcto (`http://keycloak:8080/realms/master`).

## 📡 APIs Disponibles

### Base URL
```
http://localhost:8086/api/v2
```

### Endpoints

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/fare/active` | Obtener tarifas activas |
| GET | `/schedules` | Obtener horarios |
| GET | `/routes` | Obtener rutas de distribución |
| GET | `/programs` | Obtener programas |

### Headers Requeridos

```typescript
{
  'Authorization': 'Bearer <token>',
  'Content-Type': 'application/json'
}
```

## 🔧 Configuración de CORS

El backend está configurado para permitir peticiones desde:
- `http://localhost:4200` (Angular dev server)
- `http://127.0.0.1:4200`
- `https://lab.vallegrande.edu.pe`

### Configuración en Angular

```typescript
// En tu servicio HTTP de Angular
private readonly authHeaders: HttpHeaders = new HttpHeaders({
  'Authorization': 'Bearer <token-generado-por-script>',
  'Content-Type': 'application/json'
});
```

## 🐳 Docker

### Construir imagen

```bash
docker build -t vg-web-be-distribucion-backend .
```

### Ejecutar contenedor

```bash
docker run -p 8086:8086 vg-web-be-distribucion-backend
```

## 📁 Estructura del Proyecto

```
├── src/
│   └── main/
│       ├── java/pe/edu/vallegrande/ms_distribution/
│       │   ├── application/
│       │   │   ├── config/
│       │   │   │   ├── GlobalCorsConfig.java
│       │   │   │   └── SecurityConfig.java
│       │   │   └── services/
│       │   ├── domain/
│       │   │   └── models/
│       │   └── infrastructure/
│       │       └── dto/
│       └── resources/
│           └── application.yml
├── keycloak/
│   └── realms/
│       └── jass-realm.json
├── docker-compose.yml
├── Dockerfile
├── get-angular-token.sh
└── README.md
```

## 🔍 Troubleshooting

### Error de CORS
Si ves errores de CORS, verifica:
1. Que el backend esté ejecutándose
2. Que la configuración de CORS incluya tu dominio
3. Que el token sea válido

### Error 401 Unauthorized
Si ves errores 401:
1. Ejecuta `./get-angular-token.sh` para obtener un token fresco
2. Verifica que el token no haya expirado
3. Asegúrate de que el issuer sea correcto

### Error de Conexión a Keycloak
Si hay problemas de conexión:
1. Verifica que Keycloak esté ejecutándose en el puerto 8080
2. Revisa los logs: `docker logs vg-web-be-distribucion-keycloak-1`

## 📝 Logs

### Ver logs del backend
```bash
docker logs vg-web-be-distribucion-backend-1
```

### Ver logs de Keycloak
```bash
docker logs vg-web-be-distribucion-keycloak-1
```

## 🤝 Contribuir

1. Fork el proyecto
2. Crea una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

## 📄 Licencia

Este proyecto está bajo la Licencia MIT. Ver el archivo `LICENSE` para más detalles.

## 👥 Autores

- **vcuaresmadev** - *Trabajo inicial* - [GitHub](https://github.com/vcuaresmadev)

## 🙏 Agradecimientos

- Keycloak por el sistema de autenticación
- Spring Boot por el framework
- MongoDB por la base de datos
