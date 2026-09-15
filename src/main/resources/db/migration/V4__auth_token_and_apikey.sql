CREATE TABLE refresh_token (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES user_account(id) ON DELETE CASCADE,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ,
    user_agent VARCHAR(255),
    ip INET,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE TABLE api_key (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id UUID NOT NULL REFERENCES user_account(id) ON DELETE CASCADE,
    org_id UUID NOT NULL REFERENCES organization(id) ON DELETE CASCADE,
    name VARCHAR(120) NOT NULL,
    key_hash VARCHAR(64) NOT NULL UNIQUE,
    scopes VARCHAR(255) NOT NULL DEFAULT '',
    expires_at TIMESTAMPTZ,
    revoked_at TIMESTAMPTZ,
    last_used_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);