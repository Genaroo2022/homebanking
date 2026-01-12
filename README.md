# 🏦 Digital Home Banking - Hexagonal Architecture

![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=openjdk)
![Spring Boot 4.0.1](https://img.shields.io/badge/Spring_Boot-4.0.1-brightgreen?style=flat-square&logo=springboot)![Architecture](https://img.shields.io/badge/Architecture-Hexagonal-blueviolet?style=flat-square)
![License](https://img.shields.io/badge/License-MIT-lightgrey?style=flat-square)

Proyecto de Home Banking desarrollado con fines educativos y profesionales.

El objetivo es construir una plataforma financiera moderna, escalable y segura. El diseño prioriza el **desacoplamiento de componentes** mediante la Arquitectura Hexagonal (Ports and Adapters), permitiendo que la lógica de negocio permanezca agnóstica a la infraestructura tecnológica.

## 🏗️ Arquitectura y Diseño

El núcleo del sistema (`Domain`) está completamente aislado de frameworks externos. La comunicación se realiza estrictamente a través de Interfaces (`Ports`) e Implementaciones (`Adapters`).

### Estructura del Proyecto
El código sigue una organización semántica clara:

````text
src/
├── main/java/com/homebanking/
│   ├── application
│   │   ├── dto          # Request/Response records
│   │   └── usecase      # Lógica de aplicación (Casos de Uso)
│   ├── domain
│   │   ├── entity       # Entidades del núcleo (sin dependencias)
│   │   ├── service      # Servicios de dominio
│   │   └── exception    # Excepciones de dominio
│   ├── port
│   │   ├── in           # Interfaces de entrada
│   │   └── out          # Interfaces de salida
│   ├── adapter
│   │   ├── in
│   │   │   └── web      # Controllers, Filters (JWT) y Mappers
│   │   └── out
│   │       ├── persistence # JPA Repositories & Entities
│   │       └── external
│   │           ├── audit       # Adaptador de Auditoría
│   │           ├── notification # Email/SMS
│   │           └── security    # Adaptador de Seguridad
│   └── config           # Beans de Spring (Security, OpenAPI, Persistence)
│
└── test/java/com/homebanking/
    ├── application      # Unit Tests de Casos de Uso
    ├── domain           # Unit Tests de Entidades/Servicios
    ├── adapter          # Slice Tests (Controllers/Repositories)
    └── integration      # Tests de integración end-to-end
````


## 🛠️ Stack Tecnológico

Utilizamos las últimas versiones estables para garantizar un desarrollo empresarial robusto:

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

- [ ] 🔐 **Auth & Seguridad:** Login, implementación de JWT, Filtros de seguridad y Auditoría.
- [ ] 💰 **Gestión de Cuentas:** Consulta de saldos en tiempo real y generación de CBU.
- [ ] 💸 **Transacciones:** Transferencias entre terceros con validaciones ACID (atómicas).
- [ ] 🧾 **Pagos:** Módulo de pago de servicios (`BillUseCase`).
- [ ] 🔔 **Notificaciones:** Integración con adaptadores de Email, SMS y Push.
- [ ] 💳 **Tarjetas:** Gestión completa de tarjetas de débito/crédito.

---

## ⚙️ Configuración Local

Sigue estos pasos para levantar el entorno de desarrollo:

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
| **`dev`** (Default)            | **H2** (Memoria) | ❌ | Desarrollo local rápido. Datos volátiles. Logs en modo `DEBUG`. |
| **`test`** | **H2** (Reset) | ❌ | **Ejecución de Tests Automáticos** (CI/CD). DB limpia por test. |
| **`prod`**                     | **PostgreSQL** | ✅ | Despliegue en contenedores. Datos persistentes. Seguridad endurecida. |

### Cómo ejecutar en diferentes ambientes:

**Modo Desarrollo (Por defecto):**
```bash
./mvnw spring-boot:run
```
**Testing (Automático): Ejecuta la batería de pruebas unitarias y de integración.**
```bash
./mvnw test
```
**Simulación de Producción (requiere Docker):**
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```


## 🤝 Contribución y Estado del Proyecto ##

## 🤝 Estado del Proyecto

🟢 **Fase 1: Núcleo de Dominio (COMPLETO)**

Se ha finalizado la implementación de la capa de Dominio, logrando un **Rich Domain Model** (Modelo de Dominio Rico) totalmente desacoplado y autovalidado.

**Entidades Implementadas:**
- **User & Account:** Validación estricta de formatos, edad y consistencia de datos.
- **Transfer:** Implementación de **Máquina de Estados** (Pending -> Completed/Failed) para garantizar la integridad transaccional.
- **Card:** Validación algorítmica (**Luhn Algorithm**), control de fechas de vencimiento y lógica de activación.

**Características de Arquitectura:**
- **Inmutabilidad:** Se eliminaron los `Setters` públicos. El estado solo cambia a través de métodos de negocio (`deposit`, `activate`, `markAsCompleted`).
- **Seguridad JPA:** Constructores vacíos protegidos (`protected`) para impedir la creación de objetos inválidos fuera del framework.
- **Zero-Trust:** Validaciones exhaustivas en el constructor. No existen objetos "a medio llenar" en memoria.

**🚀 Próximos Pasos Inmediatos:**
1. **Puertos (Ports):** Definición de interfaces para Repositorios (`UserRepository`, `AccountRepository`).
2. **Adaptadores (Adapters):** Implementación de la persistencia con Spring Data JPA.

© 2026 - Desarrollado por Genaro Rotstein

📖 Para detalles técnicos profundos, ver [Documentación de Arquitectura](docs/ARCHITECTURE.md)
