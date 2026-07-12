# FreeMind — Agent Rules

## Project

Java 21 desktop mind-mapping app (FreeMind fork). Maven multi-module.

## Build & Run

Ensure you use Java 21 (`/usr/lib64/jvm/java-21-openjdk-21`) and run Maven commands from the project root:

```bash
# Prefix command with JAVA_HOME environment variable:
JAVA_HOME=/usr/lib64/jvm/java-21-openjdk-21 mvn clean install -DskipTests          # full build
JAVA_HOME=/usr/lib64/jvm/java-21-openjdk-21 mvn test                               # run all tests
JAVA_HOME=/usr/lib64/jvm/java-21-openjdk-21 mvn test -pl freemind-core -Dtest=TestClassName
JAVA_HOME=/usr/lib64/jvm/java-21-openjdk-21 mvn exec:java                          # launch app
```

## Architecture

- **freemind-core** — main app logic (controller, view, model, modes)
- **freemind-plugins** — plugin modules (time management, svg, latex, etc.)
- **freemind-distribution** — packaging (assembly descriptor, scripts)
- UI: Swing + JMapViewer for maps
- Model: XML-based mind map (`.mm`), bound via JiBX
- Tests: JUnit 4 with vintage engine, test maps in `freemind-core/tests/freemind/`

## Conventions

- Java package root: `freemind.*`
- Use `SimpleDateFormat` with explicit patterns in tests (avoid locale-dependent parsing)
- i18n properties files: `Resources_*.properties` — edit `Resources_en.properties` for new strings
- HighDPI: use `ScalableImageIcon` / `MultipleImage` for icons; null-guard GPU rendering paths

## Rules

- Never commit or push without explicit permission.
- Run `mvn test` before proposing any change.
- Prefer existing patterns over new dependencies.
