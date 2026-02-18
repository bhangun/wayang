-- Initial data for Wayang Standalone Runtime

-- Insert default tenant if multitenancy is enabled
INSERT INTO tenant (tenant_id, name, description, status, created_at, updated_at) VALUES
('default-tenant', 'Default Tenant', 'Default tenant for standalone mode', 'ACTIVE', NOW(), NOW())
ON CONFLICT DO NOTHING;

-- Insert default project
INSERT INTO wayang_project (project_id, tenant_id, project_name, description, project_type, created_by, created_at, updated_at, is_active) VALUES
('default-project', 'default-tenant', 'Default Project', 'Default project for standalone mode', 'WORKFLOW', 'system', NOW(), NOW(), true)
ON CONFLICT DO NOTHING;

-- Insert any other initial data as needed