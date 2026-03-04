# Wayang Control WebSocket

Realtime control-plane endpoint for frontend interoperability.

## Endpoint

- `ws://<host>:<port>/ws/v1/control-plane/{workspaceId}`

## Incoming Message

```json
{
  "type": "ping | validate | publish",
  "correlationId": "optional-client-correlation-id",
  "channel": "workflow | wayang | agent | node",
  "schemaId": "optional schema id",
  "payload": {},
  "metadata": {}
}
```

## Schema Routing

- `workflow` -> default `workflow-spec` (allows `workflow`, `workflow-spec`)
- `wayang` -> default `wayang-spec` (allows `wayang-spec`)
- `agent` -> default `agent-config` (allows `agent-config`, `agent-*`)
- `node` -> default `workflow-spec` (allows any registered catalog schema id)

If `schemaId` is incompatible with `channel`, message is rejected.

## REST -> WebSocket bridge

Control-plane REST APIs publish CDI events that are automatically forwarded to
workspace websocket rooms:

- `tenant:<tenantId>`
- `project:<projectId>`
- `route:<routeId>`

Current bridge emitters:

- `ProjectResource`: `project.created`, `project.deleted`
- `AgentResource`: `agent.created`, `agent.execution.started`
- `DesignerResource`: `designer.route.created`, `designer.node.added`,
  `designer.connection.added`, `designer.route.deployed`
- `SchemaRegistryService`: `schema.registered`, `schema.removed`

## Security (configurable)

Properties in runtime `application.properties`:

- `wayang.websocket.auth.enforce` (default `false`)
- `wayang.websocket.auth.require-bearer` (default `true`)
- `wayang.websocket.auth.require-tenant` (default `true`)

When `enforce=true`, websocket handshake requires:

- `Authorization: Bearer <token>` (if `require-bearer=true`)
- `X-Tenant-Id: <tenant>` (if `require-tenant=true`)
