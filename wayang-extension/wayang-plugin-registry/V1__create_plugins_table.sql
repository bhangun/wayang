
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Plugins table
CREATE TABLE plugins (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    plugin_id VARCHAR(255) NOT NULL,
    version VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'pending',
    status_reason VARCHAR(500),
    descriptor JSONB NOT NULL,
    checksum VARCHAR(150) NOT NULL,
    signature TEXT,
    published_by VARCHAR(255),
    tenant_id VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    metadata JSONB,
    
    CONSTRAINT unique_plugin_version UNIQUE (plugin_id, version),
    CONSTRAINT valid_status CHECK (status IN ('pending', 'scanning', 'approved', 'rejected', 'revoked', 'deprecated'))
);

-- Indexes
CREATE INDEX idx_plugins_plugin_id ON plugins(plugin_id);
CREATE INDEX idx_plugins_status ON plugins(status);
CREATE INDEX idx_plugins_tenant_id ON plugins(tenant_id);
CREATE INDEX idx_plugins_created_at ON plugins(created_at DESC);

-- JSONB indexes for querying capabilities
CREATE INDEX idx_plugins_capabilities ON plugins USING GIN ((descriptor->'capabilities'));

-- Audit table
CREATE TABLE plugin_audit_events (
    audit_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    event_type VARCHAR(50) NOT NULL,
    plugin_id VARCHAR(255) NOT NULL,
    version VARCHAR(50),
    tenant_id VARCHAR(255),
    actor_type VARCHAR(20) NOT NULL,
    actor_id VARCHAR(255) NOT NULL,
    actor_name VARCHAR(255),
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    details JSONB,
    error JSONB,
    hash VARCHAR(100) NOT NULL,
    signature TEXT,
    
    CONSTRAINT valid_event_type CHECK (event_type IN (
        'PLUGIN_REGISTERED', 'PLUGIN_SCANNED', 'PLUGIN_APPROVED',
        'PLUGIN_REJECTED', 'PLUGIN_LOADED', 'PLUGIN_UNLOADED',
        'PLUGIN_REVOKED', 'PLUGIN_ERROR', 'PLUGIN_UPDATED'
    ))
);

-- Audit indexes
CREATE INDEX idx_audit_plugin_id ON plugin_audit_events(plugin_id);
CREATE INDEX idx_audit_event_type ON plugin_audit_events(event_type);
CREATE INDEX idx_audit_timestamp ON plugin_audit_events(timestamp DESC);
CREATE INDEX idx_audit_tenant_id ON plugin_audit_events(tenant_id);

-- Trigger to update updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_plugins_updated_at
    BEFORE UPDATE ON plugins
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Comments
COMMENT ON TABLE plugins IS 'Central registry of all plugin metadata';
COMMENT ON TABLE plugin_audit_events IS 'Immutable audit log of all plugin operations';
COMMENT ON COLUMN plugins.descriptor IS 'Full plugin descriptor as JSONB';
COMMENT ON COLUMN plugin_audit_events.hash IS 'Blake3 hash for tamper detection';
