-- =====================================================================
-- Script tạo users riêng cho từng service (Microservices Security Best Practice)
-- Mỗi service CHỈ có quyền trên schema của riêng nó
-- =====================================================================

-- =====================================================================
-- 1. AuthService
-- =====================================================================
CREATE USER auth_user WITH PASSWORD 'auth_password_123';
CREATE SCHEMA IF NOT EXISTS auth_schema AUTHORIZATION auth_user;
GRANT ALL PRIVILEGES ON SCHEMA auth_schema TO auth_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA auth_schema GRANT ALL ON TABLES TO auth_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA auth_schema GRANT ALL ON SEQUENCES TO auth_user;

-- =====================================================================
-- 2. EventService
-- =====================================================================
CREATE USER event_user WITH PASSWORD 'event_password_123';
CREATE SCHEMA IF NOT EXISTS event_schema AUTHORIZATION event_user;
GRANT ALL PRIVILEGES ON SCHEMA event_schema TO event_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA event_schema GRANT ALL ON TABLES TO event_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA event_schema GRANT ALL ON SEQUENCES TO event_user;

-- =====================================================================
-- 3. BookingService
-- =====================================================================
CREATE USER booking_user WITH PASSWORD 'booking_password_123';
CREATE SCHEMA IF NOT EXISTS booking_schema AUTHORIZATION booking_user;
GRANT ALL PRIVILEGES ON SCHEMA booking_schema TO booking_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA booking_schema GRANT ALL ON TABLES TO booking_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA booking_schema GRANT ALL ON SEQUENCES TO booking_user;

-- =====================================================================
-- 4. PaymentService
-- =====================================================================
CREATE USER payment_user WITH PASSWORD 'payment_password_123';
CREATE SCHEMA IF NOT EXISTS payment_schema AUTHORIZATION payment_user;
GRANT ALL PRIVILEGES ON SCHEMA payment_schema TO payment_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA payment_schema GRANT ALL ON TABLES TO payment_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA payment_schema GRANT ALL ON SEQUENCES TO payment_user;

-- =====================================================================
-- 5. NotifyService
-- =====================================================================
CREATE USER notify_user WITH PASSWORD 'notify_password_123';
CREATE SCHEMA IF NOT EXISTS notify_schema AUTHORIZATION notify_user;
GRANT ALL PRIVILEGES ON SCHEMA notify_schema TO notify_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA notify_schema GRANT ALL ON TABLES TO notify_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA notify_schema GRANT ALL ON SEQUENCES TO notify_user;

-- =====================================================================
-- Verify users và schemas
-- =====================================================================
SELECT 
    u.usename as username,
    n.nspname as schema_owned,
    CASE WHEN u.usesuper THEN 'YES' ELSE 'NO' END as is_superuser
FROM pg_user u
LEFT JOIN pg_namespace n ON n.nspowner = u.usesysid
WHERE u.usename IN ('auth_user', 'event_user', 'booking_user', 'payment_user', 'notify_user')
ORDER BY u.usename;

-- =====================================================================
-- Giải thích Security Model
-- =====================================================================
-- 
-- ✅ ĐÚNG: Mỗi service có user riêng, chỉ access schema của nó
--    - AuthService → auth_user → auth_schema ONLY
--    - EventService → event_user → event_schema ONLY
--    - BookingService → booking_user → booking_schema ONLY
--    - etc.
--
-- ❌ SAI: Tất cả services dùng user 'postgres'
--    - Mọi service có thể query bất kỳ schema nào
--    - Không có isolation giữa các services
--    - Vi phạm nguyên tắc "Least Privilege"
--
-- 📌 Lợi ích:
--    1. Security: Service A KHÔNG THỂ đọc/ghi data của Service B
--    2. Audit: Dễ track xem service nào đang access database
--    3. Best Practice: Đúng với kiến trúc microservices
--    4. Production Ready: Sẵn sàng cho môi trường thực tế
--
-- =====================================================================
