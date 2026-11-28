ALTER TABLE tenant
    ADD COLUMN user_id            UUID REFERENCES users (id),
    ADD COLUMN invite_code_hash   VARCHAR(64),
    ADD COLUMN invite_expires_at  TIMESTAMPTZ;

ALTER TABLE tenant ADD CONSTRAINT ck_tenant_invite_pair
    CHECK ((invite_code_hash IS NULL) = (invite_expires_at IS NULL));

CREATE INDEX idx_tenant_user ON tenant (user_id) WHERE deleted = FALSE AND user_id IS NOT NULL;
CREATE UNIQUE INDEX uk_tenant_invite_code ON tenant (invite_code_hash) WHERE invite_code_hash IS NOT NULL;
