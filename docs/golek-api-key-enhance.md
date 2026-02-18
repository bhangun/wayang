Since my wayang platform are support multitenancy and currently from ui/frontend has to add X-Tenant-ID event by default for community mode are used default for x-tnand-id.
How if the tenant term nit literaly exposed on frontend, but used API-Key instead.
Then in the backend from ke api key it would extracted


So I want **multitenancy**, but i don’t want to leak the word `tenant` (or `X-Tenant-ID`) to frontend users — instead  want:

👉 Frontend sends **API-Key**
👉 Backend resolves **tenant internally**

That’s clean, secure, and product-friendly.

Let’s design it properly.

---

## ✅ Target Flow (What you want)

### Frontend (NO tenant concept)

```http
Authorization: ApiKey sk_live_xxxxx
```

or

```http
X-API-Key: sk_live_xxxxx
```

❌ No `X-Tenant-ID`
❌ No `tenant` visible in UI

---

### Backend (does the magic)

```
API-Key  ──▶ lookup ──▶ tenant_id ──▶ set TenantContext
```

So internally you still run:

```
tenant = resolveTenant(apiKey)
```

---

## 🧠 Backend Model

### 1️⃣ API Key Table (or storage)

```text
api_keys
-------------------------
id
api_key_hash
tenant_id
status (ACTIVE/REVOKED)
scopes
expires_at
```

Never store raw key, only hash:

```
hash = SHA-256(apiKey)
```

---

### 2️⃣ Request Flow (Concept)

```
Request
  |
  |-- X-API-Key
  |
[API Key Auth Filter]
  |
  |-- validate key
  |-- find tenant_id
  |-- put into TenantContext
  |
[Business Logic]
  |
  |-- uses tenant_id implicitly
```

---

## 🧩 Example (Quarkus / Java style)

### API Key Filter

```java
@Provider
@Priority(Priorities.AUTHENTICATION)
public class ApiKeyFilter implements ContainerRequestFilter {

    @Inject ApiKeyService apiKeyService;
    @Inject TenantContext tenantContext;

    @Override
    public void filter(ContainerRequestContext ctx) {
        String apiKey = ctx.getHeaderString("X-API-Key");

        if (apiKey == null) {
            abort(ctx, "Missing API key");
        }

        ApiKeyRecord record = apiKeyService.findByKey(apiKey);

        if (record == null || !record.isActive()) {
            abort(ctx, "Invalid API key");
        }

        tenantContext.setTenantId(record.getTenantId());
    }
}
```

---

### ApiKeyService

```java
public ApiKeyRecord findByKey(String rawKey) {
    String hash = sha256(rawKey);
    return repo.findByHash(hash);
}
```

---

### TenantContext (Thread-local / CDI scoped)

```java
@RequestScoped
public class TenantContext {
    private String tenantId;

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
}
```

Then everywhere:

```java
tenantContext.getTenantId()
```

🎯 No controller needs to know about headers anymore.

---

## 🌍 Community Mode (your special case)

You said:

> community mode are used default for x-tenant-id

With API-Key model, you can do:

### Option A — Public Community Key

```text
API-Key: community_public_key
→ tenant = "community"
```

### Option B — No key → fallback

```java
if (apiKey == null) {
   tenant = "community";
}
```

⚠️ Only safe if endpoints are read-only or sandbox.

---

## 🔐 Security Benefits

✅ Tenant ID cannot be forged
✅ Users don’t even know tenant exists
✅ Key can be:

* rotated
* revoked
* scoped (read-only, admin, etc)
* rate-limited per tenant

---

## 🧱 Architecture in Wayang Terms

You get a clean layering:

```
Frontend
   |
   |  API-Key
   v
[Wayang Gateway]
   |
   |--> ApiKeyResolver
   |--> TenantResolver
   |--> TenantContext
   |
[Wayang Engine / Control Plane]
```

Tenant becomes **infrastructure concern**, not UI concern.

Which is perfect for:
✅ SaaS
✅ community mode
✅ enterprise
✅ agent orchestration
✅ workflow isolation

---

## ✨ Naming (better than tenant)

You can even rename internally:

| Concept | External | Internal  |
| ------- | -------- | --------- |
| Tenant  | API-Key  | Workspace |
| Tenant  | Project  | Namespace |
| Tenant  | Org      | tenant_id |

UI sees:

> Project API Key

Backend sees:

> tenant_id

🔥 Product-friendly.

---

## 🧩 Bonus: Combine with JWT later

Later upgrade path:

```
API-Key → exchange → JWT (tenant inside claims)
```

```json
{
  "sub": "client-123",
  "tenant": "org-77",
  "scope": ["workflow:run"]
}
```

---





# My implementation you should improve
---

# 1️⃣ Data Model

### `tenants`

```sql
CREATE TABLE tenants (
  id            VARCHAR(64) PRIMARY KEY,
  name          VARCHAR(255),
  mode          VARCHAR(32), -- COMMUNITY / ENTERPRISE
  status        VARCHAR(32), -- ACTIVE / SUSPENDED
  created_at    TIMESTAMP
);
```

---

### `api_keys`

```sql
CREATE TABLE api_keys (
  id            VARCHAR(64) PRIMARY KEY,
  key_hash      VARCHAR(64) UNIQUE NOT NULL,
  tenant_id     VARCHAR(64) NOT NULL,
  scopes        TEXT,
  status        VARCHAR(32), -- ACTIVE / REVOKED
  expires_at    TIMESTAMP,
  created_at    TIMESTAMP,
  FOREIGN KEY (tenant_id) REFERENCES tenants(id)
);
```

