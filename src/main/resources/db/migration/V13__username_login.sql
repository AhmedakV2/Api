ALTER TABLE user_account ADD COLUMN username CITEXT;

WITH numbered AS (
    SELECT id,
           regexp_replace(lower(split_part(email::text, '@', 1)), '[^a-z0-9._-]', '', 'g') AS base,
           row_number() OVER (
               PARTITION BY regexp_replace(lower(split_part(email::text, '@', 1)), '[^a-z0-9._-]', '', 'g')
               ORDER BY created_at, id
           ) AS position
    FROM user_account
)
UPDATE user_account u
SET username = CASE
                   WHEN numbered.base = '' THEN 'kullanici' || numbered.position
                   WHEN numbered.position = 1 THEN numbered.base
                   ELSE numbered.base || numbered.position
               END
FROM numbered
WHERE u.id = numbered.id;

ALTER TABLE user_account ALTER COLUMN username SET NOT NULL;
ALTER TABLE user_account ADD CONSTRAINT uq_user_account_username UNIQUE (username);
ALTER TABLE user_account ADD CONSTRAINT ck_user_username_format CHECK (username ~ '^[A-Za-z0-9._-]{3,64}$');

CREATE INDEX ix_user_account_username ON user_account (username);
