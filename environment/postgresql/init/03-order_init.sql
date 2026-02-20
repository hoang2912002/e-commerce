\c e_order_service;

-- Đảm bảo extension pgcrypto có sẵn để xử lý UUID nếu cần
CREATE EXTENSION IF NOT EXISTS "pgcrypto";
CREATE EXTENSION IF NOT EXISTS "unaccent";
CREATE SEQUENCE IF NOT EXISTS order_detail_seq START 1;

-- 1. Table: coupons
CREATE TABLE IF NOT EXISTS coupons (
    id UUID PRIMARY KEY,
    version BIGINT DEFAULT 0,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    type VARCHAR(20) CHECK (type IN ('PERCENT', 'SPECIFIC_AMOUNT')),
    stock INTEGER DEFAULT 0,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    coupon_amount DECIMAL(19, 2) NOT NULL,
    activated BOOLEAN DEFAULT TRUE,

    -- Audit fields
    created_by VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_coupon_code ON coupons(code);
CREATE INDEX idx_coupon_name ON coupons(name);
CREATE INDEX idx_coupon_dates ON coupons(start_date, end_date);

-- Comment
COMMENT ON COLUMN coupons.type IS 'Save type of Coupon including: PERCENT, SPECIFIC_AMOUNT';

-- 2. Table: orders
CREATE TABLE IF NOT EXISTS orders (
    id UUID NOT NULL,
    version BIGINT DEFAULT 0,
    code VARCHAR(50) NOT NULL,

    -- Status fields
    status VARCHAR(50) CHECK (status IN ('PENDING', 'CONFIRMED', 'SHIPPING', 'DELIVERED', 'CANCELLED', 'RETURNED')),
    payment_status VARCHAR(50) CHECK (payment_status IN ('PENDING', 'SUCCESS', 'FAILED')),
    shipping_status VARCHAR(50) CHECK (shipping_status IN ('PENDING', 'SHIPPING', 'DELIVERED', 'RETURNED', 'FAILED')),
    payment_method VARCHAR(100),

    -- Financial fields
    total_item INTEGER NOT NULL DEFAULT 0,
    total_price DECIMAL(19, 2) NOT NULL,
    discount_price DECIMAL(19, 2) DEFAULT 0,
    final_price DECIMAL(19, 2) NOT NULL,
    shipping_fee DECIMAL(19, 2) DEFAULT 0,

    -- Customer and relation fields
    user_id UUID,
    shipping_id UUID,
    address_id UUID,
    payment_id UUID,
    coupon_id UUID,
    
    -- Receiver information
    receiver_name VARCHAR(255),
    receiver_email VARCHAR(255),
    receiver_phone VARCHAR(11),
    receiver_address VARCHAR(255),
    receiver_province VARCHAR(100),
    receiver_district VARCHAR(100),
    receiver_ward VARCHAR(100),
    
    note TEXT,
    activated BOOLEAN DEFAULT TRUE,

    -- Audit fields
    created_by VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id, created_at), -- Partition key on created_at for time-based partitioning
    CONSTRAINT fk_order_coupon FOREIGN KEY (coupon_id) REFERENCES coupons (id) ON DELETE SET NULL
) PARTITION BY RANGE (created_at);

CREATE INDEX idx_order_code ON orders(code);
CREATE INDEX idx_order_user_id ON orders(user_id);
CREATE INDEX idx_order_status ON orders(status);
CREATE INDEX idx_order_created_at ON orders(created_at);
CREATE UNIQUE INDEX idx_order_code_unique ON orders (code, created_at);


-- Comment
COMMENT ON COLUMN orders.status IS 'Save status of Order including: PENDING, CONFIRMED, SHIPPING, DELIVERED, CANCELLED, RETURNED';
COMMENT ON COLUMN orders.payment_status IS 'Save payment status of Order including: PENDING, SUCCESS, FAILED';
COMMENT ON COLUMN orders.shipping_status IS 'Save shipping status of Order including: WAITING, SHIPPING, DELIVERED, FAILED';

-- 3. Table: order_details
CREATE TABLE IF NOT EXISTS order_details (
    id BIGSERIAL NOT NULL,
    
    -- Financial fields
    price_original DECIMAL(19, 2) NOT NULL,    -- Original price (Undiscounted)
    price DECIMAL(19, 2) NOT NULL,             -- Real price (after discount)
    promotion_discount DECIMAL(19, 2) NOT NULL, -- Total discount from promotions
    total_price DECIMAL(19, 2) NOT NULL,       -- Total price (price * quantity)
    
    quantity INTEGER NOT NULL,
    activated BOOLEAN NOT NULL DEFAULT TRUE,
    product_id UUID NOT NULL,
    product_sku_id UUID NOT NULL,
    order_id UUID,
    order_created_at TIMESTAMP NOT NULL,

    -- Audit fields
    created_by VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id, created_at), -- Partition key on created_at for time-based partitioning
    CONSTRAINT fk_detail_order FOREIGN KEY (order_id,order_created_at) REFERENCES orders (id,created_at) ON DELETE CASCADE
) PARTITION BY RANGE (created_at);

CREATE INDEX idx_order_detail_order_id ON order_details(order_id);
CREATE INDEX idx_order_detail_product_sku_id ON order_details(product_sku_id);
CREATE INDEX idx_order_detail_product_id ON order_details(product_id);
CREATE INDEX idx_order_detail_created_at ON order_details(created_at);

ALTER TABLE order_details ALTER COLUMN id TYPE BIGINT;
ALTER TABLE order_details ALTER COLUMN id SET DEFAULT nextval('order_detail_seq');

-- 4. Table: saga_state
CREATE TABLE IF NOT EXISTS saga_states (
    id UUID NOT NULL,
    order_id UUID NOT NULL,
    order_created_at TIMESTAMP NOT NULL,
    order_code VARCHAR(50),
    status VARCHAR(50) CHECK (status IN ('START', 'COMPLETED', 'FAILED')),
    step VARCHAR(50) CHECK (step IN ('PAYMENT', 'SHIPPING', 'PROMOTION', 'INVENTORY')),
    payload TEXT,

    -- Audit fields
    created_by VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id, created_at),
    CONSTRAINT fk_saga_state FOREIGN KEY (order_id, order_created_at) REFERENCES orders (id, created_at) ON DELETE CASCADE
) PARTITION BY RANGE (created_at);

-- Index for optimization
CREATE INDEX idx_saga_states_order_id ON saga_states(order_id);
CREATE INDEX idx_saga_states_order_code ON saga_states(order_code);
CREATE INDEX idx_saga_states_created_at ON saga_states(created_at);

-- 5. Table partitions for orders by month
CREATE TABLE orders_2026_02 PARTITION OF orders
    FOR VALUES FROM ('2026-02-01') TO ('2026-03-01');

-- 6. Table partitions for order_details by month
CREATE TABLE order_details_2026_02 PARTITION OF order_details
    FOR VALUES FROM ('2026-02-01') TO ('2026-03-01');

-- 6. Table partitions for order_details by month
CREATE TABLE saga_states_2026_02 PARTITION OF saga_states
    FOR VALUES FROM ('2026-02-01') TO ('2026-03-01');