
CREATE TABLE transfers (
                           id BIGSERIAL PRIMARY KEY,
                           source_account_id BIGINT NOT NULL,
                           destination_account_id BIGINT,
                           destination_cbu VARCHAR(22) NOT NULL,
                           amount DECIMAL(19, 2) NOT NULL,
                           description VARCHAR(200) NOT NULL,
                           status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                           created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           executed_at TIMESTAMP,
                           failure_reason VARCHAR(500),

                           FOREIGN KEY (source_account_id) REFERENCES accounts(id) ON DELETE RESTRICT,
                           FOREIGN KEY (destination_account_id) REFERENCES accounts(id) ON DELETE RESTRICT,

                           CHECK (amount > 0),
                           CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED', 'REJECTED'))
);

CREATE INDEX idx_source_account ON transfers(source_account_id);
CREATE INDEX idx_destination_account ON transfers(destination_account_id);
CREATE INDEX idx_status ON transfers(status);
CREATE INDEX idx_created_at ON transfers(created_at);

CREATE TABLE transfer_idempotency_keys (
                                           id BIGSERIAL PRIMARY KEY,
                                           idempotency_key VARCHAR(255) NOT NULL UNIQUE,
                                           transfer_id BIGINT NOT NULL,
                                           created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                           FOREIGN KEY (transfer_id) REFERENCES transfers(id) ON DELETE CASCADE
);

CREATE INDEX idx_idempotency_key ON transfer_idempotency_keys(idempotency_key);