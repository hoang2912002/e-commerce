-- Create Database e_inventory_service
\c e_inventory_service;

-- Đảm bảo extension pgcrypto có sẵn để xử lý UUID nếu cần
CREATE EXTENSION IF NOT EXISTS "pgcrypto";
CREATE EXTENSION IF NOT EXISTS "unaccent";

-- 1 Table: ware_houses
CREATE TABLE IF NOT EXISTS ware_houses (
    id UUID PRIMARY KEY,
    code VARCHAR(10) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    location VARCHAR(255) NOT NULL,
    status VARCHAR(20) CHECK (status IN ('PENDING', 'ACTIVE', 'INACTIVE', 'CLOSED')),
    activated BOOLEAN DEFAULT TRUE,

    -- Audit fields (Từ AbstractAuditingEntity)
    created_by VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Index for optimization
CREATE INDEX idx_warehouse_code ON ware_houses(code);
CREATE INDEX idx_warehouse_name ON ware_houses(name);

-- Comment
COMMENT ON COLUMN ware_houses.status IS 'Save status of WareHouse including: ACTIVE, INACTIVE, PENDING, CLOSED';

-- 2 Table: inventories
CREATE TABLE IF NOT EXISTS inventories (
    id UUID PRIMARY KEY,
    product_id UUID NOT NULL,
    product_sku_id UUID NOT NULL,
    quantity_available INTEGER DEFAULT 0,
    quantity_reserved INTEGER DEFAULT 0,
    quantity_sold INTEGER DEFAULT 0,
    activated BOOLEAN DEFAULT TRUE,
    warehouse_id UUID,

    -- Audit fields
    created_by VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_inventory_warehouse FOREIGN KEY (warehouse_id) REFERENCES ware_houses (id) ON DELETE CASCADE,
    -- Constraint: Unique for SKU + Warehouse
    CONSTRAINT uk_inventory_sku_warehouse UNIQUE (product_sku_id, warehouse_id)
);

CREATE INDEX idx_inventory_product_id ON inventories(product_id);
CREATE INDEX idx_inventory_warehouse_id ON inventories(warehouse_id);
CREATE INDEX idx_inventory_sku_id ON inventories(product_sku_id);
CREATE INDEX idx_inventory_product_sku_id ON inventories(product_id, product_sku_id);

-- 3. Table: inventory_transactions
CREATE TABLE IF NOT EXISTS inventory_transactions (
    id BIGSERIAL PRIMARY KEY,
    product_sku_id UUID NOT NULL,
    warehouse_id UUID NOT NULL,
    quantity_change INTEGER NOT NULL,
    before_quantity INTEGER NOT NULL,
    after_quantity INTEGER NOT NULL,
    type VARCHAR(50) CHECK (type IN ('IMPORT', 'EXPORT', 'ORDER_RESERVE', 'ORDER_RELEASE', 'ADJUSTMENT', 'RETURN')),
    reference_type VARCHAR(50) CHECK (reference_type IN ('PRODUCT', 'ORDER', 'INVENTORY')),
    reference_id UUID NOT NULL,
    event_id UUID NOT NULL,
    note TEXT,
    activated BOOLEAN DEFAULT TRUE,

    -- Audit fields
    created_by VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_transaction_warehouse FOREIGN KEY (warehouse_id) REFERENCES ware_houses (id) ON DELETE CASCADE
);
CREATE INDEX idx_inv_trx_product_sku ON inventory_transactions(product_sku_id);
CREATE INDEX idx_inv_trx_reference ON inventory_transactions(reference_id);

-- Comment
COMMENT ON COLUMN inventory_transactions.type IS 'Save type of Inventory Transaction including: IMPORT, EXPORT, ORDER_RESERVE, ORDER_RELEASE, ADJUSTMENT, RETURN';
COMMENT ON COLUMN inventory_transactions.reference_type IS 'Save reference type of Inventory Transaction including: PRODUCT, ORDER, INVENTORY';

INSERT INTO ware_houses (id, code, name, location, status, activated, created_by)
VALUES
    (gen_random_uuid(), 'WH-HCM-001', 'Kho Thủ Đức HCM', 'Thủ Đức, Thành phố Hồ Chí Minh', 'ACTIVE', TRUE, 'system'),
    (gen_random_uuid(), 'WH-HN-001', 'Kho Hà Nội', 'Hà Nội', 'ACTIVE', TRUE, 'system');