# Blaikistall Copilot Instructions for Java 25 Spring Boot 4.x CLI Application

## 0. Ground rules

- You must follow all rules depicted in the file .blaikistall/blaikistall_ground_rules.md in all interactions. If you are unsure about how to apply these rules in a specific situation, ask for clarification.

## 1. Technology Stack & Environment

- **Language**: Java 25 (Preview features enabled if specified).
- **Framework**: Spring Boot 4.x.
- **Build Tool**: Maven.
- **Interface**: Command Line Interface (CLI).
- **Testing**: JUnit 5, Mockito.

## 2. General Philosophy

- **Clean Code**: Adhere to Robert C. Martin's principles. Functions should do one thing, variable names must be descriptive, and comments should explain _why_, not _what_.
- **Effective Java**: Follow Joshua Bloch's advice. Favor immutability, use dependency injection, and prefer standard functional interfaces.
- **Clean Architecture**: Isolate the domain logic from the CLI delivery mechanism and external infrastructure.

## 3. Coding Standards (Java 25)

- **Immutability**: Use `record` for data carriers (DTOs, Value Objects) by default.
- **Pattern Matching**: Utilize pattern matching for `switch` and `instanceof` to reduce boilerplate.
- **Type Inference**: Use `var` for local variables where the type is obvious from the right-hand side.
- **Null Safety**: Treat all fields/parameters as non-null by default. Use `Optional<T>` for return types that might be empty. avoid passing `null` or `Optional` as parameters.
- **Streams & Lambdas**: Prefer Stream API for collection processing but avoid over-complex streams that harm readability.
- **Constructors**: Prefer constructor injection over field injection (`@Autowired` on fields is forbidden).

## 4. Architecture & Structure

- **Layering**:
  - `command`: CLI specific classes (ShellComponents, Converters).
  - `service`: Application business rules (Orchestration).
  - `domain`: Core business logic and entities (POJOs, Records).
  - `infrastructure`: External adapters (File I/O, API clients).
- **Dependency Rule**: Dependencies point inwards. Domain code must not depend on infrastructure details.
- **CLI Specifics**:
  - Keep command entry points thin. Delegate logic to services immediately.
  - Use concise command groups and help descriptions.

## 5. Error Handling

- **Exceptions**: Use checked exceptions for recoverable conditions and unchecked (Runtime) exceptions for programming errors.
- **Exit Codes**: The application must return standard exit codes (0 for success, non-zero for failure).
- **User Feedback**: Errors presented to the user should be human-readable, not stack traces (unless `--debug` is active).

## 6. Logging

- **Framework**: Use SLF4J.
- **Levels**:
  - `DEBUG`: Detailed flow information useful for developers.
  - `INFO`: High-level progress (e.g., "Processing file X...").
  - `WARN`: Recoverable issues or potential configuration problems.
  - `ERROR`: Failures requiring user intervention or bug reports.
- **Format**: Do not log sensitive data (passwords, tokens).

## 7. Testing

- **Frameworks**: JUnit 5 (Jupiter), Mockito.
- **Unit Tests**:
  - Fast, isolated tests for Domain and Service layers.
  - Use `Mockito` to mock external dependencies.
  - One assert concept per test.
- **Integration Tests**:
  - Use `@SpringBootTest` sparingly for full context loading.
- **Naming**:
  - `when_transition_then_output`.
  - Use snake case for test names.

## 8. Maven Build

- Keep `pom.xml` sorted and clean.
- Use properties for version management.
- Ensure the `spring-boot-maven-plugin` is configured for building executable JARs.