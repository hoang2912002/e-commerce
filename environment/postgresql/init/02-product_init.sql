-- CREATE DATABASE e_product_service;

-- Connect to right database
\c e_product_service;

-- 1. Table: categories
CREATE TABLE IF NOT EXISTS categories (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    slug VARCHAR(100) NOT NULL UNIQUE,
    activated BOOLEAN DEFAULT TRUE,
    parent_id UUID,
    
    -- Audit fields
    created_by VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Foreign Key (Self-reference)
    CONSTRAINT fk_category_parent FOREIGN KEY (parent_id) 
        REFERENCES categories (id) ON DELETE CASCADE
);

CREATE INDEX idx_category_parent_id ON categories(parent_id);

-- 2. Table: shop_managements
CREATE TABLE IF NOT EXISTS shop_managements (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL UNIQUE,
    business_name VARCHAR(255),
    business_no VARCHAR(255),
    business_date_issue DATE,
    business_place VARCHAR(255),
    tax_code VARCHAR(255),
    business_type INTEGER,
    account_name VARCHAR(255),
    account_number VARCHAR(255),
    bank_name VARCHAR(255),
    bank_branch VARCHAR(255),
    description TEXT,
    address_id UUID,
    user_id UUID,
    activated BOOLEAN DEFAULT TRUE,
    
    created_by VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. Table: products
CREATE TABLE IF NOT EXISTS products (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL UNIQUE,
    price DECIMAL(19, 2) NOT NULL,
    description TEXT,
    activated BOOLEAN DEFAULT TRUE,
    category_id UUID,
    shop_management_id UUID,
    
    created_by VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_product_category FOREIGN KEY (category_id) 
        REFERENCES categories (id) ON DELETE SET NULL,
    CONSTRAINT fk_product_shop FOREIGN KEY (shop_management_id) 
        REFERENCES shop_managements (id) ON DELETE CASCADE
);

-- Index optimization
CREATE INDEX idx_product_name_slug ON products(name, slug);
CREATE INDEX idx_product_category_id ON products(category_id);
CREATE INDEX idx_product_shop_management_id ON products(shop_management_id);

-- 4. Table: product_skus
CREATE TABLE IF NOT EXISTS product_skus (
    id UUID PRIMARY KEY,
    sku VARCHAR(255) NOT NULL UNIQUE,
    price DECIMAL(19, 2),
    temp_stock INTEGER DEFAULT 0,
    activated BOOLEAN DEFAULT TRUE,
    product_id UUID NOT NULL,
    
    created_by VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_sku_product FOREIGN KEY (product_id) 
        REFERENCES products (id) ON DELETE CASCADE
);

-- Index optimization
CREATE INDEX idx_sku_product_id ON product_skus(product_id);
CREATE INDEX idx_sku_sku ON product_skus(sku);

-- 5. Table: options
CREATE TABLE IF NOT EXISTS options (
    id BIGSERIAL PRIMARY KEY, -- BIGSERIAL tương đương BIGINT AUTO_INCREMENT
    name VARCHAR(100) NOT NULL,
    slug VARCHAR(100) NOT NULL UNIQUE,
    activated BOOLEAN DEFAULT TRUE,
    created_by VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 6. Table: option_values
CREATE TABLE IF NOT EXISTS option_values (
    id BIGSERIAL PRIMARY KEY,
    value VARCHAR(100) NOT NULL,
    slug VARCHAR(100) NOT NULL UNIQUE,
    activated BOOLEAN DEFAULT TRUE,
    option_id BIGINT NOT NULL,
    created_by VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_option_value_option FOREIGN KEY (option_id) 
        REFERENCES options (id) ON DELETE CASCADE
);

-- 7. Table: promotions
CREATE TABLE IF NOT EXISTS promotions (
    id UUID PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    discount_percent INTEGER,
    min_discount_amount DECIMAL(19, 2),
    max_discount_amount DECIMAL(19, 2),
    quantity INTEGER,
    discount_type VARCHAR(20) CHECK (discount_type IN ('PRODUCT', 'CATEGORY', 'FREESHIP', 'FLASHSALE')),
    start_date DATE,
    end_date DATE,
    activated BOOLEAN DEFAULT TRUE,
    option_promotion SMALLINT NOT NULL,
    event_id UUID NOT NULL,
    
    created_by VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Index optimization
CREATE INDEX idx_promotion_code ON promotions(code);
CREATE INDEX idx_promotion_name ON promotions(name);
CREATE INDEX idx_promotion_start_end_date ON promotions(start_date, end_date);

-- 8. Table: promotion_products
CREATE TABLE IF NOT EXISTS promotion_products (
    id BIGSERIAL PRIMARY KEY,
    activated BOOLEAN DEFAULT TRUE,
    product_id UUID,
    promotion_id UUID,
    category_id UUID,
    created_by VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_pp_promotion FOREIGN KEY (promotion_id) REFERENCES promotions (id) ON DELETE CASCADE,
    CONSTRAINT fk_pp_category FOREIGN KEY (category_id) REFERENCES categories (id) ON DELETE SET NULL,
    CONSTRAINT fk_pp_product FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE
);

-- 9. Table: variants
CREATE TABLE IF NOT EXISTS variants (
    id BIGSERIAL PRIMARY KEY,
    activated BOOLEAN DEFAULT TRUE,
    product_id UUID NOT NULL,
    product_sku_id UUID NOT NULL,
    option_id BIGINT NOT NULL,
    option_value_id BIGINT NOT NULL,
    created_by VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_variant_product FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE,
    CONSTRAINT fk_variant_sku FOREIGN KEY (product_sku_id) REFERENCES product_skus (id) ON DELETE CASCADE,
    CONSTRAINT fk_variant_option FOREIGN KEY (option_id) REFERENCES options (id) ON DELETE CASCADE,
    CONSTRAINT fk_variant_value FOREIGN KEY (option_value_id) REFERENCES option_values (id) ON DELETE CASCADE,
    UNIQUE (product_id, product_sku_id, option_id, option_value_id)
);

-- 10. Table: approval_masters
CREATE TABLE IF NOT EXISTS approval_masters (
    id UUID PRIMARY KEY,
    entity_type VARCHAR(100) NOT NULL,
    step INTEGER NOT NULL,
    status VARCHAR(20) CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'ADJUSTMENT', 'NEEDS_ADJUSTMENT', 'FINISHED_ADJUSTMENT')),
    role_id BIGINT NOT NULL,
    user_id UUID,
    required BOOLEAN DEFAULT FALSE,
    activated BOOLEAN DEFAULT TRUE,
    created_by VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 11. Table: approval_histories
CREATE TABLE IF NOT EXISTS approval_histories (
    id BIGSERIAL PRIMARY KEY,
    request_id UUID NOT NULL,
    note TEXT,
    approved_at TIMESTAMP,
    activated BOOLEAN DEFAULT TRUE,
    approval_master_id UUID NOT NULL,
    created_by VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_history_master FOREIGN KEY (approval_master_id) 
        REFERENCES approval_masters (id) ON DELETE CASCADE
);

-- Index bổ sung
CREATE INDEX idx_history_request_id ON approval_histories(request_id);

-- Extension pgcrypto để dùng gen_random_uuid()
CREATE EXTENSION IF NOT EXISTS "pgcrypto";
CREATE EXTENSION IF NOT EXISTS "unaccent";

-- 1. Seed Categories (Cây danh mục)
INSERT INTO categories (id, name, slug, activated, created_by) VALUES 
(gen_random_uuid(), 'Đồng phục học sinh', 'dong-phuc-hoc-sinh', TRUE, 'system'),
(gen_random_uuid(), 'Đồng phục văn phòng', 'dong-phuc-van-phong', TRUE, 'system'),
(gen_random_uuid(), 'Đồng phục thể thao', 'dong-phuc-the-thao', TRUE, 'system');

-- 2. Seed Options (Thuộc tính)
INSERT INTO options (name, slug, activated, created_by) VALUES 
('Màu sắc', 'mau-sac', TRUE, 'system'),
('Kích cỡ', 'kich-co', TRUE, 'system');

-- 3. Seed Option Values (Giá trị thuộc tính)
INSERT INTO option_values (value, slug, option_id, activated, created_by) 
SELECT 'Đỏ', 'do', id, TRUE, 'system' FROM options WHERE slug = 'mau-sac';
INSERT INTO option_values (value, slug, option_id, activated, created_by) 
SELECT 'Xanh', 'xanh', id, TRUE, 'system' FROM options WHERE slug = 'mau-sac';
INSERT INTO option_values (value, slug, option_id, activated, created_by) 
SELECT 'Đen', 'den', id, TRUE, 'system' FROM options WHERE slug = 'mau-sac';
INSERT INTO option_values (value, slug, option_id, activated, created_by) 
SELECT 'S', 'size-s', id, TRUE, 'system' FROM options WHERE slug = 'kich-co';
INSERT INTO option_values (value, slug, option_id, activated, created_by) 
SELECT 'M', 'size-m', id, TRUE, 'system' FROM options WHERE slug = 'kich-co';
INSERT INTO option_values (value, slug, option_id, activated, created_by) 
SELECT 'L', 'size-l', id, TRUE, 'system' FROM options WHERE slug = 'kich-co';
INSERT INTO option_values (value, slug, option_id, activated, created_by) 
SELECT 'XL', 'size-xl', id, TRUE, 'system' FROM options WHERE slug = 'kich-co';

-- 4. Seed Shop Management (Cửa hàng mẫu)
INSERT INTO shop_managements (id, name, slug, business_name, activated, created_by) VALUES
(gen_random_uuid(), 'Xưởng May Đồng Phục ABC', 'xuong-may-abc', 'Công ty TNHH ABC', TRUE, 'system');

-- 5. Seed Approval Masters (Quy trình duyệt)
INSERT INTO approval_masters (id, entity_type, step, status, role_id, required, activated, created_by) VALUES
(gen_random_uuid(), 'PRODUCT', 1, 'PENDING', 1, TRUE, TRUE, 'system'),
(gen_random_uuid(), 'PRODUCT', 2, 'APPROVED', 1, TRUE, TRUE, 'system'),
(gen_random_uuid(), 'PRODUCT', 3, 'REJECTED', 1, TRUE, TRUE, 'system'),
(gen_random_uuid(), 'PRODUCT', 4, 'ADJUSTMENT', 1, TRUE, TRUE, 'system'),
(gen_random_uuid(), 'PRODUCT', 5, 'NEEDS_ADJUSTMENT', 1, TRUE, TRUE, 'system'),
(gen_random_uuid(), 'PRODUCT', 6, 'FINISHED_ADJUSTMENT', 1, TRUE, TRUE, 'system'),
(gen_random_uuid(), 'SHOP_MANAGEMENT', 1, 'PENDING', 1, TRUE, TRUE, 'system'),
(gen_random_uuid(), 'SHOP_MANAGEMENT', 2, 'APPROVED', 1, TRUE, TRUE, 'system'),
(gen_random_uuid(), 'SHOP_MANAGEMENT', 3, 'REJECTED', 1, TRUE, TRUE, 'system'),
(gen_random_uuid(), 'SHOP_MANAGEMENT', 4, 'ADJUSTMENT', 1, TRUE, TRUE, 'system'),
(gen_random_uuid(), 'SHOP_MANAGEMENT', 5, 'NEEDS_ADJUSTMENT', 1, TRUE, TRUE, 'system'),
(gen_random_uuid(), 'SHOP_MANAGEMENT', 6, 'FINISHED_ADJUSTMENT', 1, TRUE, TRUE, 'system');

-- 6. Seed Promotions (Khuyến mãi)
INSERT INTO promotions (id, code, name, discount_percent, discount_type, quantity, option_promotion, event_id, activated, created_by) VALUES
(gen_random_uuid(), 'SUMMER2026', 'Khuyến mãi hè 2026', 10, 'PRODUCT', 100, 1, gen_random_uuid(), TRUE, 'system');