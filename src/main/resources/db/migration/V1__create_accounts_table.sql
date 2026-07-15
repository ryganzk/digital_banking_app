CREATE TABLE accounts (
    id              UUID PRIMARY KEY,
    account_number  VARCHAR(20) NOT NULL UNIQUE,
    owner_name      VARCHAR(100) NOT NULL,
    balance         NUMERIC(19, 4) NOT NULL DEFAULT 0,
    currency        CHAR(3) NOT NULL DEFAULT 'USD',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);