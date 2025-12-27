-- =====================================================================
-- Script insert dữ liệu ban đầu cho AuthService
-- Database: ticket
-- Schema: auth_schema
-- =====================================================================

SET search_path TO auth_schema;

-- =====================================================================
-- 1. Insert Permissions
-- =====================================================================

INSERT INTO auth_schema.permissions (name, description, created_at, updated_at) 
VALUES 
    ('USER_READ', 'Read user information', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('USER_CREATE', 'Create new users', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('USER_UPDATE', 'Update existing users', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('USER_DELETE', 'Delete users', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    
    ('ROLE_READ', 'Read role information', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('ROLE_CREATE', 'Create new roles', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('ROLE_UPDATE', 'Update existing roles', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('ROLE_DELETE', 'Delete roles', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    
    ('PERMISSION_READ', 'Read permission information', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('PERMISSION_CREATE', 'Create new permissions', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('PERMISSION_UPDATE', 'Update existing permissions', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('PERMISSION_DELETE', 'Delete permissions', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    
    ('EVENT_READ', 'Read event information', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('EVENT_CREATE', 'Create new events', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('EVENT_UPDATE', 'Update existing events', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('EVENT_DELETE', 'Delete events', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    
    ('BOOKING_READ', 'Read booking information', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('BOOKING_CREATE', 'Create new bookings', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('BOOKING_UPDATE', 'Update existing bookings', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('BOOKING_DELETE', 'Delete bookings', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    
    ('PAYMENT_READ', 'Read payment information', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('PAYMENT_CREATE', 'Process payments', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('PAYMENT_UPDATE', 'Update payment status', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('PAYMENT_DELETE', 'Delete payments', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (name) DO NOTHING;

-- =====================================================================
-- 2. Insert Roles
-- =====================================================================

INSERT INTO auth_schema.roles (name, description, created_at, updated_at)
VALUES
    ('ADMIN', 'Administrator with full access', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('USER', 'Regular user with basic access', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('EVENT_MANAGER', 'Manage events and view bookings', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('BOOKING_MANAGER', 'Manage bookings and payments', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (name) DO NOTHING;

-- =====================================================================
-- 3. Insert Users (password: admin123 đã được BCrypt encode)
-- =====================================================================

-- Password: admin123 (BCrypt với strength=10)
-- Bạn có thể generate BCrypt password tại: https://bcrypt-generator.com/
INSERT INTO auth_schema.users (username, email, password, full_name, enabled, created_at, updated_at)
VALUES
    ('admin', 'admin@ticketflow.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'System Administrator', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (username) DO NOTHING;

-- =====================================================================
-- 4. Assign Permissions to Roles
-- =====================================================================

-- ADMIN role - ALL permissions
INSERT INTO auth_schema.role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM auth_schema.roles r
CROSS JOIN auth_schema.permissions p
WHERE r.name = 'ADMIN'
ON CONFLICT DO NOTHING;

-- USER role - Read permissions + booking create/update
INSERT INTO auth_schema.role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM auth_schema.roles r, auth_schema.permissions p
WHERE r.name = 'USER'
  AND (p.name LIKE '%_READ' OR p.name IN ('BOOKING_CREATE', 'BOOKING_UPDATE'))
ON CONFLICT DO NOTHING;

-- EVENT_MANAGER role - All event permissions + booking/payment read
INSERT INTO auth_schema.role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM auth_schema.roles r, auth_schema.permissions p
WHERE r.name = 'EVENT_MANAGER'
  AND (p.name LIKE 'EVENT_%' OR p.name IN ('BOOKING_READ', 'PAYMENT_READ'))
ON CONFLICT DO NOTHING;

-- BOOKING_MANAGER role - All booking and payment permissions + event read
INSERT INTO auth_schema.role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM auth_schema.roles r, auth_schema.permissions p
WHERE r.name = 'BOOKING_MANAGER'
  AND (p.name LIKE 'BOOKING_%' OR p.name LIKE 'PAYMENT_%' OR p.name = 'EVENT_READ')
ON CONFLICT DO NOTHING;

-- =====================================================================
-- 5. Assign Roles to Users
-- =====================================================================

-- Assign ADMIN role to admin user
INSERT INTO auth_schema.user_roles (user_id, role_id)
SELECT u.id, r.id
FROM auth_schema.users u, auth_schema.roles r
WHERE u.username = 'admin' AND r.name = 'ADMIN'
ON CONFLICT DO NOTHING;

-- =====================================================================
-- Verify inserted data
-- =====================================================================

-- Count permissions
SELECT 'Permissions' as entity, COUNT(*) as count FROM auth_schema.permissions
UNION ALL
SELECT 'Roles', COUNT(*) FROM auth_schema.roles
UNION ALL
SELECT 'Users', COUNT(*) FROM auth_schema.users
UNION ALL
SELECT 'Role-Permissions', COUNT(*) FROM auth_schema.role_permissions
UNION ALL
SELECT 'User-Roles', COUNT(*) FROM auth_schema.user_roles;

-- Show admin user details
SELECT 
    u.username,
    u.email,
    u.full_name,
    u.enabled,
    r.name as role
FROM auth_schema.users u
JOIN auth_schema.user_roles ur ON u.id = ur.user_id
JOIN auth_schema.roles r ON ur.role_id = r.id
WHERE u.username = 'admin';

-- =====================================================================
-- Login Information
-- =====================================================================
-- Username: admin
-- Password: admin123
-- Email: admin@ticketflow.com
-- ⚠️  WARNING: Change default password in production!
-- =====================================================================
