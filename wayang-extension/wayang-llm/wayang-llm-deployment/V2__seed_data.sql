-- Seed default models

-- Ollama models
INSERT INTO models (model_id, name, version, provider, type, capabilities, max_tokens, max_output_tokens, status, owner)
VALUES 
    ('llama3-8b', 'Llama 3 8B', '1.0', 'ollama', 'llm', 
     ARRAY['chat', 'completion', 'streaming'], 8192, 4096, 'ACTIVE', 'system'),
    
    ('llama3-70b', 'Llama 3 70B', '1.0', 'ollama', 'llm', 
     ARRAY['chat', 'completion', 'streaming'], 8192, 4096, 'ACTIVE', 'system'),
    
    ('mistral-7b', 'Mistral 7B', '1.0', 'ollama', 'llm', 
     ARRAY['chat', 'completion', 'streaming'], 8192, 4096, 'ACTIVE', 'system');

-- OpenAI models (if API key provided)
INSERT INTO models (model_id, name, version, provider, type, capabilities, max_tokens, max_output_tokens, latency_profile, cost_profile, status, owner)
VALUES 
    ('gpt-4-turbo', 'GPT-4 Turbo', '1.0', 'openai', 'llm',
     ARRAY['chat', 'completion', 'streaming', 'function_calling', 'vision', 'json_mode'], 128000, 4096,
     '{"p50Ms": 500, "p95Ms": 2000, "p99Ms": 5000, "avgMs": 800}'::JSONB,
     '{"perInputToken": 0.00001, "perOutputToken": 0.00003}'::JSONB,
     'ACTIVE', 'system'),
    
    ('gpt-3.5-turbo', 'GPT-3.5 Turbo', '1.0', 'openai', 'llm',
     ARRAY['chat', 'completion', 'streaming', 'function_calling'], 16385, 4096,
     '{"p50Ms": 200, "p95Ms": 800, "p99Ms": 2000, "avgMs": 400}'::JSONB,
     '{"perInputToken": 0.0000005, "perOutputToken": 0.0000015}'::JSONB,
     'ACTIVE', 'system'),
    
    ('text-embedding-ada-002', 'Text Embedding Ada 002', '1.0', 'openai', 'embedding',
     ARRAY['embedding'], 8191, NULL,
     '{"p50Ms": 100, "p95Ms": 300, "p99Ms": 500, "avgMs": 150}'::JSONB,
     '{"perInputToken": 0.0000001}'::JSONB,
     'ACTIVE', 'system');

-- Add tags
UPDATE models SET tags = ARRAY['fast', 'local', 'opensource'] WHERE provider = 'ollama';
UPDATE models SET tags = ARRAY['cloud', 'production', 'high-quality'] WHERE provider = 'openai' AND model_id LIKE 'gpt-4%';
UPDATE models SET tags = ARRAY['cloud', 'cost-effective', 'fast'] WHERE model_id = 'gpt-3.5-turbo';
UPDATE models SET tags = ARRAY['embedding', 'fast'] WHERE model_id = 'text-embedding-ada-002';