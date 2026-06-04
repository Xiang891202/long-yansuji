-- 为 employees 表添加 password_hash 列（可选，若员工使用密码登录）
ALTER TABLE employees ADD COLUMN IF NOT EXISTS password_hash VARCHAR(255);

-- 为 admins 表添加 password_hash 列
ALTER TABLE admins ADD COLUMN IF NOT EXISTS password_hash VARCHAR(255);

-- 更新现有测试数据：生成初始密码哈希（例如将 identityNumber 或 email 作为密码？实际中需为用户提供重置密码流程）
-- 这里仅为示例，生产环境不要这样操作
