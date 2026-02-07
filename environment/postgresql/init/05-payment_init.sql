-- Create Database e_payment_service
\c e_payment_service;

-- Đảm bảo extension pgcrypto có sẵn để xử lý UUID nếu cần
CREATE EXTENSION IF NOT EXISTS "pgcrypto";
CREATE EXTENSION IF NOT EXISTS "unaccent";
CREATE SEQUENCE IF NOT EXISTS payment_transaction_seq START 1;

-- 1. Table: payment_methods
CREATE TABLE IF NOT EXISTS payment_methods (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    status VARCHAR(30) CHECK (status IN ('ACTIVE', 'INACTIVE', 'MAINTENANCE')),
    activated BOOLEAN DEFAULT TRUE,

    -- Audit fields
    created_by VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Table: payments
CREATE TABLE IF NOT EXISTS payments (
    id UUID NOT NULL,
    amount DECIMAL(19, 2) NOT NULL,
    status VARCHAR(50) CHECK (status IN ('PENDING', 'SUCCESS', 'FAILED')),
    order_id UUID NOT NULL,
    payment_method_id BIGINT,
    activated BOOLEAN DEFAULT TRUE,
    paid_at TIMESTAMP,

    -- Audit fields
    created_by VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id, created_at), -- Partition key on created_at for time-based partitioning
    CONSTRAINT fk_payment_method FOREIGN KEY (payment_method_id) REFERENCES payment_methods(id)
) PARTITION BY RANGE (created_at);

-- Index for optimization
CREATE INDEX idx_payments_order_id ON payments(order_id);

-- Comment
COMMENT ON COLUMN payments.status IS 'Save status of Payment including: PENDING, SUCCESS, FAILED';

-- 3. Table: payment_transactions (Partition theo tháng)
CREATE TABLE IF NOT EXISTS payment_transactions (
    id BIGSERIAL NOT NULL,
    transaction_id VARCHAR(100) NOT NULL,
    status VARCHAR(50) CHECK (status IN ('PENDING', 'SUCCESS', 'FAILED')),
    raw_response TEXT,
    note TEXT,
    payment_id UUID NOT NULL,
    event_id UUID NOT NULL,
    
    -- Audit fields
    created_by VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id, created_at),
    CONSTRAINT fk_transaction_payment FOREIGN KEY (payment_id, created_at) REFERENCES payments (id, created_at) ON DELETE CASCADE
) PARTITION BY RANGE (created_at);

-- Comment
COMMENT ON COLUMN payment_transactions.status IS 'Save status of Payment including: PENDING, SUCCESS, FAILED';

INSERT INTO payment_methods (code, name, status, activated, created_by, created_at, updated_by, updated_at) VALUES
('momo', 'Momo', 'ACTIVE', TRUE, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
('vnpay', 'VNPAY', 'ACTIVE', TRUE, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
('cod', 'Cash on Delivery', 'ACTIVE', TRUE, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);

ALTER TABLE payment_transactions ALTER COLUMN id TYPE BIGINT;
ALTER TABLE payment_transactions ALTER COLUMN id SET DEFAULT nextval('payment_transaction_seq');

-- 4. Table partitions for payment by month
CREATE TABLE payments_2026_02 PARTITION OF payments
    FOR VALUES FROM ('2026-02-01') TO ('2026-03-01');

-- 5. Table partitions for payment_transactions by month
CREATE TABLE payment_transactions_2026_02 PARTITION OF payment_transactions
    FOR VALUES FROM ('2026-02-01') TO ('2026-03-01');