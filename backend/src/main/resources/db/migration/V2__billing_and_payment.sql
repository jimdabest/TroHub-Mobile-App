-- V2: chi so dien nuoc, hoa don, chi tiet hoa don, thanh toan, giao dich VNPay/MoMo.
-- Moi tao schema; API cho cac bang nay lam o buoc tiep theo.

CREATE TABLE meter_reading (
    id                UUID PRIMARY KEY,
    property_id       UUID           NOT NULL REFERENCES property (id),
    room_id           UUID           NOT NULL REFERENCES room (id),
    service_price_id  UUID           NOT NULL REFERENCES service_price (id),
    period            DATE           NOT NULL,     -- ngay dau thang cua ky ghi
    previous_value    NUMERIC(12, 2) NOT NULL,
    current_value     NUMERIC(12, 2) NOT NULL,
    read_at           TIMESTAMPTZ,
    created_at        TIMESTAMPTZ    NOT NULL,
    updated_at        TIMESTAMPTZ    NOT NULL,
    deleted           BOOLEAN        NOT NULL DEFAULT FALSE,
    CONSTRAINT ck_meter_reading_value CHECK (current_value >= previous_value)
);
CREATE UNIQUE INDEX uk_meter_reading ON meter_reading (room_id, service_price_id, period) WHERE deleted = FALSE;
CREATE INDEX idx_meter_reading_sync ON meter_reading (property_id, updated_at);

CREATE TABLE invoice (
    id            UUID PRIMARY KEY,
    property_id   UUID           NOT NULL REFERENCES property (id),
    room_id       UUID           NOT NULL REFERENCES room (id),
    contract_id   UUID           REFERENCES contract (id),
    period        DATE           NOT NULL,
    issue_date    DATE           NOT NULL,
    due_date      DATE,
    total_amount  NUMERIC(14, 2) NOT NULL DEFAULT 0,
    paid_amount   NUMERIC(14, 2) NOT NULL DEFAULT 0,
    status        VARCHAR(20)    NOT NULL,         -- DRAFT | ISSUED | PARTIALLY_PAID | PAID | CANCELLED
    note          VARCHAR(500),
    created_at    TIMESTAMPTZ    NOT NULL,
    updated_at    TIMESTAMPTZ    NOT NULL,
    deleted       BOOLEAN        NOT NULL DEFAULT FALSE
);
CREATE UNIQUE INDEX uk_invoice_room_period ON invoice (room_id, period) WHERE deleted = FALSE AND status <> 'CANCELLED';
CREATE INDEX idx_invoice_sync ON invoice (property_id, updated_at);

CREATE TABLE invoice_item (
    id           UUID PRIMARY KEY,
    invoice_id   UUID           NOT NULL REFERENCES invoice (id),
    item_type    VARCHAR(20)    NOT NULL,          -- RENT | ELECTRIC | WATER | SERVICE | OTHER
    description  VARCHAR(200)   NOT NULL,
    quantity     NUMERIC(12, 2) NOT NULL DEFAULT 1,
    unit_price   NUMERIC(14, 2) NOT NULL,
    amount       NUMERIC(14, 2) NOT NULL,
    created_at   TIMESTAMPTZ    NOT NULL,
    updated_at   TIMESTAMPTZ    NOT NULL,
    deleted      BOOLEAN        NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_invoice_item_invoice ON invoice_item (invoice_id);

-- Ghi nhan thanh toan (tien mat, chuyen khoan, hoac ket qua tu cong thanh toan)
CREATE TABLE payment (
    id           UUID PRIMARY KEY,
    invoice_id   UUID           NOT NULL REFERENCES invoice (id),
    amount       NUMERIC(14, 2) NOT NULL,
    method       VARCHAR(20)    NOT NULL,          -- CASH | BANK_TRANSFER | VNPAY | MOMO
    paid_at      TIMESTAMPTZ    NOT NULL,
    note         VARCHAR(500),
    created_at   TIMESTAMPTZ    NOT NULL,
    updated_at   TIMESTAMPTZ    NOT NULL,
    deleted      BOOLEAN        NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_payment_invoice ON payment (invoice_id);

-- Giao dich qua cong thanh toan; txn_ref la ma doi soat gui sang VNPay/MoMo, duy nhat de IPN idempotent
CREATE TABLE payment_transaction (
    id                UUID PRIMARY KEY,
    invoice_id        UUID           NOT NULL REFERENCES invoice (id),
    provider          VARCHAR(20)    NOT NULL,     -- VNPAY | MOMO
    txn_ref           VARCHAR(64)    NOT NULL,
    amount            NUMERIC(14, 2) NOT NULL,
    status            VARCHAR(20)    NOT NULL,     -- PENDING | SUCCESS | FAILED
    provider_txn_no   VARCHAR(64),
    raw_ipn           TEXT,
    created_at        TIMESTAMPTZ    NOT NULL,
    updated_at        TIMESTAMPTZ    NOT NULL,
    CONSTRAINT uk_payment_transaction_ref UNIQUE (provider, txn_ref)
);
CREATE INDEX idx_payment_transaction_invoice ON payment_transaction (invoice_id);
