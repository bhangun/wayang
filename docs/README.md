# Wayang Platform Docs

This folder contains the canonical architecture and roadmap documents for the Wayang platform and its core subsystems.

## Architecture (Current)

* Big picture (Wayang + Gamelan + Gollek): `wayang/docs/0-wayang-gamelan-gollek.md`
* Wayang control plane: `wayang/docs/1-wayang-architecture.md`
* Gamelan execution engine: `wayang/docs/2-gamelan-architecture.md`
* Gamelan DAG module upgrade: `wayang/docs/5-gamelan-dag-module.md`
* Gollek inference engine: `wayang/docs/3-gollek-architecture.md`
* Gollek implementation guide: `wayang/docs/4-gollek-implementation-guide.md`
* Wayang error codes: `wayang/docs/error-codes.md`

## Roadmap

* Platform roadmap (Wayang + Gamelan + Gollek): `wayang/docs/RAODMAP.md`

## Configuration

* Runtime flags and config reference: `wayang/docs/CONFIGURATION.md`

## Runtime Flags (Quick Reference)

| Flag | Default | Notes |
| --- | --- | --- |
| `wayang.multitenancy.enabled` | `false` | Enabled automatically by `tenant-*-ext` modules |
| `gamelan.dag.plugin.enabled` | `true` | Enables DAG validator plugin when `mode: DAG` |
| `gamelan.dag.scheduler.enabled` | `false` | Enables topological ordering for ready nodes |
| `gamelan.dag.validator.enabled` | `true` | Turns DAG validator on/off |
| `gamelan.dag.validator.allowMultipleRoots` | `false` | Allow multiple DAG roots |
| `gamelan.dag.validator.allowOrphanNodes` | `false` | Allow orphan nodes |
| `gamelan.dag.validator.maxDepth` | `100` | Max DAG depth |
| `gamelan.dag.validator.maxWidth` | `50` | Max DAG width |

## Multi-Tenancy Activation

Multi-tenancy is disabled by default and enabled per component via extensions.

* Gollek: `tenant-gollek-ext`
* Gamelan: `tenant-gamelan-ext`
* Wayang: `tenant-wayang-ext`

See `wayang-enterprise/modules/tenant/README.md` for details.
