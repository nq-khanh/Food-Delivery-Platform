-- =========================================================
-- EXTENSIONS
-- =========================================================
CREATE EXTENSION IF NOT EXISTS vector;

-- =========================================================
-- 1. USERS & AUTHORIZATION
-- =========================================================
CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255),
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    phone VARCHAR(20) UNIQUE NOT NULL,
    avatar_url TEXT,
    is_verified BOOLEAN DEFAULT false,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Bảng địa chỉ riêng biệt
CREATE TABLE user_addresses (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    address_name VARCHAR(50), -- Ví dụ: "Nhà riêng", "Công ty"
    full_address TEXT NOT NULL,
    latitude NUMERIC(10,8) NOT NULL,
    longitude NUMERIC(11,8) NOT NULL,
    is_default BOOLEAN DEFAULT false,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Ràng buộc: Mỗi user chỉ có duy nhất 1 địa chỉ mặc định
CREATE UNIQUE INDEX idx_only_one_default_address 
ON user_addresses (user_id) 
WHERE (is_default IS TRUE);

CREATE TABLE roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(30) UNIQUE NOT NULL
);

CREATE TABLE user_roles (
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    role_id INT REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token TEXT UNIQUE NOT NULL,
    expiry_date TIMESTAMPTZ NOT NULL,
    is_revoked BOOLEAN DEFAULT false,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Email verification / password reset
CREATE TABLE user_verifications (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash TEXT NOT NULL,
    type VARCHAR(20) NOT NULL, -- EMAIL_VERIFY / PASSWORD_RESET
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- =========================================================
-- 2. RESTAURANTS & PRODUCTS
-- =========================================================
CREATE TABLE restaurants (
    id UUID PRIMARY KEY,
    owner_id UUID NOT NULL REFERENCES users(id),
    name VARCHAR(255) NOT NULL,
    address TEXT NOT NULL,
    latitude NUMERIC(10,8) NOT NULL,
    longitude NUMERIC(11,8) NOT NULL,
    description TEXT,
    approval_status VARCHAR(20) DEFAULT 'PENDING',
    rating_avg NUMERIC(2,1) DEFAULT 0 CHECK (rating_avg BETWEEN 0 AND 5),
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_restaurants_location 
ON restaurants (latitude, longitude);

CREATE TABLE restaurant_operating_hours (
    id SERIAL PRIMARY KEY,
    restaurant_id UUID REFERENCES restaurants(id) ON DELETE CASCADE,
    day_of_week INT CHECK (day_of_week BETWEEN 0 AND 6),
    open_time TIME NOT NULL,
    close_time TIME NOT NULL,
    is_closed BOOLEAN DEFAULT false
);

CREATE TABLE categories (
    id SERIAL PRIMARY KEY,
    restaurant_id UUID REFERENCES restaurants(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    display_order INT DEFAULT 0
);

CREATE TABLE products (
    id UUID PRIMARY KEY,
    restaurant_id UUID REFERENCES restaurants(id) ON DELETE CASCADE,
    category_id INT REFERENCES categories(id) ON DELETE SET NULL,
    name VARCHAR(255) NOT NULL,
    price NUMERIC(12,2) NOT NULL CHECK (price >= 0),
    description TEXT,
    is_available BOOLEAN DEFAULT true,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_products_restaurant_category 
ON products (restaurant_id, category_id);

-- =========================================================
-- 3. SHIPPING (SHIPPER & CONFIG)
-- =========================================================
CREATE TABLE shippers (
    id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    vehicle_info VARCHAR(100),
    license_plate VARCHAR(20) UNIQUE,
    is_online BOOLEAN DEFAULT false,
    is_busy BOOLEAN DEFAULT false,
    current_lat NUMERIC(10,8),
    current_lng NUMERIC(11,8)
);

CREATE TABLE shipping_configs (
    id SERIAL PRIMARY KEY,
    base_fee NUMERIC(12,2) NOT NULL,
    base_distance_km NUMERIC(5,2) NOT NULL,
    fee_per_km NUMERIC(12,2) NOT NULL,
    active_from TIMESTAMPTZ DEFAULT NOW()
);

-- =========================================================
-- 4. PROMOTION & SYSTEM CONFIG
-- =========================================================
CREATE TABLE vouchers (
    id UUID PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    discount_type VARCHAR(20) NOT NULL,
    discount_value NUMERIC(12,2) NOT NULL,
    min_order_value NUMERIC(12,2) DEFAULT 0,
    max_discount_amount NUMERIC(12,2),
    usage_limit INT DEFAULT 1,
    used_count INT DEFAULT 0,
    expiry_date TIMESTAMPTZ NOT NULL,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE system_configs (
    id SERIAL PRIMARY KEY,
    key VARCHAR(50) UNIQUE NOT NULL,
    value NUMERIC(10,2) NOT NULL,
    description TEXT,
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- =========================================================
-- 5. ORDERS
-- =========================================================
CREATE TABLE orders (
    id UUID PRIMARY KEY,
    order_code VARCHAR(20) UNIQUE NOT NULL,
    customer_id UUID REFERENCES users(id),
    restaurant_id UUID REFERENCES restaurants(id),
    shipper_id UUID REFERENCES shippers(id),
    voucher_id UUID REFERENCES vouchers(id),

    -- Phần tài chính (Nên có CHECK để đảm bảo dữ liệu sạch)
    subtotal NUMERIC(12,2) NOT NULL CHECK (subtotal >= 0),
    shipping_distance_km NUMERIC(6,2) NOT NULL CHECK (shipping_distance_km >= 0),
    shipping_fee NUMERIC(12,2) NOT NULL CHECK (shipping_fee >= 0),
    discount_amount NUMERIC(12,2) DEFAULT 0 CHECK (discount_amount >= 0),
    total_amount NUMERIC(12,2) NOT NULL CHECK (total_amount >= 0),

    -- Phần hoa hồng sàn
    commission_rate NUMERIC(5,2) CHECK (commission_rate >= 0),
    commission_amount NUMERIC(12,2) CHECK (commission_amount >= 0),

    -- Trạng thái
    order_status VARCHAR(30) NOT NULL, -- PENDING, CONFIRMED, SHIPPING, COMPLETED, CANCELLED
    payment_status VARCHAR(30) NOT NULL, -- UNPAID, PAID, REFUNDED

    -- Thông tin giao hàng
    delivery_address TEXT NOT NULL,
    delivery_lat NUMERIC(10,8),
    delivery_lng NUMERIC(11,8),

    -- Thời gian
    placed_at TIMESTAMPTZ DEFAULT NOW(),
    completed_at TIMESTAMPTZ,
    cancelled_at TIMESTAMPTZ
);

CREATE INDEX idx_orders_customer_id 
ON orders (customer_id);

CREATE TABLE order_items (
    id SERIAL PRIMARY KEY,
    order_id UUID REFERENCES orders(id) ON DELETE CASCADE,
    product_id UUID REFERENCES products(id),
    quantity INT CHECK (quantity > 0),
    price_at_purchase NUMERIC(12,2) NOT NULL
);

CREATE TABLE order_status_history (
    id SERIAL PRIMARY KEY,
    order_id UUID REFERENCES orders(id) ON DELETE CASCADE,
    status VARCHAR(30),
    changed_by UUID REFERENCES users(id),
    changed_at TIMESTAMPTZ DEFAULT NOW()
);

-- =========================================================
-- 6. PAYMENTS & WALLET
-- =========================================================
CREATE TABLE payments (
    id UUID PRIMARY KEY,
    order_id UUID REFERENCES orders(id),
    vnp_txn_ref VARCHAR(100) UNIQUE,
    vnp_transaction_no VARCHAR(100),
    amount NUMERIC(12,2),
    payment_status VARCHAR(20),
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE wallets (
    id UUID PRIMARY KEY,
    user_id UUID UNIQUE REFERENCES users(id),
    balance NUMERIC(14,2) DEFAULT 0 CHECK (balance >= 0),
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE wallet_transactions (
    id UUID PRIMARY KEY,
    wallet_id UUID REFERENCES wallets(id),
    order_id UUID REFERENCES orders(id),
    transaction_code VARCHAR(50) UNIQUE,
    type VARCHAR(20),
    amount NUMERIC(14,2),
    description TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE payout_methods (
    id UUID PRIMARY KEY,
    user_id UUID REFERENCES users(id),
    bank_name VARCHAR(100),
    bank_account_number VARCHAR(50),
    bank_account_holder VARCHAR(100),
    bank_branch VARCHAR(100),
    is_default BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE payout_requests (
    id UUID PRIMARY KEY,
    wallet_id UUID REFERENCES wallets(id),
    payout_method_id UUID REFERENCES payout_methods(id),
    amount NUMERIC(14,2) CHECK (amount > 0),
    status VARCHAR(20) DEFAULT 'PENDING',
    approved_by UUID REFERENCES users(id),
    approved_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- =========================================================
-- 7. REVIEWS & MEDIA
-- =========================================================
CREATE TABLE order_reviews (
    id UUID PRIMARY KEY,
    order_id UUID UNIQUE REFERENCES orders(id),
    customer_id UUID REFERENCES users(id),
    restaurant_id UUID REFERENCES restaurants(id),
    rating INT CHECK (rating BETWEEN 1 AND 5),
    comment TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE order_item_reviews (
    id UUID PRIMARY KEY,
    order_item_id INT UNIQUE REFERENCES order_items(id),
    product_id UUID REFERENCES products(id),
    rating INT CHECK (rating BETWEEN 1 AND 5),
    comment TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE media_files (
    id UUID PRIMARY KEY,
    entity_type VARCHAR(30),
    entity_id UUID,
    file_url TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- =========================================================
-- 8. AI EMBEDDINGS (pgvector)
-- =========================================================
CREATE TABLE restaurant_embeddings (
    restaurant_id UUID PRIMARY KEY REFERENCES restaurants(id) ON DELETE CASCADE,
    embedding vector(1536)
);

CREATE TABLE product_embeddings (
    product_id UUID PRIMARY KEY REFERENCES products(id) ON DELETE CASCADE,
    embedding vector(1536)
);

CREATE INDEX idx_restaurant_embedding 
ON restaurant_embeddings 
USING hnsw (embedding vector_cosine_ops);
