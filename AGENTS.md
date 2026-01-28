# 🤖 Project Context for AI Agents
This document describes the AI ​​agent ecosystem and orchestration rules used to develop this home banking system. The goal is to ensure technical consistency under a hexagonal architecture and SOLID principles.

## 🏦 Project Overview: Digital Home Banking
This is a robust, enterprise-grade Home Banking REST API built with **Java 21** and **Spring Boot 3.4+**. The project prioritizes scalability, security, and strict decoupling of components.

## 🏗️ Architecture: Hexagonal (Ports & Adapters)
The project strictly follows **Hexagonal Architecture**.
**Crucial Rule:** The `Domain` layer must NEVER depend on `Application`, `Adapter`, or external frameworks (like Spring or Jakarta Persistence).

### Directory Structure & Responsibilities
* **`domain/`**: The core. Contains Entities, Value Objects, Enums, and Domain Services. Pure Java.
* **`application/`**: Contains `usecase` (logic orchestration) and `dto` (input/output records).
* **`port/`**: Interfaces. `in` (Driver ports) and `out` (Driven ports/Repositories).
* **`adapter/`**: Infrastructure.
    * `in/web`: Controllers, RestControllerAdvice.
    * `out/persistence`: JPA Entities, Spring Data Repositories, Mappers.
    * `out/external`: API clients, Email services, etc.
* **`config/`**: Spring Beans configuration.

## 🛠️ Tech Stack & Conventions
* **Language:** Java 21.
* **Framework:** Spring Boot 4.1.0
* **Database:** H2 (Dev) / PostgreSQL (Prod).
* **Security:** Spring Security + Stateless JWT + BCrypt.
* **Validation:** Jakarta Validation (`@Valid`, `@NotNull`) in DTOs; Domain validation in Entity constructors.
* **Mapping:** Manual mapping or MapStruct (preferred manual for total control in Hexagonal).
* **Concurrency:** Optimistic locking (planned) and `@Transactional` at UseCase level.

## ✅ Current Status
* **Identity:** User Registration fully implemented with domain validations.
* **Security:** JWT Authentication, Public/Private endpoints, Password encryption.
* **Accounts:** Automatic Account creation upon registration, Balance queries (`/me`).
* **Next Priority:** **Transfers Module Integrations (events, notifications, scheduler hardening).**

## 📝 Coding Standards for AI
1.  **Rich Domain Model:** Do not create Anemic entities. Business logic belongs in the Entity.
2.  **Immutability:** Use `record` for DTOs.
3.  **Fail-Fast:** Validate inputs immediately. Use custom Exceptions in `domain/exception`.
4.  **No Leaking:** Do not return Domain Entities in Controllers. Map to Response DTOs.

## 🤖 Agent Roles & Responsibilities

| Agent | Primary Responsibility | Critical Context |
|---|---|---|
| Domain Architect | Design rich entities and pure business logic. | `src/main/java/com/homebanking/domain` |
| Ports & Adapters Expert | Define contracts and keep infrastructure decoupled. Domain must not depend on adapters. | `src/main/java/com/homebanking/port`, `adapter` |
| QA Automation Bot | Create unit tests with Mockito and JUnit 5. | `src/test/java/com/homebanking` |
| Refactor Sentinel | Optimize code, reduce technical debt, and enforce SOLID without changing public behavior. | Whole project |
| Security Steward (optional) | Guard JWT/security filters, profiles, and endpoint exposure. | `config/`, `adapter/in/web/security` |
| Docs & DX (optional) | Keep README and architecture docs aligned with changes. | `README.md`, `docs/ARCHITECTURE.md` |

Full directory structure lives in `README.md` and `docs/ARCHITECTURE.md`.
