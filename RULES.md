# 📏 Golden Rules

To preserve architectural integrity, all agents must follow these guidelines:

1. **Domain Agnostic:** The `domain` package must never import classes from `application`, `adapter`, or any framework (including Spring).
2. **Rich Entities:** Anemic entities are not allowed. Business validations (e.g., minimum age in `User`, CBU format in `Account`) must live in entity constructors or methods.
3. **Dependency Inversion:** The flow must always be `Adapter -> Port (In) -> UseCase -> Port (Out) -> Adapter`.
4. **Strict Mapping:** Each layer owns its data model. Use dedicated mappers to convert between `WebDTO`, `DomainEntity`, and `PersistenceEntity`.

# 🔁 Refactoring & Scaling Flow

When a new feature or refactor is requested, the AI assistance process follows this order:

1. **Domain Impact Analysis:** Define changes in domain entities or services first.
2. **Ports Contract:** Update or create the necessary `Ports`.
3. **Use Case Implementation:** Implement logic in `application.usecase`, ensuring atomicity with `@Transactional`.
4. **Infrastructure Adaptation:** Implement the corresponding controllers or JPA repositories.
5. **Testing Cycle:** Generate unit tests aiming for 100% coverage of business logic.

# 🧰 Tooling & MCP

- **Editor:** Configured with custom instructions to respect the semantic folder structure.
- **Testing:** Use Mockito to fully isolate external dependencies in use case tests.
