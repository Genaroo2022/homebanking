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
├── main/java/com/homebanking/
│   ├── application
│   │   ├── dto          # Request/Response records
│   │   └── usecase      # Logica de aplicacion (Casos de Uso)
│   ├── domain
│   │   ├── entity       # Entidades del nucleo (sin dependencias)
│   │   ├── service      # Servicios de dominio
│   │   └── exception    # Excepciones de dominio
│   ├── port
│   │   ├── in           # Interfaces de entrada
│   │   └── out          # Interfaces de salida
│   ├── adapter
│   │   ├── in
│   │   │   ├── web        # Controllers, Filters (JWT) y Mappers
│   │   │   └── scheduler  # Jobs programados (procesamiento asincrono)
│   │   └── out
│   │       ├── persistence # JPA Repositories & Entities
│   │       ├── external
│   │       │   ├── audit        # Adaptador de Auditoria
│   │       │   ├── notification # Email/SMS
│   │       │   └── security     # Adaptador de Seguridad (e.g. validación externa)
│   │       └── security    # Hash de contrasenas (puerto out)
│   └── config           # Beans de Spring (Security, OpenAPI, Persistence)
│
└── test/java/com/homebanking/
    ├── application      # Unit Tests de Casos de Uso
    ├── domain           # Unit Tests de Entidades/Servicios
    ├── adapter          # Slice Tests (Controllers/Repositories)
    └── integration      # Tests de integracion end-to-end
```

### Notas de implementacion actuales
- Scheduling de transferencias aislado en `adapter/in/scheduler`.
- Generacion de tokens y hashing de contrasenas desacoplados via puertos `TokenGenerator` y `PasswordHasher`.
- Respuestas de error unificadas con `ErrorResponse` (incluye validaciones).
- Transferencias se rechazan si la cuenta destino no existe.
- Concurrencia de saldo protegida con `@Version` en Account JPA.
- Endpoint interno de deposito (solo `dev`): `POST /accounts/{id}/deposit`.
- El procesamiento de transferencias es asíncrono. Al crear una transferencia, su estado inicial es `PENDING` y se procesa automáticamente en segundo plano.
- Reintentos manuales disponibles en `POST /api/transfers/{id}/retry`.


## 🛠️ Stack Tecnológico

Utilizo las últimas versiones estables para garantizar un desarrollo empresarial robusto:

| Categoría | Tecnologías |
| :--- | :--- |
| **Core** | ![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white) ![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.0.1-6DB33F?style=for-the-badge&logo=spring&logoColor=white) |
| **Persistencia** | ![H2](https://img.shields.io/badge/H2-Dev_Mode-blue?style=for-the-badge) ➡️ ![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Prod_Goal-316192?style=for-the-badge&logo=postgresql&logoColor=white) |
| **Infraestructura** | ![Docker](https://img.shields.io/badge/Docker-Containerization_Planned-2496ED?style=for-the-badge&logo=docker&logoColor=white) |
| **Seguridad** | ![Spring Security](https://img.shields.io/badge/Spring_Security-JWT-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white) |
| **Herramientas** | ![MapStruct](https://img.shields.io/badge/MapStruct-Mapping-transparent?style=for-the-badge) ![Lombok](https://img.shields.io/badge/Lombok-Boilerplate-bc0230?style=for-the-badge&logo=lombok&logoColor=white) |
| **Docs & Test** | ![Swagger](https://img.shields.io/badge/Swagger-OpenAPI-85EA2D?style=for-the-badge&logo=swagger&logoColor=black) ![JUnit5](https://img.shields.io/badge/JUnit-5-25A162?style=for-the-badge&logo=junit5&logoColor=white) ![Mockito](https://img.shields.io/badge/Mockito-Testing-yellow?style=for-the-badge) |
---

## 🚀 Funcionalidades (Roadmap)

El diseño actual contempla la implementación modular de las siguientes características:

- [x] 🔐 **Auth & Seguridad:** Login, implementación de JWT, Filtros de seguridad y Auditoría.
- [x] 💰 **Gestión de Cuentas:** Consulta de saldos en tiempo real y generación de CBU.
- [x] 💸 **Transacciones:** Transferencias entre terceros con validaciones ACID (atómicas).
- [ ] 🧾 **Pagos:** Módulo de pago de servicios (`BillUseCase`).
- [ ] 🔔 **Notificaciones:** Integración con adaptadores de Email, SMS y Push.
- [ ] 💳 **Tarjetas:** Gestión completa de tarjetas de débito/crédito.

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

## 🌍 Ambientes y Perfiles (Spring Profiles)

El proyecto utiliza el sistema de perfiles de Spring Boot para adaptar la infraestructura según el entorno de ejecución, manteniendo la lógica de negocio inalterada.

| Perfil                         | Base de Datos | Docker | Uso Previsto |
|:-------------------------------| :--- | :---: | :--- |
| **`dev`**                      | **H2** (Memoria) | ❌ | Desarrollo local rápido. Habilita endpoints internos de testing. |
| **`test`** | **H2** (Reset) | ❌ | **Ejecución de Tests Automáticos** (CI/CD). DB limpia por test. |
| **`prod`**                     | **PostgreSQL** | ✅ | Despliegue en contenedores. Datos persistentes. Seguridad endurecida. |

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

# 🧪 Guía de Uso de la API

El sistema implementa un flujo seguro completo. Sigue estos pasos para probarlo:

### 1️⃣ Registrar un Usuario (Crea Cuenta Automática)
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
### 2️⃣ Iniciar Sesión (Obtener Token)
* **Endpoint:** `POST` `/auth/login`
* **Body:**

```json

{
  "email": "lio@messi.com",
  "password": "SecurePass123!"
}
```
**💡 Respuesta: Recibirás un token JWT. Cópialo para usarlo en el siguiente paso.**

### 3️⃣ Ver Mi Perfil y Cuentas (Endpoint Seguro)
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

### 4️⃣ Cargar Saldo (Solo DEV)
* **Endpoint:** `POST` `/accounts/{id}/deposit`
* **Headers:** `Authorization: Bearer <TU_TOKEN_AQUI>`
* **Body:**
```json
{
  "amount": 1000.00
}
```

### 5️⃣ Crear Transferencia
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

### 6️⃣ Consultar o Reintentar una Transferencia
El procesamiento de la transferencia se inicia automáticamente en segundo plano después de la creación. Para conocer su estado final, puedes usar los siguientes endpoints:

* **Consultar el estado de una transferencia:**
    * `GET /api/transfers/{id}`
* **Reintentar una transferencia que ha fallado:**
    * `POST /api/transfers/{id}/retry`
# 🧪 Testing
* **Ejecutar Tests Unitarios**
```bash
bash./mvnw test
```

* **Características:**

**✅ Sin Spring context (ultrarrápidos <100ms)**

**✅ Mocks con Mockito**

**✅ 100% coverage de casos de uso**

**✅ CI/CD ready**

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
| **Payments** | Transferencias Atómicas (ACID)       | ✅ **Core Implemented**                                                   | `POST /api/transfers`  |
| **Cards** | Emisión y Lógica de Luhn             | 🚧 Core Implemented                                                       | `POST /cards`      |

 **Genaro Rotstein** | **Software Engineer**

📖 Para detalles técnicos profundos, ver [Documentación de Arquitectura](docs/ARCHITECTURE.md)


