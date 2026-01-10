-- =====================================================================
-- Script insert dữ liệu ban đầu cho AuthService
-- Database: ticket
-- Schema: auth_schema
-- =====================================================================

SET search_path TO auth_schema;

-- =====================================================================
-- 1. Insert Permissions (5 permissions cơ bản)
-- =====================================================================

INSERT INTO auth_schema.permissions (name, description, created_at, updated_at) 
VALUES 
    ('USER_MANAGE', 'Manage users (create, read, update, delete)', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('EVENT_MANAGE', 'Manage events (create, read, update, delete)', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('BOOKING_MANAGE', 'Manage bookings (create, read, update, delete)', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('PAYMENT_MANAGE', 'Manage payments (process, refund, view)', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('SYSTEM_CONFIG', 'Configure system settings and roles', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (name) DO NOTHING;

-- =====================================================================
-- 2. Insert Roles (2 roles: SUPER_ADMIN, USER)
-- =====================================================================

INSERT INTO auth_schema.roles (name, description, created_at, updated_at)
VALUES
    ('SUPER_ADMIN', 'Super administrator with all permissions', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('USER', 'Regular user - can book events and view own data', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (name) DO NOTHING;

-- =====================================================================
-- 3. Insert Users
-- =====================================================================

-- Password: admin123 (BCrypt với strength=10)
-- Generate tại: https://bcrypt-generator.com/
INSERT INTO auth_schema.users (username, email, password, full_name, role_id, enabled, created_at, updated_at)
VALUES
    ('admin', 'admin@ticketflow.com', '$2a$12$18X75fuWIxItrkFzC9JV4e4H8If6uYyjeXcAAXca2f9.MFpVL5cse', 'Super Administrator', 
     (SELECT id FROM auth_schema.roles WHERE name = 'SUPER_ADMIN'), 
     true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    
    ('testuser', 'user@ticketflow.com', '$2a$12$18X75fuWIxItrkFzC9JV4e4H8If6uYyjeXcAAXca2f9.MFpVL5cse', 'Test User', 
     (SELECT id FROM auth_schema.roles WHERE name = 'USER'), 
     true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (username) DO NOTHING;

-- =====================================================================
-- 4. Assign Permissions to Roles
-- =====================================================================

-- SUPER_ADMIN role - ALL permissions
INSERT INTO auth_schema.role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM auth_schema.roles r
CROSS JOIN auth_schema.permissions p
WHERE r.name = 'SUPER_ADMIN'
ON CONFLICT DO NOTHING;

-- USER role - Only BOOKING_MANAGE (can create/view own bookings)
INSERT INTO auth_schema.role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM auth_schema.roles r, auth_schema.permissions p
WHERE r.name = 'USER' AND p.name = 'BOOKING_MANAGE'
ON CONFLICT DO NOTHING;

-- =====================================================================
-- Verify inserted data
-- =====================================================================

-- Count all entities
SELECT 'Permissions' as entity, COUNT(*) as count FROM auth_schema.permissions
UNION ALL
SELECT 'Roles', COUNT(*) FROM auth_schema.roles
UNION ALL
SELECT 'Users', COUNT(*) FROM auth_schema.users
UNION ALL
SELECT 'Role-Permissions', COUNT(*) FROM auth_schema.role_permissions;

-- Show all users with their roles and permissions
SELECT 
    u.username,
    u.email,
    u.full_name,
    u.enabled,
    r.name as role,
    STRING_AGG(p.name, ', ') as permissions
FROM auth_schema.users u
LEFT JOIN auth_schema.roles r ON u.role_id = r.id
LEFT JOIN auth_schema.role_permissions rp ON r.id = rp.role_id
LEFT JOIN auth_schema.permissions p ON rp.permission_id = p.id
GROUP BY u.username, u.email, u.full_name, u.enabled, r.name
ORDER BY u.username;

-- =====================================================================
-- Login Information
-- =====================================================================
-- SUPER ADMIN:
--   Username: superadmin
--   Password: admin123
--   Email: admin@ticketflow.com
--
-- TEST USER:
--   Username: testuser
--   Password: admin123
--   Email: user@ticketflow.com
--
-- ⚠️  WARNING: Change default passwords in production!
-- =====================================================================
