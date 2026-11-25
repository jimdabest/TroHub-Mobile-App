ALTER TABLE property
    ADD COLUMN bank_name           VARCHAR(100),
    ADD COLUMN bank_account_number VARCHAR(30),
    ADD COLUMN bank_account_holder VARCHAR(100);

ALTER TABLE property
    ADD CONSTRAINT ck_property_bank_all_or_none CHECK (
        (bank_name IS NULL) = (bank_account_number IS NULL)
        AND (bank_name IS NULL) = (bank_account_holder IS NULL)
    );
