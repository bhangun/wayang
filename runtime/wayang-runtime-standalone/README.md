# Wayang Runtime Standalone

Standalone runtime that composes:
- Wayang control services
- Gamelan orchestration engine
- Gollek inference engine

## Run

```bash
mvn -f pom.xml -pl wayang/runtime/wayang-runtime-standalone -am \
  quarkus:dev
```

## Distributable Builds

### Portable JAR (single file)

```bash
mvn -f wayang/pom.xml -pl runtime/wayang-runtime-standalone -am \
  clean package -DskipTests
```

Output:
- `wayang/runtime/wayang-runtime-standalone/target/wayang-runtime-standalone-1.0.0-SNAPSHOT-runner.jar`

Run:

```bash
java -jar wayang/runtime/wayang-runtime-standalone/target/wayang-runtime-standalone-1.0.0-SNAPSHOT-runner.jar
```

Mode switching:
- Community mode (default, embedded H2):
```bash
java -jar wayang/runtime/wayang-runtime-standalone/target/wayang-runtime-standalone-1.0.0-SNAPSHOT-runner.jar
```
- Enterprise mode (external PostgreSQL):
```bash
java -Dquarkus.profile=enterprise \
  -DWAYANG_DB_JDBC_URL=jdbc:postgresql://localhost:5432/wayang \
  -DWAYANG_DB_USERNAME=wayang \
  -DWAYANG_DB_PASSWORD=wayang \
  -DWAYANG_DB_REACTIVE_URL=postgresql://localhost:5432/wayang \
  -jar wayang/runtime/wayang-runtime-standalone/target/wayang-runtime-standalone-1.0.0-SNAPSHOT-runner.jar
```

### Native Binary

```bash
mvn -f wayang/pom.xml -pl runtime/wayang-runtime-standalone -am \
  clean package -Dnative -DskipTests
```

Output:
- `wayang/runtime/wayang-runtime-standalone/target/wayang-runtime-standalone-1.0.0-SNAPSHOT-runner`

Note:
- Native profile excludes `gollek-sdk-java-local` and `hypersistence-utils-hibernate-63` to keep native-image compilation stable.

## Main endpoints

- `GET /api/runtime/status` unified status across Wayang, Gamelan, Gollek
- `GET /api/orchestration/workflows` orchestration workflow listing

Detailed architecture and configuration: `STANDALONE_RUNTIME_DOCUMENTATION.md`.
