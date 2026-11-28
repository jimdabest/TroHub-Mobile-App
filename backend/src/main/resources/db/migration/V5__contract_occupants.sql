ALTER TABLE contract ADD COLUMN occupants INTEGER NOT NULL DEFAULT 1;
ALTER TABLE contract ADD CONSTRAINT ck_contract_occupants CHECK (occupants >= 1);
