-- Create Database e_shipping_service
\c e_shipping_service;

-- Đảm bảo extension pgcrypto có sẵn để xử lý UUID nếu cần
CREATE EXTENSION IF NOT EXISTS "pgcrypto";
CREATE EXTENSION IF NOT EXISTS "unaccent";

-- 1. Table: shippings
CREATE TABLE IF NOT EXISTS shippings (
    id UUID NOT NULL,
    delivered_at TIMESTAMP,
    estimated_date TIMESTAMP,
    provider VARCHAR(30) CHECK (provider IN ('GHN', 'GHTK', 'VTN', 'NINJA', 'SPEEDLINK')),
    shipping_at TIMESTAMP,
    shipping_fee DECIMAL(19, 2) DEFAULT 0,
    status VARCHAR(30) CHECK (status IN ('PENDING', 'SHIPPING', 'DELIVERED', 'RETURNED', 'FAILED')),
    tracking_code VARCHAR(100),
    order_id UUID NOT NULL,
    order_code VARCHAR(50) NOT NULL,
    order_created_at TIMESTAMP NOT NULL,
    event_id UUID NOT NULL,
    activated BOOLEAN DEFAULT TRUE,

    -- Audit fields
    created_by VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id, created_at) -- Partition key on created_at for time-based partitioning
) PARTITION BY RANGE (created_at);

-- Index for optimization
CREATE UNIQUE INDEX idx_shipping_order_created_at ON shippings(order_created_at);
CREATE UNIQUE INDEX idx_shipping_code_unique ON shippings (tracking_code, created_at);

-- Comment
COMMENT ON COLUMN shippings.status IS 'Save status of Shipping including: PENDING, SHIPPING, DELIVERED, RETURNED, FAILED';
COMMENT ON COLUMN shippings.provider IS 'Save provider of Shipping including: GHN, GHTK, VTN, NINJA, SPEEDLINK';

-- 2. Table partitions for shippings by month
CREATE TABLE shippings_2026_02 PARTITION OF shippings
    FOR VALUES FROM ('2026-02-01') TO ('2026-03-01');