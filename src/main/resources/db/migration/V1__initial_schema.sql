CREATE TABLE partners (
    id BIGSERIAL PRIMARY KEY,
    external_id VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    credit_balance DECIMAL(19, 2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Create indexes on partners table
CREATE INDEX idx_partner_external_id ON partners(external_id);

-- Transactions table
CREATE TABLE transactions (
                              id BIGSERIAL PRIMARY KEY,
                              transaction_id UUID NOT NULL UNIQUE,
                              partner_id BIGINT NOT NULL REFERENCES partners(id),
                              type VARCHAR(20) NOT NULL,
                              amount DECIMAL(19, 2) NOT NULL,
                              description TEXT NOT NULL,
                              status VARCHAR(20) NOT NULL,
                              chave_key VARCHAR(100),
                              created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                              updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
                              completed_at TIMESTAMP,
                              failure_reason TEXT,
                              CONSTRAINT ck_transaction_type CHECK (type IN ('CREDIT', 'DEBIT')),
                              CONSTRAINT ck_transaction_status CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED'))
);

-- Create indexes on transactions table
CREATE INDEX idx_transaction_partner_id ON transactions(partner_id);
CREATE INDEX idx_transaction_chave_key ON transactions(chave_key);
CREATE INDEX idx_transaction_status ON transactions(status);
CREATE INDEX idx_transaction_created_at ON transactions(created_at);
