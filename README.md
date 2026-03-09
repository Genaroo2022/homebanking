# 🏦 Digital Home Banking - Hexagonal Architecture

![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=openjdk)
![Spring Boot 4.0.1](https://img.shields.io/badge/Spring_Boot-4.0.1-brightgreen?style=flat-square&logo=springboot)![Architecture](https://img.shields.io/badge/Architecture-Hexagonal-blueviolet?style=flat-square)
![License](https://img.shields.io/badge/License-MIT-lightgrey?style=flat-square)

Plataforma de banca digital moderna, escalable y segura desarrollada con Arquitectura Hexagonal pura, Clean Architecture y principios SOLID 100%.

El diseño invierte todas las dependencias hacia el dominio, garantizando que la lógica de negocio permanezca completamente agnóstica a frameworks externos. Cada cambio en infraestructura ocurre sin tocar el núcleo del sistema.

## 🏗️ Arquitectura y Diseño

El núcleo del sistema (`Domain`) está completamente aislado de frameworks externos. La comunicación se realiza estrictamente a través de Interfaces (`Ports`) e Implementaciones (`Adapters`).

### Estructura del Proyecto
El código sigue una organización semántica clara:

```text
src/
+-- main/
    +-- java/com/homebanking/
    ¦   +-- adapter
    ¦   ¦   +-- in
    ¦   ¦   ¦   +-- event/         # Listeners de Eventos (ej. TransferEventListener)
    ¦   ¦   ¦   +-- scheduler/     # Tareas Programadas (ej. TransferProcessingScheduler)
    ¦   ¦   ¦   +-- web/           # Controladores REST, Handlers de Excepciones, Mappers, etc.
    ¦   ¦   +-- out
    ¦   ¦       +-- event/         # Implementaciones de Publicadores de Eventos
    ¦   ¦       +-- external/      # Adaptadores para servicios externos (pagos, notificaciones)
    ¦   ¦       +-- persistence/   # Implementaciones JPA de repositorios
    ¦   ¦       +-- security/      # PasswordHasher y rate limiting
    ¦   +-- application
    ¦   ¦   +-- dto/             # DTOs para casos de uso
    ¦   ¦   +-- service/         # Servicios de aplicación que orquestan casos de uso
    ¦   ¦   +-- usecase/         # Implementaciones de casos de uso
    ¦   +-- config/              # Beans de configuración de Spring
    ¦   +-- domain
    ¦   ¦   +-- entity/          # Entidades de dominio (ej. Account, Transfer)
    ¦   ¦   +-- enums/           # Enumeraciones específicas del dominio
    ¦   ¦   +-- event/           # Definiciones de eventos de dominio
    ¦   ¦   +-- exception/       # Excepciones de dominio personalizadas
    ¦   ¦   +-- policy/          # Políticas de dominio (ej. transiciones de estado)
    ¦   ¦   +-- service/         # Servicios de dominio
    ¦   ¦   +-- valueobject/     # Value Objects de dominio
    ¦   +-- port
    ¦   ¦   +-- in/              # Puertos de entrada (interfaces de casos de uso)
    ¦   ¦   +-- out/             # Puertos de salida (interfaces de repositorios, servicios)
    ¦   +-- HomebankingApplication.java
    +-- resources/
        +-- db/migration/      # Migraciones de base de datos (Flyway/Liquibase)
        +-- static/
        +-- templates/
        +-- application.properties
```

### Notas de implementacion actuales
- Scheduling de transferencias aislado en `adapter/in/scheduler` con guardas anti-solapamiento.
- Procesamiento batch acotado por configuracion (`transfer.processor.max-batch-size`).
- Generacion de tokens y hashing desacoplados via puertos `TokenGenerator` y `PasswordHasher`.
- Rate limiting por IP en `/auth/login` + backoff exponencial por usuario.
- Deteccion de anomalias de login desacoplada por puerto (`LoginAnomalyDetector`) con adapter heuristico inicial.
- Refresh tokens con rotacion y blacklist en Redis (logout real).
- Blacklist de access tokens para revocacion inmediata.
- 2FA TOTP (setup, QR y habilitacion) por usuario.
- Auditoria con `@Aspect` y logger `AUDIT` (JSON + archivo dedicado).
- Headers de seguridad (HSTS, CSP, Referrer, Permissions) en `SecurityConfig`.
- Respuestas de error unificadas con `ErrorResponse` (incluye validaciones).
- Transferencias se rechazan si la cuenta destino no existe.
- Concurrencia de saldo protegida con `@Version` en Account JPA.
- Endpoint interno de deposito (solo `dev`): `POST /accounts/{id}/deposit`.
- Transferencias asincronas: estado inicial `PENDING`, procesamiento en background, reintentos manuales en `POST /api/transfers/{id}/retry`.
- Modulo de pago de servicios implementado (`POST /api/bills/pay`, `GET /api/bills/{id}`) con idempotencia y ownership checks.
- Gestion de tarjetas implementada (`/cards`) con almacenamiento cifrado de PAN/CVV.


## 🛠️ Stack Tecnológico

Utilizo las últimas versiones estables para garantizar un desarrollo empresarial robusto:

| Categoría | Tecnologías |
| :--- | :--- |
| **Core** | ![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white) ![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.0.1-6DB33F?style=for-the-badge&logo=spring&logoColor=white) |
| **Persistencia** | ![H2](https://img.shields.io/badge/H2-Dev_Mode-blue?style=for-the-badge) ➜ ![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Prod_Goal-316192?style=for-the-badge&logo=postgresql&logoColor=white) |
| **Infraestructura** | ![Docker](https://img.shields.io/badge/Docker-Containerization_Planned-2496ED?style=for-the-badge&logo=docker&logoColor=white) |
| **Seguridad** | ![Spring Security](https://img.shields.io/badge/Spring_Security-JWT-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white) ![Redis](https://img.shields.io/badge/Redis-Token_Blacklist-red?style=for-the-badge&logo=redis&logoColor=white) |
| **Herramientas** | ![MapStruct](https://img.shields.io/badge/MapStruct-Mapping-transparent?style=for-the-badge) ![Lombok](https://img.shields.io/badge/Lombok-Boilerplate-bc0230?style=for-the-badge&logo=lombok&logoColor=white) |
| **Docs & Test** | ![Swagger](https://img.shields.io/badge/Swagger-OpenAPI-85EA2D?style=for-the-badge&logo=swagger&logoColor=black) ![JUnit5](https://img.shields.io/badge/JUnit-5-25A162?style=for-the-badge&logo=junit5&logoColor=white) ![Mockito](https://img.shields.io/badge/Mockito-Testing-yellow?style=for-the-badge) |
---

## 🚀 Funcionalidades (Roadmap)

El diseño actual contempla la implementación modular de las siguientes características:

- [x] 🔐 **Auth & Seguridad:** Login, implementación de JWT, Filtros de seguridad y Auditoría.
- [x] 🏦 **Gestión de Cuentas:** Consulta de saldos en tiempo real y generación de CBU.
- [x] 💸 **Transacciones:** Transferencias entre terceros con validaciones ACID (atómicas).
- [x] 🧾 **Pagos:** Módulo de pago de servicios (`BillUseCase`) implementado.
- [x] 💳 **Tarjetas:** Gestión de emisión/consulta/activación/desactivación implementada.
- [x] 📧 **Notificaciones:** Orquestación desacoplada y configurable de canales (Email/SMS/Push) implementada a nivel adapter.
- [ ] 🌐 **Integraciones externas productivas:** proveedores reales de Email/SMS/Push y pipeline Kafka/SIEM para anomalías.
- [ ] 🔐 **Hardening criptográfico avanzado:** rotación de claves de cifrado de tarjetas y política operacional de llaves.

---

## ⚙️ Configuración Local

Sigue estos pasos para levantar el entorno de desarrollo:

## 📋 Prerrequisitos

Antes de ejecutar el proyecto, asegúrate de tener instalado:

- **Java JDK 21 o superior**
- **Java correctamente configurado en el PATH**
- **Maven 3.9+ (incluido en mvnw)**
- **Git para clonar el repositorio**

Puedes verificarlo ejecutando:

```bash
java -version
mvn -version
```
**-Una vez realizada la verificación, entonces:**

**1. Clonar el repositorio**
```bash
git clone [https://github.com/Genaroo2022/homebanking.git](https://github.com/Genaroo2022/homebanking.git)
cd homebanking
```

**2. Compilar el proyecto:**

```bash

./mvnw clean install
```

**3. Ejecutar la aplicación:**

```bash

./mvnw spring-boot:run
```
La base de datos H2 se iniciará automáticamente en memoria.

**4. Documentación API: Una vez iniciado, accede a Swagger UI en: http://localhost:8080/swagger-ui.html**

## 🧪 Ambientes y Perfiles (Spring Profiles)

El proyecto utiliza el sistema de perfiles de Spring Boot para adaptar la infraestructura según el entorno de ejecución, manteniendo la lógica de negocio inalterada.

| Perfil                         | Base de Datos | Docker | Uso Previsto |
|:-------------------------------| :--- | :---: | :--- |
| **`dev`**                      | **H2** (Memoria) | ❌ | Desarrollo local rápido. Habilita endpoints internos de testing. |
| **`test`** | **H2** (Reset) | ❌ | **Ejecución de Tests Automáticos** (CI/CD). DB limpia por test. |
| **`prod`**                     | **PostgreSQL** | ✅ | Despliegue en contenedores. Datos persistentes. HTTPS requerido. |

### Cómo ejecutar en diferentes ambientes:

**Modo Desarrollo (con profile `dev`):**
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```
**Testing (Automático): Ejecuta la batería de pruebas unitarias y de integración.**
```bash
./mvnw test
```
**Simulación de Producción (requiere Docker):**
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

### Variables de entorno sensibles recomendadas
- `JWT_SECRET` (Base64, requerido)
- `CARD_DATA_KEY` (Base64 AES 16/24/32 bytes; requerido en `prod`)
- `SSL_KEY_STORE`, `SSL_KEY_STORE_PASSWORD`, `SSL_KEY_STORE_TYPE` (para TLS en `prod`)

# 📘 Guía de Uso de la API

El sistema implementa un flujo seguro completo. Sigue estos pasos para probarlo:

### 1️⃣ Registrar un Usuario 
#### (Crea Cuenta Automática)

> **Nota:** Al registrarse, el sistema genera automáticamente una **Caja de Ahorro** con CBU y Alias únicos.

* **Endpoint:** `POST` `/users`
* **Body:**

```json
{
  "name": "Lionel",
  "lastName": "Messi",
  "email": "lio@messi.com",
  "password": "SecurePass123!",
  "dni": "10101010",
  "birthDate": "1987-06-24",
  "address": "Miami, USA"
}
```
### 2️⃣ Iniciar Sesión 
#### (Obtener Token)
* **Endpoint:** `POST` `/auth/login`
* **Body:**

```json

{
  "email": "lio@messi.com",
  "password": "SecurePass123!"
}
```
**✅ Respuesta: Recibirás un token JWT. Cópialo para usarlo en el siguiente paso.**

### 3️⃣ Refresh de Tokens
* **Endpoint:** `POST` `/auth/refresh`
* **Body:**
```json
{
  "refreshToken": "<TU_REFRESH_TOKEN>"
}
```

### 4️⃣ Logout 
#### (Revoca refresh y access)
* **Endpoint:** `POST` `/auth/logout`
* **Headers:** `Authorization: Bearer <TU_ACCESS_TOKEN>`
* **Body:**
```json
{
  "refreshToken": "<TU_REFRESH_TOKEN>"
}
```

### 5️⃣ Activar 2FA (TOTP)
* **Setup:** `POST /auth/2fa/setup`
* **QR:** `GET /auth/2fa/qr` (image/png)
* **Enable:** `POST /auth/2fa/enable`
```json
{
  "code": "123456"
}
```

### 6️⃣ Ver Mi Perfil y Cuentas 
#### (Endpoint Seguro)
* **Endpoint:** `GET` `/auth/me`
* **Headers:** `Authorization: Bearer <TU_TOKEN_AQUI>`


* **Respuesta Esperada:**
```json
{
"id": 1,
"email": "lio@messi.com",
"name": "Lionel",
"lastName": "Messi",
"accounts": [
{
"id": 1,
"cbu": "1234567890123456789012",
"alias": "lionel.messi.123",
"balance": 0
}
]
}
```

### 7️⃣ Cargar Saldo (Solo DEV)
* **Endpoint:** `POST` `/accounts/{id}/deposit`
* **Headers:** `Authorization: Bearer <TU_TOKEN_AQUI>`
* **Body:**
```json
{
  "amount": 1000.00
}
```

### 8️⃣ Crear Transferencia
> **Nota:** Al crear la transferencia, su estado inicial será `PENDING`. El procesamiento se realiza automáticamente en segundo plano.

* **Endpoint:** `POST` `/api/transfers`
* **Headers:** `Authorization: Bearer <TU_TOKEN_AQUI>`
* **Body:**
```json
{
  "originAccountId": 1,
  "targetCbu": "1234567890123456789012",
  "amount": 150.50,
  "description": "Pago alquiler",
  "idempotencyKey": "{{$guid}}"
}
```

### 9️⃣Consultar o Reintentar  una Transferencia

El procesamiento de la transferencia se inicia automáticamente en segundo plano después de la creación. Para conocer su estado final, puedes usar los siguientes endpoints:

* **Consultar el estado de una transferencia:**
    * `GET /api/transfers/{id}`
* **Reintentar una transferencia que ha fallado:**
    * `POST /api/transfers/{id}/retry`

### 🔟 Pagar un Servicio
* **Endpoint:** `POST` `/api/bills/pay`
* **Headers:** `Authorization: Bearer <TU_TOKEN_AQUI>`, `Idempotency-Key: <UUID>`
* **Body:**
```json
{
  "accountId": "8cc8f23a-44f2-4b5d-a82d-694112ae2ad9",
  "billerCode": "EDENOR",
  "reference": "INV-2026-0001",
  "amount": 2500.75
}
```

### 1️⃣1️⃣ Consultar Pago de Servicio
* **Endpoint:** `GET` `/api/bills/{id}`
* **Headers:** `Authorization: Bearer <TU_TOKEN_AQUI>`

### 1️⃣2️⃣ Gestionar Tarjetas
* **Emitir tarjeta:** `POST /cards`
* **Listar por cuenta:** `GET /cards/account/{accountId}`
* **Activar tarjeta:** `PATCH /cards/{cardId}/activate`
* **Desactivar tarjeta:** `PATCH /cards/{cardId}/deactivate`
* **Headers:** `Authorization: Bearer <TU_TOKEN_AQUI>`
# 🧪 Testing
* **Ejecutar Tests Unitarios**
```bash
bash./mvnw test
```

* **Características:**

**⚡ Sin Spring context (ultrarrápidos <100ms)**

**🧪 Mocks con Mockito**

**✅ 100% coverage de casos de uso**

**🚀 CI/CD ready**

- ## Tests por Capa ##
```bash
bash# 
```
- ## Tests de Dominio ##
```bash
./mvnw test -Dtest=*Entity*
```

## Tests de UseCase ##
```bash
./mvnw test -Dtest=*UseCaseImpl*
```

## Tests de Controller ##
```bash
./mvnw test -Dtest=*Controller*
```

# 🤝 Contribución y Estado del Proyecto #

**El proyecto avanza por "Vertical Slices" funcionales.**

| Módulo | Funcionalidad                        | Estado                                                                    | Endpoint           |
| :--- |:-------------------------------------|:--------------------------------------------------------------------------|:-------------------|
| **Identity** | Registro de Usuario & Validaciones   | ✅ **Production Ready**                                                    | `POST /users`      |
| **Security** | Autenticación JWT & Stateless        | ✅ **Production Ready**                                                    | `POST /auth/login` |
| **Accounts** | Persistencia, Relaciones y Consultas | ✅ **Production Ready**                                                                         | `GET /auth/me`                 |
| **Payments** | Transferencias Atómicas (ACID)       | 🟡 **Core Implemented**                                                   | `POST /api/transfers`  |
| **Cards** | Emisión y Lógica de Luhn             | 🟡 Core Implemented                                                       | `POST /cards`      |

 **Genaro Rotstein** | **Software Engineer**

📚 Para detalles técnicos profundos, ver [Documentación de Arquitectura](docs/ARCHITECTURE.md)



