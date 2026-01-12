# 🏛️ Arquitectura del Sistema

🚧 Nota de Estado: Este proyecto se encuentra en su fase fundacional. La estructura de directorios y este documento de arquitectura representan el diseño técnico que guiará la implementación.

## 🎯 Visión General
Este proyecto implementa una **Arquitectura Hexagonal** (Ports & Adapters) estricta. El objetivo principal es garantizar que la lógica de negocio (Domain) permanezca agnóstica a la tecnología, permitiendo cambios en la infraestructura (bases de datos, APIs externas, frameworks web) sin afectar el núcleo del sistema.

### Principios Clave
1.  **Independencia de Frameworks:** El dominio no conoce a Spring Boot; este se utiliza solo como mecanismo de inyección de dependencias y configuración.
2.  **Aislamiento:** La API REST es solo un mecanismo de entrega (`adapter/in`), intercambiable por otros interfaces sin tocar la lógica.
3.  **Testabilidad:** La lógica de negocio es verificable mediante tests unitarios rápidos sin necesidad de levantar contextos de base de datos ni servidores web.

---

## 📂 Organización del Código

La estructura de carpetas es semántica y refleja la inversión de dependencias. A continuación se detalla la responsabilidad de cada módulo:

### 1. Domain Layer (`src/main/java/com/homebanking/domain`)
Es el corazón del software. No tiene dependencias externas ni de frameworks.
* **`entity/`**: Objetos de negocio con comportamiento y validación (ej. `Account`, `User`). Siguen el principio de **Entidades Ricas**.
* **`exception/`**: Excepciones de negocio (ej. `InvalidUserDataException`), desacopladas de códigos HTTP.
* **`util/`**: Constantes y reglas de negocio compartidas (ej. `DomainErrorMessages`). Permite evitar "Magic Strings" y centralizar textos de error.
* **`service/`**: Lógica de dominio pura que orquesta interacciones entre múltiples entidades (a implementar).
* **`service/`**: Lógica de dominio pura que orquesta interacciones entre múltiples entidades (a implementar).

### 2. Application Layer (`src/main/java/com/homebanking/application`)
Orquesta los casos de uso. Define **QUÉ** hace el sistema.
* **`usecase/`**: Implementación de los casos de uso (ej. `TransferMoneyUseCase`). Aquí reside la orquestación del flujo.
* **`dto/`**: Objetos inmutables (Records) para transporte de datos.
    * `request`: Estructuras de entrada (Comandos) que reciben los casos de uso. Encapsulan los datos necesarios para ejecutar una acción.
    * `response`: Estructuras de salida que devuelven los datos procesados, desacoplando el dominio de la vista API.
### 3. Interface Definition Layer (`src/main/java/com/homebanking/port`)
Define los contratos (interfaces) que desacoplan la aplicación del mundo exterior.
* **`in/`**: Interfaces que definen los casos de uso disponibles (API Driver). Lo que la aplicación *sabe hacer*.
* **`out/`**: Interfaces que definen qué necesita la aplicación del exterior (SPI Driven). Lo que la aplicación *necesita*.

### 4. Infrastructure Layer (`src/main/java/com/homebanking/adapter`)
Implementación técnica de los puertos. Aquí reside la dependencia con frameworks.

#### 🔹 Adapters In (Driving)
* **`web/controller`**: Controladores REST que reciben peticiones HTTP.
* **`web/filter`**: Filtros de seguridad (JWT) y CORS.
* **`web/mapper`**: Conversión de DTOs Web a Objetos de Dominio.
* **`event/`**: Adaptador para comunicación asíncrona.
    * `listener`: Escuchadores que reaccionan a cambios de estado internos (ej. `TransactionEventListener`) o mensajes externos.

#### 🔹 Adapters Out (Driven)
* **`persistence/`**: Capa de acceso a datos con JPA.
    * `entity`: Entidades ORM (`@Entity`) que reflejan las tablas SQL, separadas del modelo de dominio.
    * `repository`: Interfaces que extienden `JpaRepository` (Magia de Spring Data).
    * `mapper`: Convierte `Domain Model` ↔ `Persistence Entity`.
    * `adapter`: Implementación del Puerto de Salida (`Port Out`). Es el encargado de llamar al repositorio y realizar el mapeo.
