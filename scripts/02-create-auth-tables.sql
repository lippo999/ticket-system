-- =====================================================================
-- Script tạo tables cho AuthService
-- Database: ticket
-- Schema: auth_schema
-- User: auth_user (riêng biệt, chỉ có quyền trên auth_schema)
-- =====================================================================

-- Bước 3: Grant quyền CHỈ trên schema này cho user auth_user
GRANT ALL PRIVILEGES ON SCHEMA auth_schema TO auth_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA auth_schema TO auth_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA auth_schema TO auth_user;

-- Set default privileges cho các object tạo sau này
ALTER DEFAULT PRIVILEGES IN SCHEMA auth_schema 
    GRANT ALL PRIVILEGES ON TABLES TO auth_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA auth_schema 
    GRANT ALL PRIVILEGES ON SEQUENCES TO auth_user;

-- Set search path
SET search_path TO auth_schema;

-- Bước 4: Tạo 3 tables cơ bản cho AuthService

-- Table 1: users (1 user = 1 role)
CREATE TABLE IF NOT EXISTS auth_schema.users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100),
    role_id BIGINT,
    enabled BOOLEAN DEFAULT TRUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table 2: roles
CREATE TABLE IF NOT EXISTS auth_schema.roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table 3: permissions
CREATE TABLE IF NOT EXISTS auth_schema.permissions (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Add foreign key constraint for users.role_id
ALTER TABLE auth_schema.users 
    ADD CONSTRAINT fk_users_role 
    FOREIGN KEY (role_id) REFERENCES auth_schema.roles(id) ON DELETE SET NULL;

-- Table 4: role_permissions (1 role = N permissions)
CREATE TABLE IF NOT EXISTS auth_schema.role_permissions (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES auth_schema.roles(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES auth_schema.permissions(id) ON DELETE CASCADE
);

-- Indexes để tối ưu query
CREATE INDEX IF NOT EXISTS idx_users_username ON auth_schema.users(username);
CREATE INDEX IF NOT EXISTS idx_users_email ON auth_schema.users(email);
CREATE INDEX IF NOT EXISTS idx_users_role_id ON auth_schema.users(role_id);
CREATE INDEX IF NOT EXISTS idx_roles_name ON auth_schema.roles(name);
CREATE INDEX IF NOT EXISTS idx_permissions_name ON auth_schema.permissions(name);

-- Indexes cho junction table
CREATE INDEX IF NOT EXISTS idx_role_permissions_role_id ON auth_schema.role_permissions(role_id);
CREATE INDEX IF NOT EXISTS idx_role_permissions_permission_id ON auth_schema.role_permissions(permission_id);

-- =====================================================================
-- Verify
-- =====================================================================
-- Kiểm tra user đã tạo
SELECT usename, usecreatedb, usesuper FROM pg_user WHERE usename = 'auth_user';

-- Kiểm tra schema và tables
SELECT 
    schemaname as schema, 
    tablename as table_name
FROM pg_tables 
WHERE schemaname = 'auth_schema'
ORDER BY tablename;