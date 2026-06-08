-- SafeGuard Backend MySQL Database Schema
-- Matches the SQLAlchemy models declared in server/auth_routes.py

-- 1. Users Table
CREATE TABLE IF NOT EXISTS `users` (
    `id` VARCHAR(50) NOT NULL,
    `fullName` VARCHAR(100) NOT NULL,
    `email` VARCHAR(150) NOT NULL,
    `phone` VARCHAR(50) NOT NULL,
    `salt_hex` VARCHAR(64) NOT NULL,
    `password_hash` VARCHAR(128) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_users_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. OTP Code Table (Dynamic short-lived codes)
CREATE TABLE IF NOT EXISTS `otps` (
    `phone` VARCHAR(50) NOT NULL,
    `code` VARCHAR(20) NOT NULL,
    `expires_at` DOUBLE NOT NULL,
    PRIMARY KEY (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. Password Reset Tokens Table
CREATE TABLE IF NOT EXISTS `password_resets` (
    `token` VARCHAR(100) NOT NULL,
    `email` VARCHAR(150) NOT NULL,
    `expires_at` DOUBLE NOT NULL,
    PRIMARY KEY (`token`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
