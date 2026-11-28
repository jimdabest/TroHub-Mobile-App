CREATE TABLE service_price_tier (
    service_price_id UUID           NOT NULL REFERENCES service_price (id),
    tier_order       INTEGER        NOT NULL,
    up_to            NUMERIC(14, 2),
    unit_price       NUMERIC(14, 2) NOT NULL,
    PRIMARY KEY (service_price_id, tier_order)
);
