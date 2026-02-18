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

## Main endpoints

- `GET /api/runtime/status` unified status across Wayang, Gamelan, Gollek
- `GET /api/orchestration/workflows` orchestration workflow listing

Detailed architecture and configuration: `STANDALONE_RUNTIME_DOCUMENTATION.md`.