* **`external/`**: Integraciones con terceros, aisladas por contexto:
    * `audit`: Sistemas de log y auditoría.
    * `notification`: Envío de correos/SMS.
    * `payment`: Pasarelas de pago.
    * `security`: Proveedores de autenticación.

---

## 🔄 Flujo de Ejecución (Ejemplo: Transferencia)

Para ilustrar el desacoplamiento, este es el ciclo de vida de una operación:

1.  **Entrada:** El cliente envía `POST /transfers`. El `TransferController` recibe la petición.
2.  **Validación:** Se valida el token JWT (`SecurityAdapter`) y el formato del JSON.
3.  **Cruce de Frontera:** El controlador convierte el DTO web a objetos de dominio y llama al puerto de entrada (`TransferPortIn`).
4.  **Núcleo:** El caso de uso (`TransferMoneyUseCase`) ejecuta la lógica:
    * Consulta saldos a través del puerto `AccountRepositoryPort`.
    * Aplica reglas de negocio (ej: no permitir saldo negativo).
    * Ordena la persistencia de los cambios.
5.  **Salida:**
    * El adaptador de persistencia guarda los datos en PostgreSQL.
    * El adaptador de notificación envía un email de confirmación.

---

## 🛡️ Decisiones de Diseño

### Estrategia de Mapeo (Mapping)
Se ha decidido **no compartir modelos** entre capas para evitar el acoplamiento fuerte:
* **Web DTO:** Optimizado para JSON y validaciones de entrada.
* **Domain Entity:** Optimizado para lógica de negocio y consistencia.
* **Persistence Entity:** Optimizado para tablas SQL y relaciones ORM.

### Estrategia de Validación de Dominio
Para garantizar la integridad de los datos, opté por una estrategia de **Validación en Constructor con Métodos Privados**:
1. **Fail-Fast:** No es posible instanciar una entidad en un estado inválido. Si los datos son incorrectos, el constructor lanza una excepción inmediata.
2. **Clean Code:** Para evitar la "Obsesión por los Primitivos" sin sobrecargar el proyecto con demasiadas clases pequeñas (Value Objects) en esta etapa temprana, utilizo métodos privados de validación (`validateDni`, `validateAge`) dentro de la entidad.
3. **Encapsulamiento:** Las reglas como "Mayoría de edad" o "Formato de CBU" viven dentro de la entidad correspondiente, no dispersas en servicios externos.

### Gestión de Errores
Las excepciones lanzadas en el dominio (`domain/exception`) son capturadas por un manejador global en la capa de infraestructura, traduciéndolas a respuestas HTTP estandarizadas (400, 404, 409) con mensajes claros para el cliente.

### Módulos Externos y Auditoría
Los servicios como "Pagos" o "Auditoría" se tratan como adaptadores externos en adapter/out/external. Esto permite que, si mañana cambiamos el proveedor de pagos, solo se toque el adaptador correspondiente sin modificar ni una línea del caso de uso.

### Encapsulamiento y Mutabilidad
Opté por un diseño de **Entidades Ricas** en contraposición al antipatrón de "Entidades Anémicas":
1.  **Eliminación de Setters:** Se han eliminado los métodos `set()` públicos. La mutabilidad es intencional y semántica.
    * *Incorrecto:* `account.setBalance(newBalance)`
    * *Correcto:* `account.deposit(amount)` o `account.debit(amount)`
2.  **Transiciones de Estado:** Entidades como `Transfer` poseen lógica de "Guard Clauses" para impedir cambios de estado ilegales (ej: no se puede cancelar una transferencia ya completada).

### Seguridad en la Instanciación
Para conciliar la seguridad del Dominio con los requisitos de JPA/Hibernate:
* **Constructores Públicos:** Son los únicos expuestos al código cliente. Exigen todos los datos obligatorios y ejecutan validaciones estrictas.
* **Constructores Protegidos:** Se utiliza `@NoArgsConstructor(access = AccessLevel.PROTECTED)`. Esto permite que Hibernate instancie la clase mediante reflexión, pero impide que un desarrollador cree objetos vacíos o inválidos por error.