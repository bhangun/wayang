
### Runtime Skill Management & Soft-Delete (Trash) Support

---

### 1. REST API Endpoints (`/api/v1/skills`)

- **List Skills**: `GET /api/v1/skills`
  - Query params: `?category=...`, `?query=...`, `?includeTrash=true`
- **Get Skill Details**: `GET /api/v1/skills/{id}`
  - Returns metadata, YAML frontmatter, markdown instructions, references, and scripts.
- **Create / Register Skill**: `POST /api/v1/skills`
  - Creates `~/.wayang/skills/<id>/SKILL.md` (and references/scripts) and hot-registers the skill into `SkillRegistry`.
- **Modify / Update Skill**: `PUT /api/v1/skills/{id}`
  - Updates `SKILL.md` on disk and refreshes the in-memory skill instance at runtime.
- **Soft Delete (Move to Trash)**: `DELETE /api/v1/skills/{id}`
  - Moves the skill folder to `~/.wayang/skills/.trash/<id>` and unregisters it from the active registry. *(Pass `?hard=true` only if permanent deletion is explicitly requested).*
- **List Trashed Skills**: `GET /api/v1/skills/trash`
- **Restore from Trash**: `POST /api/v1/skills/{id}/restore`
  - Restores the skill folder from `.trash/` and re-registers it into the active registry.
- **Hot Reload**: `POST /api/v1/skills/reload`
  - Re-scans `~/.wayang/skills` and workspace `.wayang/skills` to synchronize all skills at runtime without restarting the server.

---

### 2. gRPC API (`SkillService` in `wayang.proto`)

```protobuf
service SkillService {
  rpc ListSkills (ListSkillsRequest) returns (ListSkillsResponse);
  rpc GetSkill (GetSkillRequest) returns (GetSkillResponse);
  rpc SaveSkill (SaveSkillRequest) returns (SaveSkillResponse);
  rpc DeleteSkill (DeleteSkillRequest) returns (DeleteSkillResponse);
  rpc RestoreSkill (RestoreSkillRequest) returns (RestoreSkillResponse);
  rpc ListTrash (Empty) returns (ListSkillsResponse);
  rpc ReloadSkills (Empty) returns (ReloadSkillsResponse);
}
```
