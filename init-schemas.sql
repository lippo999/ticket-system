-- Script để tạo các schemas cho từng microservice
-- Chạy script này một lần để khởi tạo database structure

-- Tạo schema cho AuthService
CREATE SCHEMA IF NOT EXISTS auth_schema;

-- Tạo schema cho EventService  
CREATE SCHEMA IF NOT EXISTS event_schema;

-- Tạo schema cho BookingService
CREATE SCHEMA IF NOT EXISTS booking_schema;

-- Tạo schema cho PaymentService
CREATE SCHEMA IF NOT EXISTS payment_schema;

-- Tạo schema cho NotifyService
CREATE SCHEMA IF NOT EXISTS notify_schema;

-- Grant quyền cho user postgres (hoặc user của bạn)
GRANT ALL PRIVILEGES ON SCHEMA auth_schema TO postgres;
GRANT ALL PRIVILEGES ON SCHEMA event_schema TO postgres;
GRANT ALL PRIVILEGES ON SCHEMA booking_schema TO postgres;
GRANT ALL PRIVILEGES ON SCHEMA payment_schema TO postgres;
GRANT ALL PRIVILEGES ON SCHEMA notify_schema TO postgres;

-- Hiển thị tất cả schemas
SELECT schema_name FROM information_schema.schemata;
