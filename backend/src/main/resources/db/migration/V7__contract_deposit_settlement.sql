ALTER TABLE contract
    ADD COLUMN deposit_refunded NUMERIC(14, 2),
    ADD COLUMN deposit_deducted NUMERIC(14, 2),
    ADD COLUMN deposit_note     VARCHAR(500);

ALTER TABLE contract ADD CONSTRAINT ck_contract_deposit_pair
    CHECK ((deposit_refunded IS NULL) = (deposit_deducted IS NULL));
ALTER TABLE contract ADD CONSTRAINT ck_contract_deposit_settlement
    CHECK (deposit_refunded IS NULL
           OR (deposit_refunded >= 0 AND deposit_deducted >= 0 AND deposit_refunded + deposit_deducted <= deposit));