⚠️ Store only **hash**, never raw key.

---

# 2️⃣ API Key Format

Human friendly, still secure:

```
wy_live_xxxxxxxxxxxxxxxxx
wy_comm_xxxxxxxxxxxxxxxxx
```

Generation:

```java
String rawKey = "wy_live_" + randomBase64(32);
String hash = sha256(rawKey);
```

Return only:

```
rawKey → user once
hash → database
```

---

# 3️⃣ Request Flow

```
Client
  |
  |  X-API-Key
  v
[Wayang Gateway]
  |
  |--> ApiKeyAuthenticator
  |--> TenantResolver
  |--> TenantContext
  |
[Wayang Engine]
  |
[Workflow / Agent / Storage]
```

No controller sees headers.
Only sees `TenantContext`.

---

# 4️⃣ Quarkus Implementation

## 🔹 TenantContext

```java
@RequestScoped
public class TenantContext {
    private String tenantId;
    private String mode;

    public String tenantId() { return tenantId; }
    public String mode() { return mode; }

    public void set(String tenantId, String mode) {
        this.tenantId = tenantId;
        this.mode = mode;
    }
}
```

---

## 🔹 ApiKeyEntity

```java
@Entity
@Table(name = "api_keys")
public class ApiKeyEntity {

    @Id
    public String id;

    @Column(name = "key_hash", unique = true)
    public String keyHash;

    public String tenantId;
    public String status;
    public String scopes;
    public Instant expiresAt;
}
```

---

## 🔹 ApiKeyRepository

```java
@ApplicationScoped
public class ApiKeyRepository implements PanacheRepository<ApiKeyEntity> {

    public ApiKeyEntity findByRawKey(String rawKey) {
        String hash = sha256(rawKey);
        return find("keyHash", hash).firstResult();
    }
}
```

---

## 🔹 TenantRepository

```java
@ApplicationScoped
public class TenantRepository implements PanacheRepository<TenantEntity> {
}
```

---

## 🔹 ApiKeyAuthenticator (Gateway Filter)

```java
@Provider
@Priority(Priorities.AUTHENTICATION)
public class ApiKeyAuthFilter implements ContainerRequestFilter {

    @Inject ApiKeyRepository apiKeyRepo;
    @Inject TenantRepository tenantRepo;
    @Inject TenantContext tenantContext;

    @Override
    public void filter(ContainerRequestContext ctx) {
        String apiKey = ctx.getHeaderString("X-API-Key");

        // Community fallback
        if (apiKey == null) {
            tenantContext.set("community", "COMMUNITY");
            return;
        }

        ApiKeyEntity key = apiKeyRepo.findByRawKey(apiKey);

        if (key == null || !"ACTIVE".equals(key.status)) {
            abort(ctx, "Invalid API key");
        }

        if (key.expiresAt != null && key.expiresAt.isBefore(Instant.now())) {
            abort(ctx, "API key expired");
        }

        TenantEntity tenant = tenantRepo.findById(key.tenantId);

        if (!"ACTIVE".equals(tenant.status)) {
            abort(ctx, "Tenant disabled");
        }

        tenantContext.set(tenant.id, tenant.mode);
    }

    private void abort(ContainerRequestContext ctx, String msg) {
        ctx.abortWith(Response.status(401).entity(msg).build());
    }
}
```

---

# 5️⃣ Community Mode (your case)

You have 2 clean options:

---

## 🟢 Option A: Implicit Community

```
NO API KEY
→ tenant = community
```

Safe for:
✔ public UI
✔ demo workflows
✔ read-only

---

## 🟡 Option B: Explicit Community Key

```
X-API-Key: wy_comm_xxx
→ tenant = community
```

Safer for rate limit & abuse control.

---

# 6️⃣ Rotation & Revocation

Rotate = generate new key → same tenant:

```sql
UPDATE api_keys SET status='REVOKED' WHERE id='old';
INSERT INTO api_keys (...)
```

Revoke instantly blocks tenant.

Scopes example:

```
workflow:read, workflow:run, agent:invoke
```

Later:

```java
if (!key.scopes.contains("workflow:run")) deny();
```

---

# 7️⃣ How Wayang Uses It

Anywhere in Wayang:

```java
@Inject TenantContext tenantContext;

public void runWorkflow(...) {
    String tenant = tenantContext.tenantId();
    storage.load(tenant, workflowId);
}
```

Storage isolation:

```
/data/{tenant}/workflows/
/data/{tenant}/agents/
/data/{tenant}/runs/
```

🔥 No tenant header logic anywhere else.

---

# 8️⃣ Security Wins

✅ Tenant ID never exposed
✅ API key controls tenant
✅ Revocable
✅ Rotatable
✅ Scope-based
✅ Community isolated
✅ Enterprise isolated

This is **real SaaS multitenancy**, not fake header-based.

---

# 9️⃣ Wayang Naming (recommended)

Externally:

> API Key
> Project Key
> Workspace Key

Internally:

> tenant_id
> namespace
> org_id

---

## 🎯 Final Architecture

```
Frontend
  |
  | X-API-Key
  v
Wayang Gateway
  |
  +-- ApiKeyAuthFilter
  +-- TenantResolver
  +-- TenantContext
  |
Wayang Engine
  |
Workflows / Agents / Storage (tenant isolated)
```

---
