CREATE TABLE role (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(32) NOT NULL UNIQUE,
    description VARCHAR(255) NOT NULL
);
INSERT INTO role (code, description) VALUES
    ('OWNER', 'Organizasyon yonetimi, uye ekleme ve cikarma, token butcesini belirleme'),
    ('ADMIN', 'Kullanici yonetimi, anahtar uretimi ve iptali, sistem ayarlari'),
    ('USER', 'Agent oturu acma, kendi profil ve tercihlerini duzenleme'),
    ('VIWER', 'Yalnizca okuma; agent oturumu acamamz');

CREATE TABLE user_account (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email CITEXT NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    display_name VARCHAR(120) NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    locale VARCHAR(8) NOT NULL DEFAULT 'tr',
    mfa_enabled BOOLEAN NOT NULL DEFAULT false,
    last_login_at timestamptz,
    created_at timestamptz NOT NULL DEFAULT now(),
    created_by uuid,
    updated_at timestamptz NOT NULL DEFAULT now(),
    updated_by uuid,
    version bigint NOT NULL DEFAULT 0,
    CONSTRAINT ck_user_status CHECK (status IN ('ACTIVE', 'DISABLED', 'LOCKED'))
);
CREATE TABLE user_role (
    user_id UUID NOT NULL REFERENCES user_account(id) ON DELETE CASCADE,
    role_id UUID NOT NULL REFERENCES role(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);
CREATE TABLE user_preference (
    user_id UUID NOT NULL REFERENCES user_account(id) ON DELETE CASCADE,
    pref_key VARCHAR(64) NOT NULL,
    pref_value VARCHAR(512) NOT NULL,
    update_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (user_id, pref_key)
);
CREATE TABLE password_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES user_account(id) ON DELETE CASCADE,
    password_hash VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE TABLE login_attempt (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email CITEXT NOT NULL,
    ip_ INNET,
    success BOOLEAN NOT NULL,
    attempted_at TIMESTAMPTZ NOT NULL DEFAULT now()
);