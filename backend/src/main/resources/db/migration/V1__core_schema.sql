-- V1: nguoi dung, nha tro, phan quyen theo nha tro, phong, nguoi thue, hop dong, bang gia dich vu.
-- Quy uoc: id UUID sinh o ung dung; created_at/updated_at/deleted phuc vu dong bo (pull theo updated_at,
-- xoa mem de thiet bi khac nhan duoc "tombstone").

CREATE TABLE users (
    id          UUID PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL,
    password    VARCHAR(100) NOT NULL,
    full_name   VARCHAR(100) NOT NULL,
    phone       VARCHAR(20),
    email       VARCHAR(150),
    role        VARCHAR(20)  NOT NULL,
    enabled     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ  NOT NULL,
    updated_at  TIMESTAMPTZ  NOT NULL,
    deleted     BOOLEAN      NOT NULL DEFAULT FALSE,
    CONSTRAINT uk_users_username UNIQUE (username)
);

CREATE TABLE invalidated_token (
    id           VARCHAR(64) PRIMARY KEY,   -- jti cua JWT
    expiry_time  TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_invalidated_token_expiry ON invalidated_token (expiry_time);

CREATE TABLE property (
    id           UUID PRIMARY KEY,
    name         VARCHAR(150) NOT NULL,
    address      VARCHAR(255),
    description  VARCHAR(500),
    created_at   TIMESTAMPTZ  NOT NULL,
    updated_at   TIMESTAMPTZ  NOT NULL,
    deleted      BOOLEAN      NOT NULL DEFAULT FALSE
);

-- Phan quyen theo nha tro: OWNER (chu tro) / MANAGER (nguoi quan ly)
CREATE TABLE property_member (
    id           UUID PRIMARY KEY,
    property_id  UUID        NOT NULL REFERENCES property (id),
    user_id      UUID        NOT NULL REFERENCES users (id),
    role         VARCHAR(20) NOT NULL,
    created_at   TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_property_member UNIQUE (property_id, user_id)
);
CREATE INDEX idx_property_member_user ON property_member (user_id);

CREATE TABLE room (
    id           UUID PRIMARY KEY,
    property_id  UUID          NOT NULL REFERENCES property (id),
    code         VARCHAR(20)   NOT NULL,           -- so phong, vd "101"
    block        VARCHAR(50),                      -- day / khu, dung cho ty le lap day theo day
    floor        INTEGER,
    area_m2      NUMERIC(6, 2),
    price        NUMERIC(14, 2) NOT NULL,          -- gia thue niem yet
    status       VARCHAR(20)   NOT NULL,           -- AVAILABLE | OCCUPIED | MAINTENANCE
    description  VARCHAR(500),
    created_at   TIMESTAMPTZ   NOT NULL,
    updated_at   TIMESTAMPTZ   NOT NULL,
    deleted      BOOLEAN       NOT NULL DEFAULT FALSE
);
CREATE UNIQUE INDEX uk_room_property_code ON room (property_id, code) WHERE deleted = FALSE;
CREATE INDEX idx_room_sync ON room (property_id, updated_at);

CREATE TABLE tenant (
    id           UUID PRIMARY KEY,
    property_id  UUID         NOT NULL REFERENCES property (id),
    full_name    VARCHAR(100) NOT NULL,
    phone        VARCHAR(20),
    id_number    VARCHAR(20),                      -- CCCD/CMND
    note         VARCHAR(500),
    created_at   TIMESTAMPTZ  NOT NULL,
    updated_at   TIMESTAMPTZ  NOT NULL,
    deleted      BOOLEAN      NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_tenant_sync ON tenant (property_id, updated_at);

CREATE TABLE contract (
    id            UUID PRIMARY KEY,
    property_id   UUID           NOT NULL REFERENCES property (id),
    room_id       UUID           NOT NULL REFERENCES room (id),
    tenant_id     UUID           NOT NULL REFERENCES tenant (id),
    start_date    DATE           NOT NULL,
    end_date      DATE,
    monthly_rent  NUMERIC(14, 2) NOT NULL,
    deposit       NUMERIC(14, 2) NOT NULL DEFAULT 0,
    status        VARCHAR(20)    NOT NULL,         -- ACTIVE | ENDED
    note          VARCHAR(500),
    created_at    TIMESTAMPTZ    NOT NULL,
    updated_at    TIMESTAMPTZ    NOT NULL,
    deleted       BOOLEAN        NOT NULL DEFAULT FALSE
);
-- Mot phong chi co toi da mot hop dong dang hieu luc
CREATE UNIQUE INDEX uk_contract_active_room ON contract (room_id) WHERE status = 'ACTIVE' AND deleted = FALSE;
CREATE INDEX idx_contract_sync ON contract (property_id, updated_at);

-- Bang gia va dich vu (dien, nuoc, rac, internet, ...)
CREATE TABLE service_price (
    id           UUID PRIMARY KEY,
    property_id  UUID           NOT NULL REFERENCES property (id),
    name         VARCHAR(100)   NOT NULL,
    type         VARCHAR(20)    NOT NULL,          -- ELECTRIC | WATER | FIXED | PER_PERSON
    unit         VARCHAR(20)    NOT NULL,          -- kWh, m3, thang, nguoi
    unit_price   NUMERIC(14, 2) NOT NULL,
    active       BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMPTZ    NOT NULL,
    updated_at   TIMESTAMPTZ    NOT NULL,
    deleted      BOOLEAN        NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_service_price_sync ON service_price (property_id, updated_at);
