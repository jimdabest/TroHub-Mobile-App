ALTER TABLE tenant ADD COLUMN phone_normalized VARCHAR(20);

UPDATE tenant t
SET phone_normalized = CASE
        WHEN d.v IS NULL OR length(d.v) < 9 THEN NULL
        WHEN d.v ~ '^\+84[0-9]{9}$' THEN '0' || substr(d.v, 4)
        WHEN d.v ~ '^84[0-9]{9}$'   THEN '0' || substr(d.v, 3)
        ELSE d.v
    END
FROM (SELECT id, regexp_replace(phone, '[^0-9+]', '', 'g') AS v FROM tenant WHERE phone IS NOT NULL) d
WHERE t.id = d.id;

CREATE INDEX idx_tenant_phone_normalized ON tenant (phone_normalized)
    WHERE deleted = FALSE AND phone_normalized IS NOT NULL;
