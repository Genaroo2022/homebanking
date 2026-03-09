CREATE TABLE IF NOT EXISTS bill_payments (
    id UUID PRIMARY KEY,
    account_id UUID NOT NULL,
    biller_code VARCHAR(40) NOT NULL,
    reference VARCHAR(80) NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    idempotency_key VARCHAR(64) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL,
    failure_reason VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    processed_at TIMESTAMP,
    version BIGINT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_bill_payment_account ON bill_payments(account_id);
CREATE INDEX IF NOT EXISTS idx_bill_payment_created_at ON bill_payments(created_at);

CREATE TABLE IF NOT EXISTS cards (
    id UUID PRIMARY KEY,
    account_id UUID NOT NULL,
    encrypted_number VARCHAR(512) NOT NULL,
    encrypted_cvv VARCHAR(512) NOT NULL,
    last4 VARCHAR(4) NOT NULL,
    card_holder VARCHAR(120) NOT NULL,
    from_date DATE NOT NULL,
    thru_date DATE NOT NULL,
    type VARCHAR(20) NOT NULL,
    color VARCHAR(20) NOT NULL,
    active BOOLEAN NOT NULL,
    version BIGINT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_cards_account_id ON cards(account_id);
CREATE INDEX IF NOT EXISTS idx_cards_last4 ON cards(last4);

