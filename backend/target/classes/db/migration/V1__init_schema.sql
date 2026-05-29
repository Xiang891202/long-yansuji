-- 租戶表 id 改為 INTEGER
CREATE TABLE IF NOT EXISTS tenants (
    id INTEGER PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    slug VARCHAR(50),
    schema_name VARCHAR(50),
    created_at TIMESTAMPTZ DEFAULT now()
);

-- 員工表 tenant_id 改為 INTEGER
CREATE TABLE IF NOT EXISTS employees (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id INTEGER NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    identity_number VARCHAR(10) NOT NULL,
    birth_date DATE NOT NULL,
    name VARCHAR(50) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    permissions JSONB NOT NULL DEFAULT '[]',
    created_at TIMESTAMPTZ DEFAULT now(),
    UNIQUE (tenant_id, identity_number)
);

-- 管理員表 tenant_id 改為 INTEGER
CREATE TABLE IF NOT EXISTS admins (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id INTEGER NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    name VARCHAR(50),
    created_at TIMESTAMPTZ DEFAULT now(),
    UNIQUE (tenant_id, email)
);

-- 商品分類表
CREATE TABLE IF NOT EXISTS inv_categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id INTEGER NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    name VARCHAR(50) NOT NULL,
    code VARCHAR(30) NOT NULL,
    sort_order INTEGER DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT now(),
    UNIQUE (tenant_id, code)
);

-- 商品表
CREATE TABLE IF NOT EXISTS inv_products (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id INTEGER NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    category_id UUID REFERENCES inv_categories(id) ON DELETE SET NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    image_url VARCHAR(500),
    prices JSONB NOT NULL DEFAULT '[]',
    unit VARCHAR(20) DEFAULT '包',
    is_active BOOLEAN DEFAULT true,
    sort_order INTEGER DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

-- 安全庫存模板表
CREATE TABLE IF NOT EXISTS inv_safe_stocks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id INTEGER NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES inv_products(id) ON DELETE CASCADE,
    day_of_week SMALLINT CHECK (day_of_week BETWEEN 1 AND 7),
    safe_quantity INTEGER CHECK (safe_quantity >= 0),
    version INTEGER DEFAULT 1,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now(),
    UNIQUE (product_id, day_of_week)
);

-- 庫存回報記錄表
CREATE TABLE IF NOT EXISTS inv_inventory_reports (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id INTEGER NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES inv_products(id) ON DELETE CASCADE,
    employee_id UUID REFERENCES employees(id),
    day_of_week SMALLINT NOT NULL,
    current_quantity INTEGER CHECK (current_quantity >= 0),
    reported_by VARCHAR(50),
    created_at TIMESTAMPTZ DEFAULT now()
);

-- 叫貨單主表
CREATE TABLE IF NOT EXISTS inv_replenishments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id INTEGER NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    employee_id UUID REFERENCES employees(id),
    safe_stock_version INTEGER NOT NULL,
    status VARCHAR(20) DEFAULT 'draft',
    created_by VARCHAR(50),
    created_at TIMESTAMPTZ DEFAULT now()
);

-- 叫貨明細表
CREATE TABLE IF NOT EXISTS inv_replenishment_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id INTEGER NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    replenishment_id UUID NOT NULL REFERENCES inv_replenishments(id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES inv_products(id) ON DELETE CASCADE,
    quantity INTEGER NOT NULL,
    unit VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ DEFAULT now()
);

-- 首頁設定
CREATE TABLE IF NOT EXISTS homepage_settings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id INTEGER NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    store_name VARCHAR(100),
    slogan TEXT,
    address TEXT,
    opening_hours VARCHAR(100),
    logo_url VARCHAR(500),
    updated_at TIMESTAMPTZ DEFAULT now()
);

-- API 日誌
CREATE TABLE IF NOT EXISTS api_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id INTEGER NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    method VARCHAR(10),
    path VARCHAR(255),
    request_body TEXT,
    response_status INTEGER,
    response_time_ms INTEGER,
    created_at TIMESTAMPTZ DEFAULT now()
);

-- 冪等性記錄
CREATE TABLE IF NOT EXISTS idempotency_records (
    key VARCHAR(255) PRIMARY KEY,
    response_data TEXT,
    created_at TIMESTAMPTZ DEFAULT now()
);

-- 索引
CREATE INDEX IF NOT EXISTS idx_safe_stocks_tenant_day ON inv_safe_stocks(tenant_id, day_of_week);
CREATE INDEX IF NOT EXISTS idx_products_tenant_category ON inv_products(tenant_id, category_id);
CREATE INDEX IF NOT EXISTS idx_replenishments_tenant_date ON inv_replenishments(tenant_id, created_at);
CREATE INDEX IF NOT EXISTS idx_replenishment_items_replenishment ON inv_replenishment_items(replenishment_id);
CREATE INDEX IF NOT EXISTS idx_api_logs_tenant_time ON api_logs(tenant_id, created_at DESC);

-- 插入租戶（整數 ID）
INSERT INTO tenants (id, name, slug, schema_name) VALUES 
(1, '美容專案', 'beauty', 'beauty')
ON CONFLICT (id) DO NOTHING;

INSERT INTO tenants (id, name, slug, schema_name) VALUES 
(2, '龍鹽酥雞（新市店）', 'ysgs', 'ysgs')
ON CONFLICT (id) DO NOTHING;

-- 插入測試員工（tenant_id 使用整數）
INSERT INTO employees (tenant_id, identity_number, birth_date, name, permissions)
VALUES (1, 'A123456789', '1990-01-01', '美容員工', '["inventory_access"]')
ON CONFLICT (tenant_id, identity_number) DO NOTHING;

INSERT INTO employees (tenant_id, identity_number, birth_date, name, permissions)
VALUES (2, 'B987654321', '1990-01-02', '鹽酥雞員工', '["inventory_access"]')
ON CONFLICT (tenant_id, identity_number) DO NOTHING;

-- 插入管理員
INSERT INTO admins (tenant_id, email, phone, name)
VALUES (1, 'admin@beauty.com', '0912345678', '美容管理員')
ON CONFLICT (tenant_id, email) DO NOTHING;

INSERT INTO admins (tenant_id, email, phone, name)
VALUES (2, 'boss@ysgs.com', '0987654321', '鹽酥雞老闆')
ON CONFLICT (tenant_id, email) DO NOTHING;

-- 插入分類與商品（鹽酥雞）
INSERT INTO inv_categories (tenant_id, name, code, sort_order) VALUES
(2, '招牌類', 'signature', 1),
(2, '串類', 'skewer', 2),
(2, '必點類', 'must_order', 3),
(2, '青菜類', 'vegetable', 4),
(2, '甜心類', 'dessert', 5)
ON CONFLICT (tenant_id, code) DO NOTHING;

INSERT INTO inv_products (tenant_id, category_id, name, unit, prices, is_active) 
SELECT 2, id, '雞排', '包', '[{"label":"小份","price":60},{"label":"大份","price":90}]', true
FROM inv_categories WHERE tenant_id = 2 AND code = 'signature' LIMIT 1
ON CONFLICT DO NOTHING;

INSERT INTO inv_products (tenant_id, category_id, name, unit, prices, is_active) 
SELECT 2, id, '高麗菜', '份', '[{"label":"一份","price":40}]', true
FROM inv_categories WHERE tenant_id = 2 AND code = 'vegetable' LIMIT 1
ON CONFLICT DO NOTHING;

- 若缺少樂觀鎖欄位 version，請執行：
ALTER TABLE inv_safe_stocks ADD COLUMN IF NOT EXISTS version INT DEFAULT 1;
