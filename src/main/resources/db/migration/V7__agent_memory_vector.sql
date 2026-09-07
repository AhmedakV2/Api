CREATE TABLE agent_memory (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id UUID NOT NULL REFERENCES organization(id) ON DELETE CASCADE,
    kind VARCHAR(32) NOT NULL,
    source_ref VARCHAR(200),
    content TEXT NOT NULL,
    embedding VECTOR(1536) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);


CREATE INDEX ix_agent_memory_embedding ON agent_memory
    USING ivfflat (embedding vector_cosine_ops) WITH (lists = 100);
