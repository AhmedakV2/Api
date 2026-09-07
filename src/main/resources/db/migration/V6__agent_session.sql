CREATE TABLE agent_session (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id UUID NOT NULL REFERENCES organization(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES user_account(id) ON DELETE CASCADE,
    device_id UUID NOT NULL REFERENCES client_device(id) ON DELETE SET NULL,
    title VARCHAR(200) NOT NULL DEFAULT '',
    mode VARCHAR(32) NOT NULL DEFAULT 'CHAT',
    model VARCHAR(80) NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'OPEN',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    closed_at TIMESTAMPTZ,
    CONSTRAINT ck_session_status CHECK (status IN ('OPEN', 'CLOSED', 'FAILED'))
);
CREATE TABLE agent_message (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    session_id UUID NOT NULL REFERENCES agent_session(id) ON DELETE CASCADE,
    seq INT NOT NULL,
    role VARCHAR(16) NOT NULL,
    content TEXT NOT NULL,
    token_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY(session_id, seq),
    CONSTRAINT uq_agent_message_id UNIQUE (id),
    CONSTRAINT ck_message_role CHECK (role IN ('SYSTEM', 'USER', 'ASSISTANT', 'TOOL'))
);
CREATE TABLE agent_tool_call (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    message_id UUID NOT NULL REFERENCES agent_message(id) ON DELETE CASCADE,
    device_id UUID REFERENCES client_device(id) ON DELETE SET NULL,
    tool_name VARCHAR(64) NOT NULL,
    arguments_json JSONB NOT NULL DEFAULT '{}'::jsonb,
    result_summary VARCHAR(2048),
    duration_ms INT,
    ok BOOLEAN NOT NULL DEFAULT false,
    error VARCHAR(512),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE TABLE model_usage (
    id UUUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id UUID NOT NULL REFERENCES organization(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES user_account(id) ON DELETE CASCADE,
    session_id UUID REFERENCES agent_session(id) ON DELETE SET NULL,
    model VARCHAR(80) NOT NULL,
    token_int INT NOT NULL DEFAULT 0,
    token_out INT NOT NULL DEFAULT 0,
    cost NUMERIC(12, 6) NOT NULL DEFAULT 0,
    recorded_At TIMESTAMPTZ NOT NULL DEFAULT now()
);