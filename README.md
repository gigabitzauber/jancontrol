# JanControl CLI (scaffold)

Minimal scaffold for a Java 25 + Spring Boot 4.x CLI application.

Build

```bash
mvn -U -DskipTests package
```

Run

Make sure you run with preview features enabled for Java 25:

```bash
java --enable-preview -jar target/jancontrol-0.1.0-SNAPSHOT.jar run-fan 3
```

Notes
- This scaffold follows a small package layout: `command`, `service`, `domain`, `infrastructure`.
- Hardware adapters should be added under `infrastructure` and injected into services.
