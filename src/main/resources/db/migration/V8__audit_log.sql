CREATE TABLE audit_log (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    actor_id UUID,
    action VARCHAR(64) NOT NULL,
    entity_type VARCHAR(64) NOT NULL,
    entity_id VARCHAR(64),
    ip INET,
    detail_json JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (id, created_at)
) PARTITION BY RANGE (created_at);
CREATE TABLE audit_log_default PARTITION OF audit_log DEFAULT;

CREATE RULE audit_log_no_update AS ON UPDATE TO audit_log DO INSTEAD NOTHING;
CREATE RULE audit_log_no_delete AS ON DELETE TO audit_log DO INSTEAD NOTHING;

CREATE TABLE system_setting (
    key VARCHAR(96) PRIMARY KEY,
    value VARCHAR(512) NOT NULL,
    update_by UUID,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);