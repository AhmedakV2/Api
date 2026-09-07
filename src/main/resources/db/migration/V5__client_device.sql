CREATE TABLE client_device (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id UUID NOT NULL REFERENCES organization(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES user_account(id) ON DELETE CASCADE,
    hostname VARCHAR(160) NOT NULL,
    os VARCHAR(64) NOT NULL,
    app_version VARVHAR(32) NOT NULL,
    api_key_id UUID REFERENCES api_key(id) ON DELETE SET NULL,
    last_seen_at TIMESTAMPTZ,
    status VARCHAR(16) NOT NULL DEFAULT 'OFFLINE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ck_device_status CHECK (status IN ('ONLINE', 'OFFLINE', 'BLOCKED'))
);
CREATE TABLE device_capability (
    device_id UUID NOT NULL REFERENCES client_device(id) ON DELETE CASCADE,
    tool_name VARCHAR(64) NOT NULL,
    schema_version INT NOT NULL DEFAULT 1,
    enabled BOOLEAN NOT NULL DEFAULT true,
    PRIMARY KEY (device_id, tool_name)
);