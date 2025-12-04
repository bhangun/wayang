-- Models table
CREATE TABLE models (
    model_id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    version VARCHAR(100) NOT NULL,
    provider VARCHAR(100) NOT NULL,
    type VARCHAR(50) NOT NULL,
    capabilities TEXT[],
    max_tokens INTEGER,
    max_output_tokens INTEGER,
    latency_profile JSONB,
    cost_profile JSONB,
    supported_languages TEXT[],
    description TEXT,
    tags TEXT[],
    attributes JSONB,
    endpoint VARCHAR(500),
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    owner VARCHAR(255)
);

CREATE INDEX idx_model_provider ON models(provider);
CREATE INDEX idx_model_status ON models(status);
CREATE INDEX idx_model_type ON models(type);
CREATE INDEX idx_model_capabilities ON models USING GIN(capabilities);

-- Audit table
CREATE TABLE model_audit (
    id BIGSERIAL PRIMARY KEY,
    model_id VARCHAR(255),
    action VARCHAR(50) NOT NULL,
    actor VARCHAR(255),
    changes JSONB,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_model ON model_audit(model_id);
CREATE INDEX idx_audit_timestamp ON model_audit(timestamp);