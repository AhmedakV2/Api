ALTER TABLE user_account ADD COLUMN username CITEXT;

UPDATE user_account u
SET username = numbered.candidate
FROM (
    SELECT id,
           CASE WHEN seq = 1 THEN base ELSE base || '-' || left(md5(id::text), 6) END AS candidate
    FROM (
        SELECT id,
               base,
               row_number() OVER (PARTITION BY base ORDER BY created_at, id) AS seq
        FROM (
            SELECT id,
                   created_at,
                   COALESCE(
                       NULLIF(left(regexp_replace(lower(split_part(email::text, '@', 1)),
                                                  '[^a-z0-9._-]', '', 'g'), 50), ''),
                       'kullanici'
                   ) AS base
            FROM user_account
        ) cleaned
    ) ranked
) numbered
WHERE u.id = numbered.id;

ALTER TABLE user_account ALTER COLUMN username SET NOT NULL;
ALTER TABLE user_account ADD CONSTRAINT uq_user_account_username UNIQUE (username);
ALTER TABLE user_account ADD CONSTRAINT ck_user_username_format
    CHECK (username ~ '^[A-Za-z0-9._-]{1,64}$');

CREATE INDEX ix_user_account_username ON user_account (username);
