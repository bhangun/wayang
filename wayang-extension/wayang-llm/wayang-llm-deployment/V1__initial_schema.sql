-- Enable required extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

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
    owner VARCHAR(255),
    CONSTRAINT chk_status CHECK (status IN ('ACTIVE', 'DEPRECATED', 'EXPERIMENTAL', 'DISABLED'))
);

-- Indexes for models
CREATE INDEX idx_model_provider ON models(provider);
CREATE INDEX idx_model_status ON models(status);
CREATE INDEX idx_model_type ON models(type);
CREATE INDEX idx_model_capabilities ON models USING GIN(capabilities);
CREATE INDEX idx_model_tags ON models USING GIN(tags);
CREATE INDEX idx_model_created_at ON models(created_at DESC);

-- Model audit table
CREATE TABLE model_audit (
    id BIGSERIAL PRIMARY KEY,
    model_id VARCHAR(255) NOT NULL,
    action VARCHAR(50) NOT NULL,
    actor VARCHAR(255),
    changes JSONB,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_model FOREIGN KEY (model_id) REFERENCES models(model_id) ON DELETE CASCADE
);

CREATE INDEX idx_audit_model ON model_audit(model_id);
CREATE INDEX idx_audit_timestamp ON model_audit(timestamp DESC);
CREATE INDEX idx_audit_action ON model_audit(action);

-- Inference requests table (for tracking and analytics)
CREATE TABLE inference_requests (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    request_id VARCHAR(255) UNIQUE NOT NULL,
    tenant_id VARCHAR(255) NOT NULL,
    model_id VARCHAR(255),
    request_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    tokens_in INTEGER,
    tokens_out INTEGER,
    tokens_total INTEGER,
    cost_usd DECIMAL(10, 6),
    latency_ms BIGINT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    completed_at TIMESTAMP WITH TIME ZONE,
    error_message TEXT,
    CONSTRAINT fk_model_request FOREIGN KEY (model_id) REFERENCES models(model_id) ON DELETE SET NULL
);

CREATE INDEX idx_inference_tenant ON inference_requests(tenant_id);
CREATE INDEX idx_inference_model ON inference_requests(model_id);
CREATE INDEX idx_inference_status ON inference_requests(status);
CREATE INDEX idx_inference_created_at ON inference_requests(created_at DESC);
CREATE INDEX idx_inference_request_id ON inference_requests(request_id);

-- Create view for analytics
CREATE OR REPLACE VIEW inference_analytics AS
SELECT 
    tenant_id,
    model_id,
    request_type,
    DATE_TRUNC('hour', created_at) AS hour,
    COUNT(*) AS request_count,
    SUM(tokens_in) AS total_tokens_in,
    SUM(tokens_out) AS total_tokens_out,
    SUM(cost_usd) AS total_cost_usd,
    AVG(latency_ms) AS avg_latency_ms,
    PERCENTILE_CONT(0.5) WITHIN GROUP (ORDER BY latency_ms) AS p50_latency_ms,
    PERCENTILE_CONT(0.95) WITHIN GROUP (ORDER BY latency_ms) AS p95_latency_ms,
    COUNT(CASE WHEN status = 'FAILED' THEN 1 END) AS failed_count
FROM inference_requests
WHERE status IN ('SUCCESS', 'FAILED')
GROUP BY tenant_id, model_id, request_type, DATE_TRUNC('hour', created_at);

-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Trigger for models table
CREATE TRIGGER update_models_updated_at BEFORE UPDATE ON models
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Comments
COMMENT ON TABLE models IS 'Registry of available models and their metadata';
COMMENT ON TABLE model_audit IS 'Audit trail for model changes';
COMMENT ON TABLE inference_requests IS 'Log of all inference requests for tracking and analytics';
COMMENT ON VIEW inference_analytics IS 'Aggregated analytics for inference requests';