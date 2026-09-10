ALTER TABLE refresh_token ADD COLUMN replaced_by uuid REFERENCES refresh_token (id) ON DELETE SET NULL;
ALTER TABLE refresh_token ADD COLUMN family_id uuid NOT NULL DEFAULT gen_random_uuid();

CREATE INDEX ix_refresh_token_family ON refresh_token (family_id);

CREATE UNIQUE INDEX uq_client_device_api_key ON client_device (api_key_id) WHERE api_key_id IS NOT NULL;

CREATE OR REPLACE FUNCTION ensure_audit_partition(target date)
RETURNS void AS $$
DECLARE
    start_date date := date_trunc('month', target)::date;
    end_date   date := (date_trunc('month', target) + interval '1 month')::date;
    part_name  text := 'audit_log_' || to_char(start_date, 'YYYY_MM');
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_class WHERE relname = part_name) THEN
        EXECUTE format('CREATE TABLE %I PARTITION OF audit_log FOR VALUES FROM (%L) TO (%L)',
                       part_name, start_date, end_date);
    END IF;
END;
$$ LANGUAGE plpgsql;

SELECT ensure_audit_partition(current_date);
SELECT ensure_audit_partition((current_date + interval '1 month')::date);
SELECT ensure_audit_partition((current_date + interval '2 month')::date);
